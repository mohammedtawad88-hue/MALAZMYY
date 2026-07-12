package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EmailSender
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// Custom Theme Colors (ResortNavy, LuxuryGold, SlateBlue)
val ResortNavy = Color(0xFF1A2E40)
val LuxuryGold = Color(0xFFD4AF37)
val SlateBlue = Color(0xFF4A607A)
val ErrorRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSignInDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var step by remember { mutableStateOf(1) } // 1: Input, 2: Verification
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    
    var generatedOtp by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSimulationMode by remember { mutableStateOf(false) }

    val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Brand Title
                Text(
                    text = "ملازمي - الرفاهية",
                    color = LuxuryGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "بوابة دخول المستخدم الآمنة",
                    color = ResortNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = ResortNavy.copy(alpha = 0.1f), thickness = 1.dp)

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "dialog_steps"
                ) { currentStep ->
                    if (currentStep == 1) {
                        // Step 1: Input
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "يرجى إدخال اسمك الكامل وبريدك الإلكتروني لتلقي رمز التحقق السري OTP.",
                                color = SlateBlue,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 20.sp
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    errorMessage = null
                                },
                                label = { Text("الاسم الكامل") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    focusedLabelColor = ResortNavy,
                                    unfocusedBorderColor = SlateBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it.trim()
                                    errorMessage = null
                                },
                                label = { Text("البريد الإلكتروني") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    focusedLabelColor = ResortNavy,
                                    unfocusedBorderColor = SlateBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (fullName.trim().length < 3) {
                                        errorMessage = "الرجاء إدخال اسم صحيح (3 أحرف على الأقل)"
                                        return@Button
                                    }
                                    if (!email.matches(emailRegex)) {
                                        errorMessage = "الرجاء إدخال بريد إلكتروني صحيح ومكتمل"
                                        return@Button
                                    }

                                    isSending = true
                                    errorMessage = null
                                    
                                    val otp = (1000..9999).random().toString()
                                    generatedOtp = otp

                                    scope.launch {
                                        if (EmailSender.isConfigured()) {
                                            val result = EmailSender.sendOtpEmail(email, otp)
                                            isSending = false
                                            if (result.isSuccess) {
                                                isSimulationMode = false
                                                step = 2
                                                Toast.makeText(context, "📩 تم إرسال رمز التحقق إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show()
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message ?: "فشل في إرسال البريد الإلكتروني"
                                            }
                                        } else {
                                            // Fallback to Simulation Mode
                                            isSending = false
                                            isSimulationMode = true
                                            step = 2
                                            Toast.makeText(context, "⚠️ نظام البريد غير مهيأ. تم تشغيل وضع المحاكاة للتطوير.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isSending,
                                colors = ButtonDefaults.buttonColors(containerColor = ResortNavy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("أرسل رمز التحقق ✉️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        // Step 2: Verification Code Entry
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تم إرسال الرمز بنجاح لعنوان بريدك:\n$email\nيرجى مراجعة بريدك الإلكتروني (الوارد أو المزعج/Spam).",
                                color = ResortNavy,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )

                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        enteredOtp = it.replace(Regex("[^0-9]"), "")
                                        errorMessage = null
                                    }
                                },
                                label = { Text("رمز التحقق السري (4 أرقام)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryGold,
                                    focusedLabelColor = ResortNavy,
                                    unfocusedBorderColor = SlateBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (enteredOtp == generatedOtp || (isSimulationMode && enteredOtp == generatedOtp)) {
                                            viewModel.loginAsUser(fullName, email)
                                            onSuccess()
                                        } else {
                                            errorMessage = "❌ رمز التحقق غير صحيح! يرجى إعادة المحاولة."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ResortNavy),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("تأكيد الدخول ✅", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        step = 1
                                        enteredOtp = ""
                                        errorMessage = null
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("تعديل البيانات", color = SlateBlue)
                                }
                            }

                            // Simulation warning banner
                            if (isSimulationMode) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4EC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5B041)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "DEVELOPER: SMTP NOT CONFIGURERD",
                                                color = Color(0xFFB7950B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "رمز التحقق المحاكي: $generatedOtp",
                                                color = Color(0xFF7D6608),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(generatedOtp))
                                                Toast.makeText(context, "تم نسخ الرمز: $generatedOtp", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8A020)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("نسخ الرمز", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
