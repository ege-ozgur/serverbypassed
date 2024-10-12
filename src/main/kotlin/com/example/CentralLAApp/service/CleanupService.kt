package com.example.CentralLAApp.service

import com.example.CentralLAApp.repository.NotificationRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CleanupService(val notificationRepository: NotificationRepository) {

    companion object  {
        const val DELETE_NOTIFICATIONS_OLDER_THAN_IN_DAYS: Long = 60
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun cleanupOldData() {
        val sixtyDaysAgo = LocalDateTime.now().minusDays(DELETE_NOTIFICATIONS_OLDER_THAN_IN_DAYS)
        notificationRepository.deleteRecordsOlderThan(sixtyDaysAgo)
    }
}
