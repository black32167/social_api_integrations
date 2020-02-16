package org.social.integration.mattermost.api

import org.social.integration.mattermost.ApiHttpService
import social.api.user.model.User
import social.api.user.model.UserAuth
import social.api.user.model.UserCredentials
import social.api.user.model.Users
import social.api.user.model.ValidationResult
import social.api.user.server.UserApiService

class MMUserService(private val httpClient: ApiHttpService) : UserApiService {


    override fun setCredentials(p0: String?, p1: UserCredentials?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun validate(p0: UserAuth?): ValidationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUser(p0: String?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUsers(): Users {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createUser(p0: User?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}