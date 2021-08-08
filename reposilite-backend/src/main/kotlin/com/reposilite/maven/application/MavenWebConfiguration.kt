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

package com.reposilite.maven.application

import com.reposilite.config.Configuration.RepositoryConfiguration
import com.reposilite.failure.FailureFacade
import com.reposilite.maven.MavenFacade
import com.reposilite.maven.MetadataService
import com.reposilite.maven.RepositoryFactory
import com.reposilite.maven.RepositorySecurityProvider
import com.reposilite.maven.RepositoryService
import com.reposilite.maven.infrastructure.MavenFileEndpoint
import com.reposilite.shared.HttpRemoteClient
import com.reposilite.web.api.Routes
import net.dzikoysk.dynamiclogger.Journalist
import java.nio.file.Path
import java.util.concurrent.Executors

internal object MavenWebConfiguration {

    fun createFacade(journalist: Journalist, failureFacade: FailureFacade, workingDirectory: Path, repositories: Map<String, RepositoryConfiguration>): MavenFacade {
        val repositoryFactory = RepositoryFactory(journalist, workingDirectory, Executors.newCachedThreadPool(), HttpRemoteClient())

        val repositoryService = repositories
            .mapValues { (repositoryName, repositoryConfiguration) -> repositoryFactory.createRepository(repositoryName, repositoryConfiguration) }
            .let { RepositoryService(journalist, it) }

        return MavenFacade(journalist, MetadataService(failureFacade), RepositorySecurityProvider(), repositoryService)
    }

    fun routing(mavenFacade: MavenFacade): List<Routes> =
        listOf(
            MavenFileEndpoint(mavenFacade),
        )

}