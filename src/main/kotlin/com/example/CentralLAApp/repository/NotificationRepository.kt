package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.userID = :userId ORDER BY n.timestamp DESC")
    fun findByIdByAndUserId(userId: Int): Collection<Notification>

    @Query("SELECT n FROM Notification n WHERE n.user.userID = :userId AND n.notificationId in :idList")
    fun findAllByIdsAndUserId(idList: List<Long>, userId: Int): Collection<Notification>


    @Modifying
    @Query("DELETE FROM Notification n WHERE n.timestamp < :date")
    fun deleteRecordsOlderThan(date: LocalDateTime)
}