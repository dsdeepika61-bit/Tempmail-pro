package com.example.data

import com.example.api.AccountRequest
import com.example.api.MailTmApi
import com.example.api.MessageDetail
import com.example.api.MessageIntro
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class EmailRepository(
    private val api: MailTmApi,
    private val authManager: AuthManager
) {
    suspend fun getAvailableDomain(): String {
        val domains = api.getDomains().member
        return domains.firstOrNull { it.isActive }?.domain ?: throw Exception("No active domains found")
    }

    suspend fun generateNewAccount() {
        val domain = getAvailableDomain()
        val username = generateRandomUsername()
        val address = "$username@$domain"
        val password = generateRandomPassword()

        val request = AccountRequest(address, password)
        val account = api.createAccount(request)
        val tokenResponse = api.getToken(request)

        authManager.saveAccount(address, password, tokenResponse.token, account.id)
    }

    suspend fun getMessages(): List<MessageIntro> {
        val token = authManager.tokenFlow.firstOrNull() ?: return emptyList()
        return api.getMessages("Bearer $token").member
    }

    suspend fun getMessageDetail(id: String): MessageDetail {
        val token = authManager.tokenFlow.firstOrNull() ?: throw Exception("Not authenticated")
        return api.getMessage("Bearer $token", id)
    }

    suspend fun deleteMessage(id: String) {
        val token = authManager.tokenFlow.firstOrNull() ?: return
        api.deleteMessage("Bearer $token", id)
    }

    private fun generateRandomUsername(): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..12).map { allowedChars.random() }.joinToString("")
    }

    private fun generateRandomPassword(): String {
        val uppercase = ('A'..'Z').random()
        val lowercase = (1..8).map { ('a'..'z').random() }.joinToString("")
        val digits = (1..3).map { ('0'..'9').random() }.joinToString("")
        val special = listOf('!', '@', '#', '$').random()
        return "$uppercase$lowercase$digits$special"
    }
}
