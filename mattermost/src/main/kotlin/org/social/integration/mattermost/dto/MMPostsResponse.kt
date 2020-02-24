package org.social.integration.mattermost.dto

class MMPostsResponse (
        val order: Array<String>,
        val posts: Map<String, MMPostResponse>,
        val next_post_id: String?,
        val prev_post_id: String?
)