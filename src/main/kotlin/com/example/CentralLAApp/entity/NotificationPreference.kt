package com.example.CentralLAApp.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class NotificationPreference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var directPush: Boolean = true,
    var directEmail: Boolean = true,
    var followingPush: Boolean = true,
    var followingEmail: Boolean = true,
    var followingNewAnnouncement: Boolean = false
)