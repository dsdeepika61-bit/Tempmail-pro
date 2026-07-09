package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DomainListResponse(
    @Json(name = "hydra:member") val member: List<Domain>
)

@JsonClass(generateAdapter = true)
data class Domain(
    val id: String,
    val domain: String,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class AccountRequest(
    val address: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AccountResponse(
    val id: String,
    val address: String
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    val token: String,
    val id: String
)

@JsonClass(generateAdapter = true)
data class MessageListResponse(
    @Json(name = "hydra:member") val member: List<MessageIntro>
)

@JsonClass(generateAdapter = true)
data class MessageIntro(
    val id: String,
    val accountId: String,
    val msgid: String,
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val subject: String,
    val intro: String,
    val seen: Boolean,
    val isDeleted: Boolean,
    val hasAttachments: Boolean,
    val size: Int,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class MessageDetail(
    val id: String,
    val accountId: String,
    val msgid: String,
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val subject: String,
    val intro: String,
    val seen: Boolean,
    val isDeleted: Boolean,
    val hasAttachments: Boolean,
    val size: Int,
    val createdAt: String,
    val updatedAt: String,
    val text: String?,
    val html: List<String>?,
    val attachments: List<Attachment>?
)

@JsonClass(generateAdapter = true)
data class EmailAddress(
    val address: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class Attachment(
    val id: String,
    val filename: String,
    val contentType: String,
    val size: Int,
    val downloadUrl: String
)
