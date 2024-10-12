package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.NotificationPreferenceChangeRequest
import com.example.CentralLAApp.dto.request.NotificationRequest
import com.example.CentralLAApp.dto.request.NotificationsChangeRequest
import com.example.CentralLAApp.dto.request.PublicNotificationRequest
import com.example.CentralLAApp.dto.response.NotificationResponse
import com.example.CentralLAApp.dto.response.PublicNotificationResponse
import com.example.CentralLAApp.dto.toNotification
import com.example.CentralLAApp.dto.toNotificationResponse
import com.example.CentralLAApp.entity.Notification
import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.NotificationRelationType
import com.example.CentralLAApp.enums.NotificationType
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.InvalidInputException
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import com.example.CentralLAApp.repository.NotificationPreferenceRepository
import com.example.CentralLAApp.repository.NotificationRepository
import com.example.CentralLAApp.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class NotificationService {


    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var notificationPreferenceRepository: NotificationPreferenceRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var messagingTemplate: SimpMessagingTemplate

    private val notificationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object : KLogging()

    suspend fun sendNotification(notificationRequest: NotificationRequest) = withContext(Dispatchers.IO) {
        val user = userRepository.findById(notificationRequest.userId).orElseThrow {
            NotFoundException("User with id: ${notificationRequest.userId} not found.")
        }

        val now = LocalDateTime.now()
        val response = NotificationResponse(
            id = null,
            username = user.username,
            title = notificationRequest.title,
            description = if (notificationRequest.notificationType == NotificationType.STUDENT_STATUS_UPDATE) notificationRequest.title else notificationRequest.description,
            timestamp = (notificationRequest.timestamp ?: now).toString(),
            read = false,
            notificationType = notificationRequest.notificationType,
            relation = notificationRequest.relation,
            applicationId = notificationRequest.applicationId,
            applicationRequestId = notificationRequest.applicationRequestId
        )
        val mapper = ObjectMapper().registerModule(JavaTimeModule())
        val jsonString = mapper.writeValueAsString(response)


        if ((notificationRequest.relation == NotificationRelationType.DIRECT && notificationRequest.preferences.directEmail)
            || (notificationRequest.relation == NotificationRelationType.FOLLOW && notificationRequest.preferences.followingEmail)
        ) {

            emailService.sendEmail(
                to = user.username,
                subject = notificationRequest.title,
                text = notificationRequest.description
            )
        }

        messagingTemplate.convertAndSendToUser(notificationRequest.userId.toString(), "/notifications", jsonString)

        val notification: Notification = notificationRequest.toNotification(user, now)
        notificationRepository.save(notification)
    }


    fun sendMultipleNotificationsAsync(
        interestedClients: List<User>,
        title: String,
        description: String,
        notificationType: NotificationType,
        relation: NotificationRelationType,
        roleBased: Boolean = false,
        applicationId: Long? = null,
        applicationRequestId: Long? = null
    ) {
        val currentTime = LocalDateTime.now()
        interestedClients.forEach { user ->
            notificationScope.launch {
                try {
                    sendNotification(
                        NotificationRequest(
                            userId = user.userID,
                            title = title,
                            description = description,
                            notificationType = notificationType,
                            timestamp = currentTime,
                            preferences = user.notificationPreferences,
                            relation = when {
                                (!roleBased || user.role == UserRole.INSTRUCTOR) -> NotificationRelationType.DIRECT
                                else -> NotificationRelationType.FOLLOW
                            },
                            applicationId = applicationId,
                            applicationRequestId = applicationRequestId
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Error sending notification to user ${user.userID}", e)
                }
            }
        }
    }

    fun sendPublicNotification(publicNotificationRequest: PublicNotificationRequest) {
        val response = PublicNotificationResponse(
            title = publicNotificationRequest.title,
            description = publicNotificationRequest.description,
            timestamp = (publicNotificationRequest.timestamp).toString(),
            notificationType = publicNotificationRequest.notificationType,
            relation = NotificationRelationType.FOLLOW
        )

        val mapper = ObjectMapper()
        val jsonString = mapper.writeValueAsString(response)
        messagingTemplate.convertAndSend("/topic", jsonString)


    }


    fun getNotifications(userId: Int): List<NotificationResponse> {
        return notificationRepository.findByIdByAndUserId(userId).map {
            it.toNotificationResponse(it.user.email)
        }
    }

    fun changeNotificationStatus(userId: Int, notificationsChangeRequest: NotificationsChangeRequest) {

        val (idList, notificationIdToStatusMap) = notificationsChangeRequest.notificationChanges.fold(
            Pair(emptyList<Long>(), hashMapOf<Long, Boolean>())
        ) { (ids, statusMap), change ->
            (ids + change.notificationId) to statusMap.apply { put(change.notificationId, change.read) }
        }

        if (idList.hasDuplicates()) {
            throw InvalidInputException("Duplicates are not allowed in id list")
        }

        val notifications = notificationRepository.findAllByIdsAndUserId(idList, userId)

        if (notifications.map { it.notificationId }.sorted() != idList.sorted()) {
            throw UnauthorizedException()
        }

        notifications.map {
            it.readStatus = notificationIdToStatusMap[it.notificationId] ?: throw UnauthorizedException()
        }

        notificationRepository.saveAll(notifications)
    }

    fun changeNotificationPreferences(
        userId: Int,
        notificationPreferenceChangeRequest: NotificationPreferenceChangeRequest
    ): NotificationPreference {
        return userRepository.findById(userId).get().notificationPreferences.apply {
            notificationPreferenceChangeRequest.run {
                directPush?.let { this@apply.directPush = it }
                followingEmail?.let { this@apply.followingEmail = it }
                followingPush?.let { this@apply.followingPush = it }
                followingNewAnnouncement?.let { this@apply.followingNewAnnouncement = it }
            }
        }.also {
            notificationPreferenceRepository.save(it)
            it.convertToNotificationPreferenceResponse()
        }
    }

    fun getNotificationPreferences(userId: Int): NotificationPreference {
        return userRepository.findById(userId).get().notificationPreferences.also {
            it.convertToNotificationPreferenceResponse()
        }
    }

    fun getUnreadNotificationCount(userId: Int): Int {
        return notificationRepository.findByIdByAndUserId(userId).count {
            it.readStatus.not()
        }
    }
}

private fun NotificationPreference.convertToNotificationPreferenceResponse() = NotificationPreference(
    id = id,
    directPush = directPush,
    directEmail = directEmail,
    followingPush = followingPush,
    followingEmail = followingEmail,
    followingNewAnnouncement = followingNewAnnouncement
)

fun <T> List<T>.hasDuplicates(): Boolean {
    return this.size != this.toSet().size
}
