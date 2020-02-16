package org.social.integration.mattermost.service.user

class UserResponseItem (
    val id: String,
    val create_at: Long,
    val update_at: Long,
    val delete_at: Long,
    val username: String,
    val first_name: String,
    val last_name: String,
    val nickname: String,
    val email: String,
    val email_verified: Boolean,
    val auth_service: String,
    val roles: String
)