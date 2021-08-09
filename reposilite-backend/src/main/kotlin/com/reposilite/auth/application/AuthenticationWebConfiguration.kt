/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reposilite.auth.application

import net.dzikoysk.dynamiclogger.Journalist
import com.reposilite.auth.AuthenticationFacade
import com.reposilite.auth.Authenticator
import com.reposilite.auth.SessionService
import com.reposilite.auth.infrastructure.AuthenticationEndpoint
import com.reposilite.auth.infrastructure.PostAuthHandler
import com.reposilite.maven.MavenFacade
import com.reposilite.token.AccessTokenFacade
import com.reposilite.web.api.Routes

internal object AuthenticationWebConfiguration {

    fun createFacade(journalist: Journalist, accessTokenFacade: AccessTokenFacade, mavenFacade: MavenFacade): AuthenticationFacade {
        val authenticator = Authenticator(accessTokenFacade)
        val sessionService = SessionService(mavenFacade)

        return AuthenticationFacade(journalist, authenticator, sessionService)
    }

    fun routing(authenticationFacade: AuthenticationFacade): Set<Routes> =
        setOf(
            AuthenticationEndpoint(authenticationFacade),
            PostAuthHandler()
        )

}