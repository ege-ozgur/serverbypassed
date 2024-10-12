package com.example.CentralLAApp.dto.request

data class NotificationPreferenceChangeRequest(
    val directPush: Boolean?,
    val followingPush: Boolean?,
    val followingEmail: Boolean?,
    val followingNewAnnouncement: Boolean?
) {

}
