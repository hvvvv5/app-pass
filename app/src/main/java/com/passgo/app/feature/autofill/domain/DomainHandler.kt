package com.passgo.app.feature.autofill.domain

import android.util.Patterns
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainHandler @Inject constructor() {

    fun extractDomain(url: String): String? {
        return try {
            var uri = URI(url)
            var host = uri.host
            if (host == null && !url.contains("://")) {
                uri = URI("https://$url")
                host = uri.host
            }
            host?.let { normalizeDomain(it) }
        } catch (_: Exception) {
            null
        }
    }

    fun extractDomainFromPackageName(packageName: String): String? {
        val knownDomains = mapOf(
            "com.google.android.gm" to "mail.google.com",
            "com.google.android.apps.docs" to "docs.google.com",
            "com.google.android.youtube" to "youtube.com",
            "com.facebook.katana" to "facebook.com",
            "com.facebook.orca" to "facebook.com",
            "com.twitter.android" to "twitter.com",
            "com.instagram.android" to "instagram.com",
            "com.linkedin.android" to "linkedin.com",
            "com.slack" to "slack.com",
            "com.microsoft.office.outlook" to "outlook.com",
            "com.netflix.mediaclient" to "netflix.com",
            "com.amazon.mShop.android.shopping" to "amazon.com",
            "com.amazon." to "amazon.com",
            "com.dropbox.android" to "dropbox.com",
            "com.github.android" to "github.com",
            "com.spotify.music" to "spotify.com",
            "com.trello" to "trello.com",
            "com.whatsapp" to "whatsapp.com",
            "org.telegram.messenger" to "telegram.org",
            "com.zoom.videomeetings" to "zoom.us"
        )

        return knownDomains.entries.firstOrNull { (prefix, _) ->
            packageName.startsWith(prefix)
        }?.value
    }

    fun normalizeDomain(domain: String): String {
        var normalized = domain.lowercase().trim()

        if (normalized.startsWith("www.")) {
            normalized = normalized.removePrefix("www.")
        }

        return normalized
    }

    fun matchesDomain(domain: String, savedUrl: String): Boolean {
        val normalizedDomain = normalizeDomain(domain)
        val normalizedSaved = normalizeDomain(extractDomain(savedUrl) ?: return false)

        if (normalizedDomain == normalizedSaved) return true

        if (normalizedSaved.endsWith(".$normalizedDomain") ||
            normalizedDomain.endsWith(".$normalizedSaved")
        ) return true

        return false
    }

    fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    fun isKnownLoginPage(domain: String): Boolean {
        val loginIndicators = listOf("login", "signin", "auth", "account", "logon", "sign-in", "log-in")
        val normalized = domain.lowercase()
        return loginIndicators.any { normalized.contains(it) }
    }
}
