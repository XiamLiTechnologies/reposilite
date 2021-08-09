package com.reposilite.maven

import com.reposilite.maven.api.RepositoryVisibility.HIDDEN
import com.reposilite.maven.api.RepositoryVisibility.PRIVATE
import com.reposilite.maven.api.RepositoryVisibility.PUBLIC
import com.reposilite.token.api.AccessToken
import com.reposilite.token.api.RoutePermission
import com.reposilite.token.api.RoutePermission.READ
import com.reposilite.token.api.RoutePermission.WRITE
import java.nio.file.Path

internal class RepositorySecurityProvider {

    fun canAccessResource(accessToken: AccessToken?, repository: Repository, gav: Path): Boolean =
        when (repository.visibility) {
            PUBLIC -> true
            HIDDEN -> true
            PRIVATE -> hasPermissionTo(accessToken, repository, gav, READ)
        }

    fun canBrowseResource(accessToken: AccessToken?, repository: Repository, gav: Path): Boolean =
        when (repository.visibility) {
            PUBLIC -> true
            HIDDEN -> hasPermissionTo(accessToken, repository, gav, READ)
            PRIVATE -> hasPermissionTo(accessToken, repository, gav, READ)
        }

    fun canModifyResource(accessToken: AccessToken?, repository: Repository, gav: Path): Boolean =
        hasPermissionTo(accessToken, repository, gav, WRITE)

    private fun hasPermissionTo(accessToken: AccessToken?, repository: Repository, gav: Path, permission: RoutePermission): Boolean =
        accessToken?.hasPermissionTo("/" + repository.name + "/" + gav.toString().replace("\\", "/"), permission) ?: false

}