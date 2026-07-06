package com.passgo.app.feature.autofill.matcher

import com.passgo.app.feature.autofill.domain.DomainHandler
import com.passgo.app.feature.autofill.model.AutofillCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialMatcher @Inject constructor(
    private val domainHandler: DomainHandler
) {
    fun findMatchingCredentials(
        credentials: List<AutofillCredential>,
        packageName: String,
        domain: String?
    ): List<AutofillCredential> {
        val matched = credentials.filter { credential ->
            credential.url.isNotBlank() && matchesAppContext(credential.url, packageName, domain)
        }
        return sortCredentials(matched)
    }

    fun matchesAppContext(url: String, packageName: String, domain: String?): Boolean {
        if (domain != null && domainHandler.matchesDomain(domain, url)) return true
        val packageDomain = domainHandler.extractDomainFromPackageName(packageName)
        if (packageDomain != null && domainHandler.matchesDomain(packageDomain, url)) return true
        return false
    }

    private fun sortCredentials(credentials: List<AutofillCredential>): List<AutofillCredential> {
        return credentials.sortedWith(
            compareByDescending<AutofillCredential> { it.favorite }
                .thenByDescending { it.updatedAt }
                .thenBy { it.name.lowercase() }
        )
    }
}
