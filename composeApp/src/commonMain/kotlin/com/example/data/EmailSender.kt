package com.example.data

expect object EmailSender {
    fun isConfigured(): Boolean
    suspend fun sendOtpEmail(
        recipientEmail: String,
        otp: String,
        brandName: String = "ملازمي - الرفاهية"
    ): Result<Unit>
    fun launchEmailComposer(recipientEmail: String, subject: String, body: String): Boolean
}
