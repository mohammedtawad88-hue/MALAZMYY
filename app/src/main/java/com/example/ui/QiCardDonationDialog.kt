package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Brand colors for Qi Card (Rafidain)
val QiDeepBlue = Color(0xFF0F2C59)
val QiGold = Color(0xFFE8A020)
val QiLightGold = Color(0xFFF8C462)
val QiSlate = Color(0xFF34495E)
val QiYellowBg = Color(0xFFFCD116) // Vibrant Qi Yellow

val qrCodeGrid = listOf(
    "1111111010101100101111111",
    "1000001001001010101000001",
    "1011101011011001101011101",
    "1011101001101101001011101",
    "1011101011001011101011101",
    "1000001000110100001000001",
    "1111111010101010101111111",
    "0000000011010111110000000",
    "1110110001101011000101011",
    "1001011101010011101101010",
    "0011001011001110101000111",
    "1011010101111011001111100",
    "1100110011011101010010111",
    "0111011101000110111101010",
    "1010100011010110100110001",
    "1101101111000111011110111",
    "0100101011101101001010110",
    "0000000010110110110111010",
    "1111111011110101110001011",
    "1000001000111100010110110",
    "1011101011001011100011111",
    "1011101001101110110110101",
    "1011101011110010101000011",
    "1000001000101101101110110",
    "1111111011010101011010101"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiCardDonationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1: QR view, 3: Success, 4: Loading

    Dialog(
        onDismissRequest = { if (step != 4) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = step != 4
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = QiSlate)
                    }
                    Text(
                        text = "دعم وتطوير المنصة 💖",
                        color = QiDeepBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // Center alignment spacer
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "dialog_step"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> {
                            // Step 1: Displaying the QR Code Card
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "بوابة الدعم المباشر عبر المحفظة الإلكترونية",
                                    color = QiSlate,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // Yellow Super Qi Card representation
                                Box(
                                    modifier = Modifier
                                        .width(260.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(QiYellowBg)
                                        .border(1.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .padding(vertical = 16.dp, horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Qi Logo
                                        QiLogo(
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Text(
                                            text = "إستخدم سوبر كي",
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )

                                        // White container for QR Code and text
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Sharp vector QR Code
                                                QrCodeCanvas(
                                                    modifier = Modifier
                                                        .size(150.dp)
                                                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                )

                                                Text(
                                                    text = "محمد ثائر",
                                                    color = Color.Black,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                // Information / instruction card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "💡 طريقة الدفع والدعم:",
                                            color = QiDeepBlue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = "1. افتح تطبيق (Super Qi) أو محفظتك الإلكترونية.\n2. اختر خيار التحويل السريع عن طريق مسح الباركود.\n3. قم بمسح الرمز الموضح أعلاه لإتمام الدعم بأي مبلغ ترغب به.",
                                            color = QiSlate,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        step = 4 // Go to loading state
                                        scope.launch {
                                            delay(2000) // Simulate donation confirmation check
                                            viewModel.markAsSupporter()
                                            step = 3 // Go to success screen
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = QiDeepBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("لقد قمت بالتحويل بنجاح! 🎉", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        3 -> {
                            // Step 3: Success Screen (Certificate of appreciation)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "تمت العملية بنجاح",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }

                                Text(
                                    text = "تم التبرع بنجاح! 🎉",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )

                                Text(
                                    text = "شكراً جزيلاً لدعمك الكريم وسخائك! تبرعك ومساهمتك ستساعد مباشرة في استمرار المنصة وتطويرها وتقديم أفضل ملازم وكتب مجانية لكافة الطلاب العراقيين.",
                                    color = QiSlate,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = QiLightGold.copy(alpha = 0.15f)),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, QiGold),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "🌟 شهادة شكر وتقدير 🌟",
                                            color = QiDeepBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "تم منحك لقب [داعم متميز للتعليم] مدى الحياة وسيظهر الرمز الذهبي بجانب حسابك.",
                                            color = QiSlate,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = QiDeepBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("حفظ والعودة للتطبيق 💖", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        4 -> {
                            // Step 4: Loading Processing state
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = QiDeepBlue,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(54.dp)
                                )

                                Text(
                                    text = "جاري تأكيد عملية الدعم...",
                                    color = QiDeepBlue,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "يرجى الانتظار قليلاً لتسجيل مساهمتك الكريمة.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QiLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2.6f
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw outer black circle border
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.35f)
        )

        // Draw Q tail
        val tailWidth = radius * 0.35f
        val startX = centerX + radius * 0.2f
        val startY = centerY + radius * 0.2f
        val endX = centerX + radius * 1.0f
        val endY = centerY + radius * 1.0f

        drawLine(
            color = Color.Black,
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(endX, endY),
            strokeWidth = tailWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun QrCodeCanvas(
    modifier: Modifier = Modifier,
    grid: List<String> = qrCodeGrid,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(lightColor)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numRows = grid.size
            val numCols = grid[0].length
            val sizeX = size.width / numCols
            val sizeY = size.height / numRows

            for (r in 0 until numRows) {
                val rowString = grid[r]
                for (c in 0 until numCols) {
                    if (rowString[c] == '1') {
                        drawRect(
                            color = darkColor,
                            topLeft = androidx.compose.ui.geometry.Offset(c * sizeX, r * sizeY),
                            size = androidx.compose.ui.geometry.Size(sizeX + 0.5f, sizeY + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
