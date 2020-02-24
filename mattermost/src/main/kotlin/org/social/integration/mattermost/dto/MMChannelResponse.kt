package org.social.integration.mattermost.dto

class MMChannelResponse(
        val id:String,
        val create_at:Long,
        val update_at:Long,
        val delete_at:Long,
        val team_id: String,
        val display_name: String,
        val type:String,
        val name:String,
        val header:String,
        val purpose:String,
        val last_post_at:Long,
        val total_msg_count:Int,
        val extra_update_at:Long,
        val creator_id:String,
        val scheme_id:String?
        )