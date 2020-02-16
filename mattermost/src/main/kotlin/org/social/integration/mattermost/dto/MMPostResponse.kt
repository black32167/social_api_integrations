package org.social.integration.mattermost.dto

class MMPostResponse(
        val id:String,
        val create_at:Long,
        val update_at:Long,
        val edit_at:Long,
        val delete_at:Long,
        val is_pinned:Boolean,
        val user_id:String,
        val channel_id:String,
        val root_id:String,
        val parent_id:String,
        val original_id:String,
        val message:String,
        val type:String,
        val props:Map<String, String>,
        val hashtags:String,
        val pending_post_id:String,
        val metadata:Map<String, String>
)