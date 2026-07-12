package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    // Check if the credentials are set and not placeholder values
    fun isConfigured(): Boolean {
        // Safe check for build variables
        return try {
            val email = BuildConfig.SMTP_EMAIL
            val pass = BuildConfig.SMTP_PASSWORD
            email.isNotBlank() && email != "MY_SMTP_EMAIL" && email != "YOUR_SMTP_EMAIL" &&
                    pass.isNotBlank() && pass != "MY_SMTP_PASSWORD" && pass != "YOUR_SMTP_PASSWORD"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendOtpEmail(recipientEmail: String, otp: String, brandName: String = "ملازمي - الرفاهية"): Result<Unit> = withContext(Dispatchers.IO) {
        val smtpEmail = BuildConfig.SMTP_EMAIL
        val smtpPassword = BuildConfig.SMTP_PASSWORD

        if (!isConfigured()) {
            return@withContext Result.failure(Exception("SMTP_NOT_CONFIGURED"))
        }

        // Luxurious gold & navy RTL themed HTML email
        val htmlBody = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <title>رمز التحقق الآمن</title>
                <style>
                    body {
                        font-family: 'Cairo', Arial, sans-serif;
                        background-color: #F5F7FA;
                        margin: 0;
                        padding: 0;
                        text-align: right;
                    }
                    .container {
                        max-width: 580px;
                        margin: 30px auto;
                        background-color: #FFFFFF;
                        border-radius: 12px;
                        box-shadow: 0 4px 15px rgba(26,46,64,0.06);
                        overflow: hidden;
                        border: 1px solid #E5E9F0;
                    }
                    .header {
                        background-color: #1A2E40; /* Resort Navy */
                        padding: 35px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        color: #D4AF37; /* Luxury Gold */
                        font-weight: bold;
                        letter-spacing: 1px;
                    }
                    .content {
                        padding: 35px 30px;
                        color: #2D3748;
                        line-height: 1.8;
                    }
                    .greeting {
                        font-size: 18px;
                        font-weight: bold;
                        color: #1A2E40;
                        margin-bottom: 15px;
                    }
                    .desc {
                        font-size: 14px;
                        color: #4A5568;
                        margin-bottom: 25px;
                    }
                    .otp-card {
                        background-color: #FCF9EE;
                        border: 2px dashed #D4AF37;
                        border-radius: 10px;
                        padding: 24px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .otp-code {
                        font-size: 36px;
                        font-weight: 800;
                        letter-spacing: 8px;
                        color: #1A2E40;
                        margin: 0;
                    }
                    .warning-box {
                        background-color: #FFF5F5;
                        border-right: 4px solid #E53E3E;
                        border-radius: 6px;
                        padding: 15px;
                        margin-top: 30px;
                        font-size: 13px;
                        color: #C53030;
                        font-weight: bold;
                    }
                    .footer {
                        background-color: #F7FAFC;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #718096;
                        border-top: 1px solid #E5E9F0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>$brandName</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">أهلاً بك،</div>
                        <p class="desc">لقد طلبت الحصول على رمز تحقق مؤقت (OTP) لتسجيل الدخول الفوري والآمن إلى حسابك.</p>
                        
                        <div class="otp-card">
                            <div class="otp-code">$otp</div>
                        </div>
                        
                        <p class="desc">يرجى إدخال الرمز أعلاه في التطبيق لإكمال عملية المصادقة. الرمز صالح للاستخدام لمرة واحدة فقط لمدة 10 دقائق.</p>
                        
                        <div class="warning-box">
                            ⚠️ تنبيه أمني عاجل: لا تشارك هذا الرمز السري مع أي شخص على الإطلاق لحماية سرية بياناتك.
                        </div>
                    </div>
                    <div class="footer">
                        &copy; 2026 $brandName. جميع الحقوق محفوظة لخدمات الفخامة والرفاهية.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        var lastException: Exception? = null

        // Strategy 1: Send via Port 465 SSL
        try {
            sendSsl(smtpEmail, smtpPassword, recipientEmail, brandName, htmlBody)
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EmailSender", "SSL (465) failed: ${e.message}. Attempting STARTTLS (587)...", e)
            lastException = e
        }

        // Strategy 2: Fallback to Port 587 STARTTLS
        try {
            sendStartTls(smtpEmail, smtpPassword, recipientEmail, brandName, htmlBody)
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EmailSender", "STARTTLS (587) failed: ${e.message}", e)
            lastException = e
        }

        val errorText = lastException?.message ?: "خطأ غير معروف"
        val isAuthError = errorText.contains("AuthenticationFailedException", ignoreCase = true) ||
                errorText.contains("authenticate", ignoreCase = true) ||
                errorText.contains("Password", ignoreCase = true) ||
                errorText.contains("username", ignoreCase = true)

        if (isAuthError) {
            Result.failure(Exception("يرجى التحقق من تفعيل واستخدام كلمة مرور التطبيق المكونة من 16 حرفاً (Google App Password) لبريدك الإلكتروني، بدلاً من كلمة المرور العادية للتأمين."))
        } else {
            Result.failure(Exception("فشل إرسال بريد التحقق الإلكتروني: $errorText"))
        }
    }

    private fun sendSsl(smtpEmail: String, smtpPassword: String, recipientEmail: String, brandName: String, htmlBody: String) {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", "465")
            put("mail.smtp.connectiontimeout", "6000")
            put("mail.smtp.timeout", "6000")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpEmail, smtpPassword)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpEmail, brandName))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            subject = "رمز التحقق الثنائي لـ $brandName"
            setContent(htmlBody, "text/html; charset=utf-8")
        }

        Transport.send(message)
        Log.i("EmailSender", "Email delivered successfully via SSL Port 465")
    }

    private fun sendStartTls(smtpEmail: String, smtpPassword: String, recipientEmail: String, brandName: String, htmlBody: String) {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.connectiontimeout", "6000")
            put("mail.smtp.timeout", "6000")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpEmail, smtpPassword)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpEmail, brandName))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            subject = "رمز التحقق الثنائي لـ $brandName"
            setContent(htmlBody, "text/html; charset=utf-8")
        }

        Transport.send(message)
        Log.i("EmailSender", "Email delivered successfully via STARTTLS Port 587")
    }
}
