package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants matching the premium logo
val LogoNavy = Color(0xFF1A2E40)
val LogoGreen = Color(0xFF27AE60)
val LogoGold = Color(0xFFD4AF37)
val LogoRed = Color(0xFFC0392B)
val LogoBlack = Color(0xFF1E1E1E)
val LogoWhite = Color(0xFFFFFFFF)

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 120.dp,
    showSubtitle: Boolean = true
) {
    Card(
        modifier = modifier
            .size(logoSize)
            .shadow(4.dp, shape = RoundedCornerShape(percent = 22)),
        shape = RoundedCornerShape(percent = 22),
        colors = CardDefaults.cardColors(containerColor = LogoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = maxWidth
            val height = maxHeight

            // 1. Subtle Iraq Map watermark background (Light grey abstract silhouette)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapPath = Path().apply {
                    moveTo(size.width * 0.45f, size.height * 0.25f)
                    quadraticTo(size.width * 0.55f, size.height * 0.22f, size.width * 0.65f, size.height * 0.30f)
                    quadraticTo(size.width * 0.72f, size.height * 0.35f, size.width * 0.75f, size.height * 0.45f)
                    quadraticTo(size.width * 0.70f, size.height * 0.55f, size.width * 0.60f, size.height * 0.62f)
                    quadraticTo(size.width * 0.50f, size.height * 0.58f, size.width * 0.40f, size.height * 0.50f)
                    quadraticTo(size.width * 0.35f, size.height * 0.40f, size.width * 0.45f, size.height * 0.25f)
                    close()
                }
                drawPath(
                    path = mapPath,
                    color = Color(0xFFEEEEEE),
                    style = Fill
                )
            }

            // 2. Main closed Book spine (Left) & page boundary
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .fillMaxHeight(0.64f)
                    .align(Alignment.Center)
                    .offset(y = (-8).dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(Color(0xFFF8F9FA))
            ) {
                // Book left dark blue spine
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(width * 0.08f)
                        .background(LogoNavy)
                        .align(Alignment.CenterStart)
                )

                // Book contents (Text "ملازمي" and subtitle)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = (width * 0.1f), end = 4.dp, top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ملازمي",
                        color = LogoNavy,
                        fontSize = (logoSize.value * 0.15f).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                    
                    if (showSubtitle && logoSize >= 80.dp) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "خاص بالطالب العراقي",
                            color = LogoGreen,
                            fontSize = (logoSize.value * 0.055f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            lineHeight = (logoSize.value * 0.06f).sp
                        )
                    }
                }
            }

            // 3. Ribbon bookmark (Top-Left on the book)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.1f)
                    .fillMaxHeight(0.16f)
                    .align(Alignment.TopStart)
                    .offset(x = (width * 0.23f), y = (height * 0.18f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val ribbonPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height)
                        lineTo(size.width / 2f, size.height * 0.75f)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = ribbonPath,
                        color = LogoGreen,
                        style = Fill
                    )
                }
            }

            // 4. Colored tabs on the right of the textbook (representing Iraqi flag colors)
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.35f)
                    .width(width * 0.06f)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-width * 0.12f), y = (-height * 0.08f)),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                        .background(LogoRed)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                        .background(LogoBlack)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                        .background(LogoGreen)
                )
            }

            // 5. Open book at the bottom with elegant sweep curves
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.32f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-height * 0.06f))
            ) {
                val bookWidth = size.width
                val bookHeight = size.height

                // Draw background dark page fold
                val outerPath = Path().apply {
                    moveTo(bookWidth * 0.05f, bookHeight * 0.6f)
                    quadraticTo(bookWidth * 0.25f, bookHeight * 0.1f, bookWidth * 0.5f, bookHeight * 0.7f)
                    quadraticTo(bookWidth * 0.75f, bookHeight * 0.1f, bookWidth * 0.95f, bookHeight * 0.6f)
                    lineTo(bookWidth * 0.93f, bookHeight * 0.85f)
                    quadraticTo(bookWidth * 0.75f, bookHeight * 0.4f, bookWidth * 0.5f, bookHeight * 0.95f)
                    quadraticTo(bookWidth * 0.25f, bookHeight * 0.4f, bookWidth * 0.07f, bookHeight * 0.85f)
                    close()
                }

                // Draw actual pages inside
                val pagePath = Path().apply {
                    moveTo(bookWidth * 0.07f, bookHeight * 0.55f)
                    quadraticTo(bookWidth * 0.25f, bookHeight * 0.15f, bookWidth * 0.5f, bookHeight * 0.68f)
                    quadraticTo(bookWidth * 0.75f, bookHeight * 0.15f, bookWidth * 0.93f, bookHeight * 0.55f)
                    lineTo(bookWidth * 0.91f, bookHeight * 0.78f)
                    quadraticTo(bookWidth * 0.75f, bookHeight * 0.38f, bookWidth * 0.5f, bookHeight * 0.88f)
                    quadraticTo(bookWidth * 0.25f, bookHeight * 0.38f, bookWidth * 0.09f, bookHeight * 0.78f)
                    close()
                }

                drawPath(
                    path = outerPath,
                    color = LogoNavy,
                    style = Fill
                )
                
                drawPath(
                    path = pagePath,
                    color = LogoWhite,
                    style = Fill
                )

                // Page decorative stripes representing flag colors
                val flagPathLeft = Path().apply {
                    moveTo(bookWidth * 0.12f, bookHeight * 0.70f)
                    quadraticTo(bookWidth * 0.25f, bookHeight * 0.40f, bookWidth * 0.45f, bookHeight * 0.75f)
                }
                val flagPathRight = Path().apply {
                    moveTo(bookWidth * 0.55f, bookHeight * 0.75f)
                    quadraticTo(bookWidth * 0.75f, bookHeight * 0.40f, bookWidth * 0.88f, bookHeight * 0.70f)
                }

                drawPath(
                    path = flagPathLeft,
                    color = LogoGreen,
                    style = Stroke(width = bookHeight * 0.08f)
                )

                drawPath(
                    path = flagPathRight,
                    color = LogoGreen,
                    style = Stroke(width = bookHeight * 0.08f)
                )
            }

            // 6. Academic Graduation Cap in front-center
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .fillMaxHeight(0.32f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-height * 0.12f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val capWidth = size.width
                    val capHeight = size.height

                    // Under cap skull shape
                    val skullPath = Path().apply {
                        moveTo(capWidth * 0.32f, capHeight * 0.55f)
                        lineTo(capWidth * 0.32f, capHeight * 0.75f)
                        quadraticTo(capWidth * 0.5f, capHeight * 0.95f, capWidth * 0.68f, capHeight * 0.75f)
                        lineTo(capWidth * 0.68f, capHeight * 0.55f)
                        close()
                    }
                    drawPath(
                        path = skullPath,
                        color = LogoNavy,
                        style = Fill
                    )

                    // Top Diamond Mortarboard
                    val diamondPath = Path().apply {
                        moveTo(capWidth * 0.5f, capHeight * 0.12f) // Top
                        lineTo(capWidth * 0.95f, capHeight * 0.42f) // Right
                        lineTo(capWidth * 0.5f, capHeight * 0.72f) // Bottom
                        lineTo(capWidth * 0.05f, capHeight * 0.42f) // Left
                        close()
                    }
                    drawPath(
                        path = diamondPath,
                        color = LogoNavy,
                        style = Fill
                    )

                    // Subtle highlight on Diamond
                    val highlightPath = Path().apply {
                        moveTo(capWidth * 0.5f, capHeight * 0.16f)
                        lineTo(capWidth * 0.88f, capHeight * 0.42f)
                        lineTo(capWidth * 0.5f, capHeight * 0.66f)
                        lineTo(capWidth * 0.12f, capHeight * 0.42f)
                        close()
                    }
                    drawPath(
                        path = highlightPath,
                        color = Color.White.copy(alpha = 0.1f),
                        style = Fill
                    )

                    // Tassel Button (Center of diamond)
                    drawCircle(
                        color = LogoGold,
                        radius = capWidth * 0.035f,
                        center = Offset(capWidth * 0.5f, capHeight * 0.42f)
                    )

                    // Tassel cord
                    val tasselCord = Path().apply {
                        moveTo(capWidth * 0.5f, capHeight * 0.42f)
                        quadraticTo(capWidth * 0.62f, capHeight * 0.45f, capWidth * 0.68f, capHeight * 0.65f)
                    }
                    drawPath(
                        path = tasselCord,
                        color = LogoGold,
                        style = Stroke(width = capWidth * 0.02f)
                    )

                    // Tassel fringe hanging down
                    drawCircle(
                        color = LogoGold,
                        radius = capWidth * 0.05f,
                        center = Offset(capWidth * 0.68f, capHeight * 0.70f)
                    )
                }
            }
        }
    }
}

/**
 * A beautiful, horizontal row-based header logo bar that displays the custom app logo
 * alongside the application name in a clean, modern style.
 */
@Composable
fun AppLogoHeaderBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppLogo(
            logoSize = 46.dp,
            showSubtitle = false
        )
        Column {
            Text(
                text = "ملازمي",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "منصة الطالب العراقي المميزة",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
