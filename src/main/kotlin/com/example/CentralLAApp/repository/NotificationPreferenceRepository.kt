package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.Notification
import com.example.CentralLAApp.entity.NotificationPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface NotificationPreferenceRepository : JpaRepository<NotificationPreference, Long> {


}