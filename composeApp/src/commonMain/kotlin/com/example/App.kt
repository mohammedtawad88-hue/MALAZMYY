package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

val PrimaryColor = Color(0xFF1A4A7A)
val PrimaryLightColor = Color(0xFF2563A8)
val AccentColor = Color(0xFFE8A020)
val AccentLightColor = Color(0xFFF5C842)
val GreenColor = Color(0xFF1E7C4A)
val GreenLightColor = Color(0xFF27A85F)
val RedColor = Color(0xFFB82A2A)
val RedLightColor = Color(0xFFD94040)
val PurpleColor = Color(0xFF6B21A8)
val PurpleLightColor = Color(0xFF9333EA)
val QiDeepBlue = Color(0xFF0F2C59)
val BgColor = Color(0xFFF0F4FA)
val TextDark = Color(0xFF1A1A2E)
val TextSoft = Color(0xFF4A5568)
val BorderColor = Color(0xFFD1DDF0)

// Unique Class Colors (Task 1)
val ClassIb1Color = Color(0xFFE57373) // Sunset Coral
val ClassIb2Color = Color(0xFFF06292) // Blossom Pink
val ClassIb3Color = Color(0xFFBA68C8) // Orchid Amethyst
val ClassIb4Color = Color(0xFF7986CB) // Indigo Periwinkle
val ClassIb5Color = Color(0xFF4FC3F7) // Sky Blue
val ClassIb6Color = Color(0xFF4DB6AC) // Emerald Teal
val ClassMt1Color = Color(0xFF81C784) // Soft Green
val ClassMt2Color = Color(0xFFFFB74D) // Bright Amber
val ClassMt3Color = Color(0xFFFF8A65) // Peach Terracotta
val ClassId4sColor = Color(0xFF4DD0E1) // Bright Cyan
val ClassId5sColor = Color(0xFF009688) // Deep Mint Teal
val ClassId6sColor = Color(0xFF2196F3) // Bright Blue
val ClassId4aColor = Color(0xFFAB47BC) // Plum Purple
val ClassId5aColor = Color(0xFFC0CA33) // Citron Lime
val ClassId6aColor = Color(0xFFEC407A) // Ruby Crimson

fun getClassThemeColor(classId: String): Color {
    return when (classId) {
        "ib1" -> ClassIb1Color
        "ib2" -> ClassIb2Color
        "ib3" -> ClassIb3Color
        "ib4" -> ClassIb4Color
        "ib5" -> ClassIb5Color
        "ib6" -> ClassIb6Color
        "mt1" -> ClassMt1Color
        "mt2" -> ClassMt2Color
        "mt3" -> ClassMt3Color
        "id4s" -> ClassId4sColor
        "id5s" -> ClassId5sColor
        "id6s" -> ClassId6sColor
        "id4a" -> ClassId4aColor
        "id5a" -> ClassId5aColor
        "id6a" -> ClassId6aColor
        else -> PrimaryColor
    }
}

@Composable
fun App() {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val viewModel: MainViewModel = viewModel { MainViewModel() }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = BgColor
            ) { innerPadding ->
                MainAppScreen(modifier = Modifier.padding(innerPadding), viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel { MainViewModel() }
) {
    
    val currentTab by viewModel.currentTab.collectAsState()
    val currentStageId by viewModel.currentStageId.collectAsState()
    val currentBranchId by viewModel.currentBranchId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val isFirebaseMode by viewModel.isFirebaseMode.collectAsState()
    val educationStages by viewModel.educationStages.collectAsState()

    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedSubjectClassName by viewModel.selectedSubjectClassName.collectAsState()
    val subjectFiles by viewModel.subjectFiles.collectAsState()
    val savedFiles by viewModel.savedFiles.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()

    val favoriteFiles by viewModel.favoriteFiles.collectAsState()
    val ministerialFilesIb6 by viewModel.ministerialFilesIb6.collectAsState()
    val ministerialFilesMt3 by viewModel.ministerialFilesMt3.collectAsState()
    val ministerialFilesId6 by viewModel.ministerialFilesId6.collectAsState()
    val ministerialFilesId6Sci by viewModel.ministerialFilesId6Sci.collectAsState()
    val ministerialFilesId6Lit by viewModel.ministerialFilesId6Lit.collectAsState()

    // Dynamic Color Palette
    val currentBgColor = BgColor
    val currentCardBgColor = Color.White
    val currentTextDark = TextDark
    val currentTextSoft = TextSoft
    val currentBorderColor = BorderColor

    // Preview File Dialog states
    var previewFileUrl by remember { mutableStateOf<String?>(null) }
    var previewFileName by remember { mutableStateOf<String?>(null) }

    // File Delete Confirmation states
    var showFileDeleteConfirmDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<com.example.data.SubjectFileEntity?>(null) }

    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var showUserLoginDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val isAppSupporter by viewModel.isAppSupporter.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val focusManager = LocalFocusManager.current

    // Accordion expand states (classId -> Boolean)
    val expandedClasses = remember { mutableStateMapOf<String, Boolean>() }

    // Scroll states for lists
    val scrollState = rememberScrollState()

    // File picker launcher
    val filePickerLauncher = rememberPlatformFilePicker { fileUri, name, size ->
        viewModel.uploadPdf(fileUri, name, size)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── 1. GRADIENT HEADER ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryColor, PrimaryLightColor)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Logo & Admin button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppLogoHeaderBar()

                        // Header login action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Notification Bell Icon with Badge
                            val notificationsList by viewModel.notifications.collectAsState()
                            val unreadCount = notificationsList.count { !it.isRead }
                            var showNotificationsDialog by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { showNotificationsDialog = true },
                                contentAlignment = Alignment.Center
                             ) {
                                // Separate circular background with clip, so outer Box remains unclipped!
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                )
                                Text("🔔", fontSize = 18.sp)
                                if (unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 1.dp, y = (-1).dp)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(RedColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$unreadCount",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (showNotificationsDialog) {
                                androidx.compose.ui.window.Dialog(onDismissRequest = { 
                                    showNotificationsDialog = false 
                                    viewModel.markAllNotificationsAsRead()
                                }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.85f)
                                            .padding(vertical = 16.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = BgColor),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "📢 مركز التنبيهات والإعلانات",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                IconButton(
                                                    onClick = { 
                                                        showNotificationsDialog = false 
                                                        viewModel.markAllNotificationsAsRead()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        tint = TextSoft
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = BorderColor)

                                            if (notificationsList.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text("📢", fontSize = 48.sp)
                                                        Text("لا توجد إعلانات أو تنبيهات حالياً.", color = TextSoft, fontSize = 14.sp)
                                                    }
                                                }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    items(notificationsList) { notif ->
                                                        val colorScheme = when (notif.type) {
                                                            "urgent" -> Pair(RedColor, RedLightColor)
                                                            "new_materials" -> Pair(GreenColor, GreenLightColor)
                                                            "info" -> Pair(PurpleColor, PurpleColor.copy(alpha = 0.08f))
                                                            else -> Pair(PrimaryColor, PrimaryLightColor)
                                                        }
                                                        val badgeLabel = when (notif.type) {
                                                            "urgent" -> "🚨 عاجل جداً"
                                                            "new_materials" -> "📚 ملازم جديدة"
                                                            "info" -> "💡 تنويه"
                                                            else -> "🔔 عام"
                                                        }

                                                        val localUriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = CardDefaults.cardColors(containerColor = if (notif.isRead) Color.White else Color(0xFFF0F7FF)),
                                                            border = androidx.compose.foundation.BorderStroke(
                                                                width = 1.dp,
                                                                color = if (notif.isRead) BorderColor else PrimaryColor.copy(alpha = 0.3f)
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(14.dp),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(6.dp))
                                                                            .background(colorScheme.first.copy(alpha = 0.1f))
                                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = badgeLabel,
                                                                            color = colorScheme.first,
                                                                            fontSize = 10.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }

                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                    ) {
                                                                        if (!notif.isRead) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(8.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(PrimaryColor)
                                                                            )
                                                                        }
                                                                        Text(
                                                                            text = notif.date,
                                                                            color = TextSoft,
                                                                            fontSize = 11.sp
                                                                        )
                                                                    }
                                                                }

                                                                Text(
                                                                    text = notif.title,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = TextDark
                                                                )

                                                                Text(
                                                                    text = notif.content,
                                                                    fontSize = 13.sp,
                                                                    color = TextSoft,
                                                                    lineHeight = 18.sp
                                                                )

                                                                if (notif.targetUrl != null) {
                                                                    Button(
                                                                        onClick = {
                                                                            try {
                                                                                localUriHandler.openUri(notif.targetUrl)
                                                                            } catch (e: Exception) {
                                                                                showToast("لا يمكن فتح الرابط المرفق")
                                                                            }
                                                                        },
                                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                        modifier = Modifier.height(30.dp)
                                                                    ) {
                                                                        Text("🔗 عرض الرابط أو الملف المرفق", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    showNotificationsDialog = false
                                                    viewModel.markAllNotificationsAsRead()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().height(48.dp)
                                            ) {
                                                Text("إغلاق وتحديد الكل كمقروء ✔️", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            if (isAdmin) {
                                Button(
                                    onClick = {
                                        viewModel.logoutAdmin()
                                        showToast("👋 تم تسجيل الخروج للمشرف")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentColor.copy(alpha = 0.25f),
                                        contentColor = AccentLightColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        AccentLightColor
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("⚙️ خروج المشرف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (isUserLoggedIn) {
                                Button(
                                    onClick = {
                                        viewModel.logoutUser()
                                        showToast("👋 تم تسجيل خروج المستخدم")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        Color.White.copy(alpha = 0.3f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = if (isAppSupporter) "🌟 خروج (${currentUserName?.take(6) ?: "الداعم"})" else "👤 خروج (${currentUserName?.take(8) ?: "المستخدم"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // User login button
                                Button(
                                    onClick = { showUserLoginDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.2f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("👤 دخول المستخدم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ─── 2. SEARCH BOX ───
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = {
                                Text(
                                    "ابحث عن ملزمة، كتاب، مادة...",
                                    color = Color.Gray,
                                    fontSize = 15.sp
                                )
                            },
                            leadingIcon = {
                                Text("🔍", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = PrimaryColor)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("search_input")
                        )
                    }
                }
            }

            // ─── 3. ADMIN STRIP INDICATOR ───
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentColor, AccentLightColor)
                            )
                        )
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️ وضع المشرف — يمكنك رفع وحذف الملفات والملازم",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ─── 4. MAIN BODY (ACCORDING TO TABS) ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentTab) {
                    "home" -> {
                        // Home screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(bottom = 140.dp)
                        ) {
                            // Offline Fallback Alert if Firebase not available
                            if (!isFirebaseMode) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PrimaryColor.copy(alpha = 0.08f))
                                        .border(1.5.dp, PrimaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("⚠️", fontSize = 18.sp)
                                        Text(
                                            text = "وضع العمل المحلي (تجريبي) — لربط التطبيق بقاعدة البيانات Firebase، يرجى إضافة ملف google-services.json",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryColor
                                        )
                                    }
                                }
                            }

                            // Welcome Hero Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrimaryColor, PrimaryLightColor, Color(0xFF3A7BD5))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("أهلاً بك في", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                    Text("ملازمي — منصة العراق", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                    Text(
                                        text = "جميع ملازم وكتب المراحل الدراسية بأحدث المناهج المعتمدة للطلاب العراقيين.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        StatChip("🏫 3 مراحل")
                                        StatChip("📄 15 صف")
                                        StatChip("📦 علمي وأدبي")
                                    }
                                }
                            }
                            // ─── DONATION CARD (QI CARD 1000 IQD) ───
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                if (isAppSupporter) {
                                    // Golden Gratitude Card
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text("🌟", fontSize = 28.sp)
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "شريك دعم التعليم العراقي 💖",
                                                        color = TextDark,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Text(
                                                        text = "شكراً جزيلاً لدعمك السخي بقيمة 1,000 د.ع! تبرعك يساهم في إبقاء المنصة مجانية لجميع الطلاب العراقيين.",
                                                        color = TextSoft,
                                                        fontSize = 12.sp,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { showDonationDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = QiDeepBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("💳", fontSize = 16.sp)
                                                    Text(
                                                        text = "تبرع ودعم مجدداً بـ 1,000 د.ع عبر Qi Card",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Invitation to Donate Card
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🇮🇶", fontSize = 24.sp)
                                                Text(
                                                    text = "دعم التطبيق والتعليم المستمر",
                                                    color = TextDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(QiDeepBlue.copy(alpha = 0.1f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "اختياري 💝",
                                                        color = QiDeepBlue,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "ادعم استمرارية وتطوير تطبيق ملازمي لخدمة طلابنا مجاناً بقيمة رمزية تبلغ 1,000 دينار عراقي فقط، تدفع بلمسة واحدة عبر بطاقة الكي كارد (Qi Card) لمصرف الرافدين.",
                                                color = TextSoft,
                                                fontSize = 12.sp,
                                                lineHeight = 17.sp
                                            )

                                            Button(
                                                onClick = { showDonationDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = QiDeepBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("💳", fontSize = 16.sp)
                                                    Text(
                                                        text = "تبرع ودعم بـ 1,000 د.ع عبر Qi Card",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stage selection tabs (الابتدائية، المتوسطة، الإعدادية)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StageTabButton(
                                    title = "🌱 الابتدائية",
                                    isSelected = currentStageId == "ibtidaee",
                                    activeColor = GreenColor,
                                    activeColorLight = GreenLightColor,
                                    onClick = { viewModel.selectStage("ibtidaee") },
                                    modifier = Modifier.weight(1f)
                                )
                                StageTabButton(
                                    title = "📘 المتوسطة",
                                    isSelected = currentStageId == "mutawasit",
                                    activeColor = PrimaryColor,
                                    activeColorLight = PrimaryLightColor,
                                    onClick = { viewModel.selectStage("mutawasit") },
                                    modifier = Modifier.weight(1f)
                                )
                                StageTabButton(
                                    title = "🎓 الإعدادية",
                                    isSelected = currentStageId == "i3dadee",
                                    activeColor = RedColor,
                                    activeColorLight = RedLightColor,
                                    onClick = { viewModel.selectStage("i3dadee") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stage dynamic banner
                            val activeStage = educationStages.firstOrNull { it.id == currentStageId }
                            if (activeStage != null) {
                                val stageThemeColor = when (currentStageId) {
                                    "ibtidaee" -> GreenColor
                                    "mutawasit" -> PrimaryColor
                                    else -> RedColor
                                }
                                val stageThemeColorLight = when (currentStageId) {
                                    "ibtidaee" -> GreenLightColor
                                    "mutawasit" -> PrimaryLightColor
                                    else -> RedLightColor
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    stageThemeColor,
                                                    stageThemeColorLight,
                                                    stageThemeColorLight.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(activeStage.emoji, fontSize = 42.sp)
                                        Column {
                                            Text(
                                                "المرحلة ${activeStage.name}",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = when (currentStageId) {
                                                    "ibtidaee" -> "من الصف الأول إلى السادس الابتدائي"
                                                    "mutawasit" -> "من الصف الأول إلى الثالث المتوسط"
                                                    else -> "الفرع العلمي والفرع الأدبي — الرابع إلى السادس"
                                                },
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // If High school, show scientific / literary sub tabs
                                if (currentStageId == "i3dadee") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BranchTabButton(
                                            title = "🔬 الفرع العلمي",
                                            isSelected = currentBranchId == "ilmi",
                                            activeColor = PrimaryColor,
                                            onClick = { viewModel.selectBranch("ilmi") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        BranchTabButton(
                                            title = "📖 الفرع الأدبي",
                                            isSelected = currentBranchId == "adabi",
                                            activeColor = PurpleColor,
                                            onClick = { viewModel.selectBranch("adabi") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Render classes list for current stage/branch
                                val classesToRender = if (currentStageId == "i3dadee") {
                                    activeStage.branches[currentBranchId] ?: emptyList()
                                } else {
                                    activeStage.classes
                                }

                                classesToRender.forEach { cls ->
                                    val totalFilesCount = cls.subjects.sumOf { subj ->
                                        allFiles.count { file -> file.subjectId == subj.id }
                                    }
                                    ClassAccordionItem(
                                        cls = cls,
                                        isExpanded = expandedClasses[cls.id] ?: false,
                                        themeColor = getClassThemeColor(cls.id),
                                        totalFilesCount = totalFilesCount,
                                        allFilesList = allFiles,
                                        onHeaderClick = {
                                            expandedClasses[cls.id] = !(expandedClasses[cls.id] ?: false)
                                        },
                                        onSubjectClick = { subject ->
                                            viewModel.openSubjectModal(subject, cls.name)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "search" -> {
                        // Global search screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                "البحث الشامل في ملازمي",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val results = remember(searchQuery, allFiles, educationStages) {
                                getSearchSuggestions(searchQuery, allFiles, educationStages)
                            }

                            if (searchQuery.length < 2) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🔍", fontSize = 48.sp)
                                        Text(
                                            "يرجى كتابة حرفين أو أكثر للبحث...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSoft
                                        )
                                    }
                                }
                            } else if (results.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📭", fontSize = 48.sp)
                                        Text(
                                            "لا توجد نتائج مطابقة لبحثك",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSoft
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 140.dp)
                                ) {
                                    items(results) { item ->
                                        SearchItemRow(item = item, onClick = {
                                            viewModel.selectTab("home")
                                            viewModel.selectStage(item.stageKey)
                                            if (item.stageKey == "i3dadee" && item.branch != null) {
                                                viewModel.selectBranch(item.branch)
                                            }
                                            expandedClasses[item.classId] = true
                                            if (item.kind == "subject" && item.subject != null) {
                                                viewModel.openSubjectModal(item.subject, item.className)
                                            }
                                            viewModel.searchQuery.value = ""
                                        })
                                    }
                                }
                            }
                        }
                    }

                    "saved" -> {
                        // Ministerial Questions Screen (الأسئلة الوزارية)
                        var selectedClassTab by remember { mutableStateOf("ib6") } // "ib6", "mt3", "id6_sci"
                        var uploadToCommon by remember { mutableStateOf(false) }
                        var selectedId6Branch by remember { mutableStateOf("sci") } // "sci", "lit", "all"
                        
                        // Upload states for Admin
                        var showMinisterialUploadDialog by remember { mutableStateOf(false) }
                        var ministerialUploadFileNameInput by remember { mutableStateOf("") }
                        var selectedMinisterialUriForUpload by remember { mutableStateOf<String?>(null) }
                        var selectedMinisterialSizeStr by remember { mutableStateOf("") }
                        
                        val ministerialFilePickerLauncher = rememberPlatformFilePicker { fileUri, name, size ->
                            selectedMinisterialUriForUpload = fileUri
                            selectedMinisterialSizeStr = size
                            if (ministerialUploadFileNameInput.isEmpty()) {
                                ministerialUploadFileNameInput = name.replace(".pdf", "", ignoreCase = true)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Title & Subtitle
                            Column {
                                Text(
                                    "الأسئلة الوزارية",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextDark
                                )
                                Text(
                                    "تصفح الأسئلة الوزارية الرسمية والحلول النموذجية لكل مرحلة دراسية",
                                    fontSize = 13.sp,
                                    color = currentTextSoft
                                )
                            }

                            // Sub-tabs for the classes (السادس العلمي / الأدبي merged into السادس الإعدادي)
                            val classes = listOf(
                                Triple("ib6", "السادس الابتدائي", "🏆"),
                                Triple("mt3", "الثالث متوسط", "📙"),
                                Triple("id6_sci", "السادس الإعدادي", "🎓")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                classes.forEach { (classId, name, icon) ->
                                    val isSelected = selectedClassTab == classId
                                    val count = when (classId) {
                                        "ib6" -> ministerialFilesIb6.size
                                        "mt3" -> ministerialFilesMt3.size
                                        "id6_sci" -> (ministerialFilesId6Sci + ministerialFilesId6Lit + ministerialFilesId6).distinctBy { it.id }.size
                                        else -> 0
                                    }
                                    
                                    // Set unique colors for each class using our helper
                                    val classColor = when (classId) {
                                        "ib6" -> ClassIb6Color
                                        "mt3" -> ClassMt3Color
                                        "id6_sci" -> ClassId6sColor
                                        else -> PrimaryColor
                                    }
                                    
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { 
                                                selectedClassTab = classId
                                                // Reset upload common flag
                                                uploadToCommon = false
                                            }
                                            .height(72.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) classColor else classColor.copy(alpha = 0.04f)
                                        ),
                                        border = borderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) classColor else classColor.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                             verticalAlignment = Alignment.CenterVertically,
                                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Icon and Badge in a Box
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else classColor.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(icon, fontSize = 16.sp)
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = name,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) Color.White else currentTextDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "$count ملفات",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else currentTextSoft
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // If Admin is logged in, show an Admin Upload Button
                            if (isAdmin) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.05f)),
                                    border = borderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "لوحة إدارة الأسئلة الوزارية",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PrimaryColor
                                            )
                                            Text(
                                                text = "بإمكانك رفع ملف PDF جديد للقسم المحدد حالياً",
                                                fontSize = 11.sp,
                                                color = currentTextSoft
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                selectedMinisterialUriForUpload = null
                                                ministerialUploadFileNameInput = ""
                                                selectedMinisterialSizeStr = ""
                                                showMinisterialUploadDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("➕ رفع ملف", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Files to show based on selection
                            val filesToShow = when (selectedClassTab) {
                                "ib6" -> ministerialFilesIb6
                                "mt3" -> ministerialFilesMt3
                                "id6_sci" -> (ministerialFilesId6Sci + ministerialFilesId6Lit + ministerialFilesId6).distinctBy { it.id }
                                else -> emptyList()
                            }

                            if (filesToShow.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📝", fontSize = 44.sp)
                                        Text(
                                            "لا توجد أسئلة وزارية بعد",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTextSoft
                                        )
                                        Text(
                                            "اضغط على زر الإضافة لرفع أول ملف أسئلة أو حل نموذجي.",
                                            fontSize = 13.sp,
                                            color = currentTextSoft,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 140.dp)
                                ) {
                                    items(filesToShow) { file ->
                                        FileItemRow(
                                            file = file,
                                            isAdmin = isAdmin,
                                            cardBgColor = currentCardBgColor,
                                            borderColor = currentBorderColor,
                                            textDark = currentTextDark,
                                            textSoft = currentTextSoft,
                                            onPreview = {
                                                previewFileName = file.name
                                                previewFileUrl = file.downloadUrl
                                            },
                                            onDownload = {
                                                PlatformAdManager.showAdWithAction {
                                                    viewModel.downloadRemoteFile(file)
                                                }
                                            },
                                            onPrint = {
                                                viewModel.shareLocalFile(file)
                                            },
                                            onFavoriteToggle = {
                                                viewModel.toggleFavorite(file)
                                            },
                                            onDelete = {
                                                fileToDelete = file
                                                showFileDeleteConfirmDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Admin Dialog for Uploading Ministerial Files
                        if (showMinisterialUploadDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    if (uploadProgress == null) {
                                        showMinisterialUploadDialog = false
                                    }
                                },
                                title = {
                                    Text(
                                        text = "رفع أسئلة وزارية جديدة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val classLabel = when (selectedClassTab) {
                                            "ib6" -> "السادس الابتدائي"
                                            "mt3" -> "الثالث متوسط"
                                            "id6_sci" -> if (uploadToCommon) "السادس الاعدادي (مشترك/عام)" else "السادس الإعدادي"
                                            else -> ""
                                        }
                                        Text(
                                            text = "الصف الدراسي المستهدف: $classLabel",
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryColor,
                                            fontSize = 14.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Right
                                        )

                                        if (selectedClassTab.startsWith("id6")) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { uploadToCommon = !uploadToCommon }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text(
                                                    "ملف مشترك (علمي وأدبي معاً مثل الإسلامية/العربي)",
                                                    fontSize = 11.sp,
                                                    color = currentTextSoft,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Checkbox(
                                                    checked = uploadToCommon,
                                                    onCheckedChange = { uploadToCommon = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                                                )
                                            }
                                        }

                                        if (uploadProgress != null) {
                                            // Show Upload Progress
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = { uploadProgress!!.toFloat() / 100f },
                                                    color = PrimaryColor,
                                                    trackColor = ProgressIndicatorDefaults.circularTrackColor,
                                                )
                                                Text(
                                                    text = "جاري الرفع... ${uploadProgress}%",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryColor
                                                )
                                            }
                                        } else {
                                            // Select and Name controls
                                            Button(
                                                onClick = { ministerialFilePickerLauncher() },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (selectedMinisterialUriForUpload != null) "📎 تغيير ملف الـ PDF المختار" else "📁 اختر ملف الـ PDF من جهازك",
                                                    color = Color.White
                                                )
                                            }

                                            if (selectedMinisterialUriForUpload != null) {
                                                Text(
                                                    text = "حجم المستند: $selectedMinisterialSizeStr",
                                                    fontSize = 12.sp,
                                                    color = currentTextSoft,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Right
                                                )
                                                
                                                OutlinedTextField(
                                                    value = ministerialUploadFileNameInput,
                                                    onValueChange = { ministerialUploadFileNameInput = it },
                                                    label = { Text("اسم الملف (مثال: رياضيات 2024 دور أول)") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    if (uploadProgress == null) {
                                        Button(
                                            onClick = {
                                                val uri = selectedMinisterialUriForUpload
                                                if (uri != null && ministerialUploadFileNameInput.isNotBlank()) {
                                                    val uploadClassId = if (uploadToCommon) {
                                                        "id6"
                                                    } else {
                                                        selectedClassTab
                                                    }
                                                    viewModel.uploadMinisterialPdf(
                                                        classId = uploadClassId,
                                                        fileUri = uri,
                                                        name = "${ministerialUploadFileNameInput.trim().replace(".pdf", "")}.pdf",
                                                        sizeStr = selectedMinisterialSizeStr
                                                    )
                                                    showMinisterialUploadDialog = false
                                                }
                                            },
                                            enabled = selectedMinisterialUriForUpload != null && ministerialUploadFileNameInput.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                        ) {
                                            Text("بدء رفع المستند", color = Color.White)
                                        }
                                    }
                                },
                                dismissButton = {
                                    if (uploadProgress == null) {
                                        TextButton(onClick = { showMinisterialUploadDialog = false }) {
                                            Text("إلغاء", color = currentTextSoft)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    "results" -> {
                        ResultsTabScreen(
                            viewModel = viewModel,
                            onPreviewFile = { name, url ->
                                previewFileName = name
                                previewFileUrl = url
                            },
                            onDeleteFile = { file ->
                                fileToDelete = file
                                showFileDeleteConfirmDialog = true
                            }
                        )
                    }
                    "admin" -> {
                        AdminDashboardScreen(
                            viewModel = viewModel,
                            onTriggerLogin = { showAdminLoginDialog = true }
                        )
                    }
                }

                // Suggestion Search Dropdown in Home Tab
                val showSuggestions = currentTab == "home" && searchQuery.length >= 2
                val suggestionsList = remember(searchQuery, allFiles, educationStages) {
                    if (showSuggestions) getSearchSuggestions(searchQuery, allFiles, educationStages).take(6) else emptyList()
                }

                if (showSuggestions && suggestionsList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.5.dp, BorderColor, RoundedCornerShape(12.dp))
                            .align(Alignment.TopCenter)
                            .zIndex(150f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            items(suggestionsList) { item ->
                                SearchDropdownItem(item = item, onClick = {
                                    viewModel.selectStage(item.stageKey)
                                    if (item.stageKey == "i3dadee" && item.branch != null) {
                                        viewModel.selectBranch(item.branch)
                                    }
                                    expandedClasses[item.classId] = true
                                    if (item.kind == "subject" && item.subject != null) {
                                        viewModel.openSubjectModal(item.subject, item.className)
                                    }
                                    viewModel.searchQuery.value = ""
                                    focusManager.clearFocus()
                                })
                            }
                        }
                    }
                }
            }
        }

        // ─── 5. FLOATING SUBJECT FILES MODAL ───
        selectedSubject?.let { subject ->
            Dialog(onDismissRequest = { viewModel.closeSubjectModal() }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(20.dp)
                        .testTag("subject_files_modal")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Modal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ملفات المادة الدراسية",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                            IconButton(
                                onClick = { viewModel.closeSubjectModal() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(BgColor, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp), tint = TextSoft)
                            }
                        }

                        // Subject Badge Info Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgColor)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(subject.icon, fontSize = 36.sp)
                            Column {
                                Text(
                                    subject.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    selectedSubjectClassName,
                                    fontSize = 13.sp,
                                    color = TextSoft
                                )
                            }
                        }

                        // Status Alerts
                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentColor.copy(alpha = 0.15f))
                                    .border(1.5.dp, AccentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("⚙️", fontSize = 16.sp)
                                    Text(
                                        "وضع المشرف — يمكنك رفع وحذف الملفات للطلاب",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentColor
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryColor.copy(alpha = 0.07f))
                                    .border(1.5.dp, PrimaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("📥", fontSize = 16.sp)
                                    Text(
                                        "يمكنك تحميل الملفات المتاحة أدناه — رفع الملفات للمشرف فقط",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryColor
                                    )
                                }
                            }
                        }

                        // Beautiful Separated Categorization for textbooks (الكتب) & study guides (الملازم)
                        val books = remember(subjectFiles) { subjectFiles.filter { it.name.contains("كتاب") } }
                        val notes = remember(subjectFiles) { subjectFiles.filter { !it.name.contains("كتاب") } }
                        
                        var modalFilterType by remember(subjectFiles) {
                            mutableStateOf(if (books.isNotEmpty()) "book" else "notes")
                        }
                        
                        val filteredModalFiles = remember(subjectFiles, modalFilterType) {
                            if (modalFilterType == "book") books else notes
                        }

                        // Modern Segmented Tab Switcher - Books on the right (first child in RTL), Notes on the left (second child in RTL)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgColor)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Tab 1: Books (الكتب الدراسية) - Colored Green
                            val isBooksSelected = modalFilterType == "book"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isBooksSelected) GreenColor.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { modalFilterType = "book" }
                                    .then(
                                        if (isBooksSelected) {
                                            Modifier.border(1.dp, GreenColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        } else Modifier
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📚", fontSize = 16.sp)
                                    Text(
                                        text = "الكتب الدراسية",
                                        fontSize = 12.sp,
                                        fontWeight = if (isBooksSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isBooksSelected) GreenColor else TextSoft
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isBooksSelected) GreenColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${books.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBooksSelected) GreenColor else TextSoft
                                        )
                                    }
                                }
                            }

                            // Tab 2: Notes (الملازم والملخصات) - Colored Amber
                            val isNotesSelected = modalFilterType == "notes"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNotesSelected) AccentColor.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { modalFilterType = "notes" }
                                    .then(
                                        if (isNotesSelected) {
                                            Modifier.border(1.dp, AccentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        } else Modifier
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📝", fontSize = 16.sp)
                                    Text(
                                        text = "الملازم والملخصات",
                                        fontSize = 12.sp,
                                        fontWeight = if (isNotesSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isNotesSelected) AccentColor else TextSoft
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isNotesSelected) AccentColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${notes.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNotesSelected) AccentColor else TextSoft
                                        )
                                    }
                                }
                            }
                        }

                        // Subtitle Header with custom color separation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeColor = if (modalFilterType == "book") GreenColor else AccentColor
                            Text(
                                text = if (modalFilterType == "book") "📚 الكتب المنهجية الرسمية المتاحة" else "📝 الملازم والشروحات المتاحة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                            Text(
                                text = "عدد الملفات: ${filteredModalFiles.size}",
                                fontSize = 12.sp,
                                color = activeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            if (filteredModalFiles.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(if (modalFilterType == "book") "📚" else "📝", fontSize = 36.sp)
                                    Text(
                                        text = if (modalFilterType == "book") "لا توجد كتب منهجية مرفوعة حالياً" else "لا توجد ملازم أو ملخصات مرفوعة حالياً",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentTextSoft,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredModalFiles) { file ->
                                        FileItemRow(
                                            file = file,
                                            isAdmin = isAdmin,
                                            cardBgColor = BgColor,
                                            borderColor = currentBorderColor,
                                            textDark = currentTextDark,
                                            textSoft = currentTextSoft,
                                            onPreview = {
                                                previewFileName = file.name
                                                previewFileUrl = file.downloadUrl
                                            },
                                            onDownload = {
                                                PlatformAdManager.showAdWithAction {
                                                    viewModel.downloadRemoteFile(file)
                                                }
                                            },
                                            onPrint = {
                                                viewModel.shareLocalFile(file)
                                            },
                                            onFavoriteToggle = {
                                                viewModel.toggleFavorite(file)
                                            },
                                            onDelete = {
                                                fileToDelete = file
                                                showFileDeleteConfirmDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Upload progress loader
                        uploadProgress?.let { progress ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GreenColor.copy(alpha = 0.07f))
                                    .border(1.5.dp, GreenColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("جاري الرفع... $progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenColor)
                                }
                                LinearProgressIndicator(
                                    progress = progress / 100f,
                                    color = GreenColor,
                                    trackColor = GreenColor.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        // Upload trigger button (only for admin)
                        if (isAdmin && uploadProgress == null) {
                            Button(
                                onClick = {
                                    // Trigger file selection
                                    filePickerLauncher()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("upload_file_btn")
                            ) {
                                Text("⬆️ رفع ملزمة / كتاب جديد (PDF/صور)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ─── 5.5. PDF PREVIEW DIALOG MODAL ───
        if (previewFileUrl != null && previewFileName != null) {
            PdfPreviewDialog(
                fileName = previewFileName!!,
                url = previewFileUrl!!,
                cardBgColor = currentCardBgColor,
                textDark = currentTextDark,
                textSoft = currentTextSoft,
                onDismissRequest = {
                    previewFileUrl = null
                    previewFileName = null
                },
                onDownload = {
                    PlatformAdManager.showAdWithAction {
                        viewModel.downloadRemoteFile(SubjectFileEntity(id = "", subjectId = "", name = previewFileName!!, size = "", date = "", downloadUrl = previewFileUrl!!))
                    }
                }
            )
        }

        // ─── FILE DELETION CONFIRMATION DIALOG ───
        if (showFileDeleteConfirmDialog && fileToDelete != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showFileDeleteConfirmDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗑️", fontSize = 28.sp)
                        }

                        Text(
                            text = "تأكيد حذف الملف",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = "هل أنت متأكد من رغبتك في حذف الملف:\n«${fileToDelete?.name}»؟",
                            fontSize = 14.sp,
                            color = currentTextDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "⚠️ تنبيه: سيتم حذف هذا الملف نهائياً من قاعدة البيانات وجهاز المستخدمين ولا يمكن التراجع عن هذا الإجراء.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    fileToDelete?.let { file ->
                                        viewModel.deleteFile(file)
                                    }
                                    showFileDeleteConfirmDialog = false
                                    fileToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("نعم، احذف", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    showFileDeleteConfirmDialog = false
                                    fileToDelete = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Text("إلغاء", color = TextSoft, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // ─── 6. LOGIN DIALOG MODALS ───
        if (showAdminLoginDialog) {
            AdminLoginDialog(
                viewModel = viewModel,
                onDismiss = { showAdminLoginDialog = false },
                onSuccess = {
                    showAdminLoginDialog = false
                    viewModel.selectTab("admin")
                    showToast("✅ مرحباً بك يا مشرف المنصة المعتمد!")
                }
            )
        }

        if (showUserLoginDialog) {
            EmailSignInDialog(
                viewModel = viewModel,
                onDismiss = { showUserLoginDialog = false },
                onSuccess = {
                    showUserLoginDialog = false
                    showToast("✅ تم تسجيل الدخول بنجاح كعضو مميز!")
                }
            )
        }

        if (showDonationDialog) {
            QiCardDonationDialog(
                viewModel = viewModel,
                onDismiss = { showDonationDialog = false }
            )
        }

        // ─── 7. STICKY BOTTOM AD & NAVIGATION BAR ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            AdmobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentBgColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentCardBgColor)
                    .border(width = 1.dp, color = currentBorderColor)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomNavItem(
                        title = "الرئيسية",
                        iconStr = "🏠",
                        isActive = currentTab == "home",
                        onClick = { viewModel.selectTab("home") }
                    )
                    BottomNavItem(
                        title = "البحث",
                        iconStr = "🔍",
                        isActive = currentTab == "search",
                        onClick = { viewModel.selectTab("search") }
                    )
                    BottomNavItem(
                        title = "النتائج",
                        iconStr = "🏆",
                        isActive = currentTab == "results",
                        onClick = { viewModel.selectTab("results") }
                    )
                    BottomNavItem(
                        title = "الوزاريات",
                        iconStr = "📝",
                        isActive = currentTab == "saved",
                        onClick = { viewModel.selectTab("saved") }
                    )
                    BottomNavItem(
                        title = "المشرف",
                        iconStr = "🔐",
                        isActive = currentTab == "admin",
                        onClick = {
                            if (isAdmin) {
                                viewModel.selectTab("admin")
                            } else {
                                showAdminLoginDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─── HELPER DESIGN COMPONENTS ───

@Composable
fun StatChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StageTabButton(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    activeColorLight: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(colors = listOf(activeColor, activeColorLight))
                } else {
                    Brush.linearGradient(colors = listOf(Color.White, Color.White))
                }
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent else BorderColor,
                RoundedCornerShape(30.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextSoft,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BranchTabButton(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(colors = listOf(activeColor, activeColor.copy(alpha = 0.8f)))
                } else {
                    Brush.linearGradient(colors = listOf(Color.White, Color.White))
                }
            )
            .border(
                2.dp,
                if (isSelected) activeColor else BorderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextSoft,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ClassAccordionItem(
    cls: StudyClass,
    isExpanded: Boolean,
    themeColor: Color,
    totalFilesCount: Int,
    allFilesList: List<SubjectFileEntity>,
    onHeaderClick: () -> Unit,
    onSubjectClick: (Subject) -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .animateContentSize()
    ) {
        // Card Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHeaderClick() }
                .drawRightBorder(4.dp, themeColor)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Badge with soft bg
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cls.emoji, fontSize = 22.sp)
                }

                Column {
                    Text(
                        cls.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "${cls.subjects.size} مواد · $totalFilesCount ملفات دراسية",
                        fontSize = 13.sp,
                        color = TextSoft
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand",
                modifier = Modifier
                    .rotate(rotation)
                    .size(24.dp),
                tint = TextSoft
            )
        }

        // Expanded Subjects Grid List
        if (isExpanded) {
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // We chunks the list into rows of 2 subjects
                val rows = cls.subjects.chunked(2)
                rows.forEach { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowSubjects.forEach { subject ->
                            val subjectFileCount = allFilesList.count { it.subjectId == subject.id }
                            SubjectCard(
                                subject = subject,
                                filesCount = subjectFileCount,
                                onClick = { onSubjectClick(subject) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number of subjects in last row, pad with empty spacer
                        if (rowSubjects.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    filesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(subject.icon, fontSize = 28.sp)
        Text(
            subject.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Type Pill Book vs Notes
        val badgeBg = if (subject.type == "book") PrimaryColor.copy(alpha = 0.1f) else AccentColor.copy(alpha = 0.15f)
        val badgeText = if (subject.type == "book") PrimaryColor else Color(0xFFB07A10)
        val typeLabel = if (subject.type == "book") "📚 كتاب" else "📋 ملازم"

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(typeLabel, color = badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "$filesCount ملفات",
            fontSize = 13.sp,
            color = TextSoft
        )
    }
}

@Composable
fun FileItemRow(
    file: SubjectFileEntity,
    isAdmin: Boolean,
    cardBgColor: Color,
    borderColor: Color,
    textDark: Color,
    textSoft: Color,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onPrint: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBgColor)
            .border(1.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
    ) {
        // Top clickable area for previewing the file (Reduced padding and icon size)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPreview() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isPdf = file.name.endsWith(".pdf", ignoreCase = true)
            Text(
                text = if (isPdf) "📄" else "🖼️",
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark,
                    lineHeight = 18.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(textSoft.copy(alpha = 0.08f))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = file.size,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSoft
                        )
                    }
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = textSoft
                    )
                    Text(
                        text = "تاريخ الإضافة: ${file.date}",
                        fontSize = 10.sp,
                        color = textSoft
                    )
                }
            }
        }
        
        // Divider
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = borderColor.copy(alpha = 0.4f)
        )
        
        // Bottom Action Bar (Compact vertical spacing and smaller buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left actions: Favorite & Delete (Slightly smaller icon buttons)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(if (file.isFavorite) "❤️" else "🤍", fontSize = 16.sp)
                }
                
                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("🗑️", fontSize = 15.sp)
                    }
                }
            }
            
            // Right actions: Download & Print (Ultra-clean compact buttons)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⬇️", fontSize = 12.sp)
                        Text("تحميل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🖨️", fontSize = 12.sp)
                        Text("طباعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SavedFileItemRow(
    file: SubjectFileEntity,
    cardBgColor: Color,
    borderColor: Color,
    textDark: Color,
    textSoft: Color,
    onPreview: () -> Unit,
    onDownloadClick: () -> Unit,
    onPrintClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        // Top clickable area for previewing the file
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPreview() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isPdf = file.name.endsWith(".pdf", ignoreCase = true)
            Text(
                text = if (isPdf) "📄" else "🖼️",
                fontSize = 32.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = file.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark,
                    lineHeight = 22.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(textSoft.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "الحجم: ${file.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSoft
                        )
                    }
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = textSoft
                    )
                    Text(
                        text = "تم الحفظ: ${file.date}",
                        fontSize = 11.sp,
                        color = textSoft
                    )
                }
            }
        }
        
        // Divider
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = borderColor.copy(alpha = 0.5f)
        )
        
        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left actions: Favorite
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Text(if (file.isFavorite) "❤️" else "🤍", fontSize = 20.sp)
            }
            
            // Right actions: Open & Share
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownloadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⬇️", fontSize = 14.sp)
                        Text("فتح", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = onPrintClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🖨️", fontSize = 14.sp)
                        Text("مشاركة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItemRow(
    item: SearchEntry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(item.icon, fontSize = 24.sp)
        }

        Column {
            Text(
                item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                item.subtitle,
                fontSize = 13.sp,
                color = TextSoft
            )
        }
    }
}

@Composable
fun SearchDropdownItem(
    item: SearchEntry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(item.icon, fontSize = 22.sp)
        Column {
            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(item.subtitle, fontSize = 13.sp, color = TextSoft)
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    iconStr: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(iconStr, fontSize = 24.sp)
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) PrimaryColor else TextSoft
        )
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        PlatformBannerAd(modifier = Modifier.fillMaxSize())
    }
}



// ─── UTILS & EXTENSIONS ───

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

fun Modifier.drawRightBorder(width: androidx.compose.ui.unit.Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidthPx = width.toPx()
    // Since we are in RTL, "right" border is actually visually on the right, which is standard x = size.width - strokeWidth/2
    // But logically, to make it act as a clean start border, we can draw a line on the right edge (x = size.width) or left edge.
    // In our Arabic list header, let's draw it on the rightmost edge to match border-right!
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = strokeWidthPx
    )
}

// Search suggest models
data class SearchEntry(
    val kind: String, // "class" or "subject"
    val stageKey: String,
    val branch: String?,
    val classId: String,
    val className: String,
    val subject: Subject?,
    val title: String,
    val icon: String,
    val subtitle: String
)

fun getSearchSuggestions(query: String, allFiles: List<SubjectFileEntity>, educationStages: List<Stage>): List<SearchEntry> {
    val normalizedQuery = normalizeSearchText(query)
    if (normalizedQuery.length < 2) return emptyList()

    val entries = mutableListOf<SearchEntry>()
    
    // Aggregate structural search suggestions
    educationStages.forEach { stage ->
        // Add classes
        val classes = if (stage.id == "i3dadee") {
            (stage.branches["ilmi"] ?: emptyList()) + (stage.branches["adabi"] ?: emptyList())
        } else {
            stage.classes
        }

        classes.forEach { cls ->
            val branch = if (stage.id == "i3dadee") {
                if (cls.id.endsWith("s")) "ilmi" else "adabi"
            } else null
            
            // Add class suggestion
            entries.add(
                SearchEntry(
                    kind = "class",
                    stageKey = stage.id,
                    branch = branch,
                    classId = cls.id,
                    className = cls.name,
                    subject = null,
                    title = cls.name,
                    icon = cls.emoji,
                    subtitle = "صف دراسي"
                )
            )

            // Add subject suggestions
            cls.subjects.forEach { subject ->
                entries.add(
                    SearchEntry(
                        kind = "subject",
                        stageKey = stage.id,
                        branch = branch,
                        classId = cls.id,
                        className = cls.name,
                        subject = subject,
                        title = subject.name,
                        icon = subject.icon,
                        subtitle = cls.name
                    )
                )
            }
        }
    }

    return entries.filter { item ->
        normalizeSearchText(item.title).contains(normalizedQuery) ||
                normalizeSearchText(item.subtitle).contains(normalizedQuery)
    }
}

fun normalizeSearchText(text: String): String {
    return text.lowercase()
        .replace("[ًٌٍَُِّْـ]".toRegex(), "") // Arabic diacritics
        .replace("[أإآ]".toRegex(), "ا")      // Alef normalization
        .replace("ى", "ي")
        .replace("ة", "ه")
        .trim()
}

// Resolver for display name and size of chosen Uri


// Standard file downloader helper


// Printer / share fallback helper


@Composable
fun CustomFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PrimaryColor else Color.Transparent)
            .border(1.dp, if (selected) PrimaryColor else borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextSoft
        )
    }
}

@Composable
fun PdfPreviewDialog(
    fileName: String,
    url: String,
    cardBgColor: Color,
    textDark: Color,
    textSoft: Color,
    onDismissRequest: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(cardBgColor)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Actions Row (Close and Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .background(BgColor, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Close", 
                            modifier = Modifier.size(18.dp), 
                            tint = textSoft
                        )
                    }

                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⬇️", fontSize = 14.sp)
                            Text("تحميل الملف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Dedicated Title Section for the Book Name
                Text(
                    text = fileName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = textDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 23.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Start
                )

                HorizontalDivider(color = BorderColor, thickness = 1.dp)

                // Conditional display: Image Viewer (with pinch-to-zoom) vs WebView PDF Viewer
                val isImage = fileName.endsWith(".jpg", ignoreCase = true) ||
                        fileName.endsWith(".jpeg", ignoreCase = true) ||
                        fileName.endsWith(".png", ignoreCase = true) ||
                        fileName.endsWith(".webp", ignoreCase = true) ||
                        url.contains(".jpg", ignoreCase = true) ||
                        url.contains(".jpeg", ignoreCase = true) ||
                        url.contains(".png", ignoreCase = true) ||
                        url.contains(".webp", ignoreCase = true)

                if (isImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            offset += offsetChange * scale
                        }

                        // Reset offsets when scale is reset to 1
                        if (scale == 1f) {
                            offset = Offset.Zero
                        }

                        PlatformAsyncImage(
                            model = url,
                            contentDescription = fileName,
                            modifier = Modifier
                                .fillMaxSize()
                                .transformable(state = state)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                        
                        // Pinch to zoom guide
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💡 قرّب بإصبعيك للتكبير والتصغير والتحريك",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) { PlatformPdfWebView(url = url, modifier = Modifier.fillMaxSize()) }
                }

                Text(
                    text = if (isImage) "يتم عرض الصورة بجودة عالية. يمكنك استخدام حركات التكبير." else "تنبيه: يتم تحميل المعاينة عبر الإنترنت. إذا لم تظهر، يمكنك تحميل الملف وقراءته محلياً.",
                    fontSize = 12.sp,
                    color = textSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onTriggerLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    
    val isAdmin by viewModel.isAdmin.collectAsState()
    val educationStages by viewModel.educationStages.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()

    var activeSubTab by remember { mutableStateOf("classes") } // "classes", "subjects", "notifications"

    // Form state for publishing notifications
    var notificationTitle by remember { mutableStateOf("") }
    var notificationContent by remember { mutableStateOf("") }
    var notificationType by remember { mutableStateOf("general") } // "general", "urgent", "new_materials", "info"
    var notificationTargetUrl by remember { mutableStateOf("") }

    // Form Dialog states for Classes
    var showClassDialog by remember { mutableStateOf(false) }
    var selectedStageIdForClass by remember { mutableStateOf("ibtidaee") }
    var selectedBranchIdForClass by remember { mutableStateOf<String?>("ilmi") }
    var editingClassId by remember { mutableStateOf<String?>(null) } // null means add
    var classNameInput by remember { mutableStateOf("") }
    var classEmojiInput by remember { mutableStateOf("") }

    // Form Dialog states for Subjects
    var showSubjectDialog by remember { mutableStateOf(false) }
    var selectedStageIdForSubject by remember { mutableStateOf("ibtidaee") }
    var selectedBranchIdForSubject by remember { mutableStateOf<String?>("ilmi") }
    var selectedClassIdForSubject by remember { mutableStateOf("") }
    var editingSubjectId by remember { mutableStateOf<String?>(null) } // null means add
    var subjectNameInput by remember { mutableStateOf("") }
    var subjectIconInput by remember { mutableStateOf("") }
    var subjectTypeInput by remember { mutableStateOf("book") } // "book" or "notes"

    // Active selectors for the lists
    var listSelectedStageId by remember { mutableStateOf("ibtidaee") }
    var listSelectedBranchId by remember { mutableStateOf("ilmi") }
    var listSelectedClassId by remember { mutableStateOf("") }

    // Delete Confirmation Dialog states
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteConfirmType by remember { mutableStateOf("") } // "class" or "subject"
    var deleteConfirmClassId by remember { mutableStateOf("") }
    var deleteConfirmClassName by remember { mutableStateOf("") }
    var deleteConfirmSubjectId by remember { mutableStateOf("") }
    var deleteConfirmSubjectName by remember { mutableStateOf("") }

    // Restore Confirmation Dialog states
    var showRestoreDefaultConfirm by remember { mutableStateOf(false) }
    var showRestorePointConfirm by remember { mutableStateOf(false) }

    // On startup or change, choose first class of selected stage to populate subject manager list
    LaunchedEffect(listSelectedStageId, listSelectedBranchId, educationStages) {
        val stage = educationStages.firstOrNull { it.id == listSelectedStageId }
        if (stage != null) {
            val classes = if (listSelectedStageId == "i3dadee") {
                stage.branches[listSelectedBranchId] ?: emptyList()
            } else {
                stage.classes
            }
            if (classes.isNotEmpty() && !classes.any { it.id == listSelectedClassId }) {
                listSelectedClassId = classes.first().id
            }
        }
    }

    if (!isAdmin) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("🔐", fontSize = 64.sp)
                    
                    Text(
                        text = "بوابة الإشراف الآمنة",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E40), // ResortNavy
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "هذا القسم مخصص لمشرف المنصة المعتمد فقط. يرجى تسجيل الدخول باستخدام البريد الإلكتروني المعتمد لتلقي رمز الدخول المؤقت OTP والمتابعة لإدارة الملازم والملفات.",
                        fontSize = 14.sp,
                        color = Color(0xFF4A607A), // SlateBlue
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onTriggerLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2E40)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "تسجيل الدخول كمسؤول ✉️",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 140.dp), // Extra space for bottom nav
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── ADMIN HEADER CARD ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛡️", fontSize = 28.sp)
                            Column {
                                Text(
                                    "لوحة تحكم المشرف",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "تحديث وإدارة الصفوف والمواد الدراسية",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.logoutAdmin()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Text("👋", fontSize = 16.sp)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val totalClasses = educationStages.sumOf { stage ->
                            if (stage.id == "i3dadee") {
                                (stage.branches["ilmi"]?.size ?: 0) + (stage.branches["adabi"]?.size ?: 0)
                            } else {
                                stage.classes.size
                            }
                        }
                        val totalSubjects = educationStages.sumOf { stage ->
                            val classes = if (stage.id == "i3dadee") {
                                (stage.branches["ilmi"] ?: emptyList()) + (stage.branches["adabi"] ?: emptyList())
                            } else {
                                stage.classes
                            }
                            classes.sumOf { it.subjects.size }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("${educationStages.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("مراحل", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("$totalClasses", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("صفوف", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("$totalSubjects", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("مواد", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Save New Restore Point / Backup Button
                        Button(
                            onClick = { viewModel.createRestorePoint(force = true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // Green Color
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💾", fontSize = 16.sp)
                                Text(
                                    "حفظ نقطة استعادة جديدة للتطبيق (نسخة احتياطية)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Complete Backup Restore Button
                        Button(
                            onClick = { showRestorePointConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5), // Material Blue
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🛡️", fontSize = 16.sp)
                                Text(
                                    "استعادة نسخة التطبيق الاحتياطية الشاملة",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Default Stages Restore Button
                        Button(
                            onClick = { showRestoreDefaultConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.9f),
                                contentColor = PrimaryColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔄", fontSize = 16.sp)
                                Text(
                                    "استعادة المراحل والصفوف الافتراضية فقط",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ─── SUB TABS ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "classes") PrimaryColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { activeSubTab = "classes" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🏫 الصفوف",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == "classes") PrimaryColor else TextSoft
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "subjects") PrimaryColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { activeSubTab = "subjects" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📖 المواد",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == "subjects") PrimaryColor else TextSoft
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "notifications") PrimaryColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { activeSubTab = "notifications" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📢 الإعلانات",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == "notifications") PrimaryColor else TextSoft
                    )
                }
            }

            if (activeSubTab == "classes") {
                // Stage Selection for Classes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اختر المرحلة الدراسية:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StageTabButton(
                            title = "👶 الابتدائية",
                            isSelected = listSelectedStageId == "ibtidaee",
                            activeColor = GreenColor,
                            activeColorLight = GreenLightColor,
                            onClick = { listSelectedStageId = "ibtidaee" },
                            modifier = Modifier.weight(1f)
                        )
                        StageTabButton(
                            title = "👦 المتوسطة",
                            isSelected = listSelectedStageId == "mutawasit",
                            activeColor = PrimaryColor,
                            activeColorLight = PrimaryLightColor,
                            onClick = { listSelectedStageId = "mutawasit" },
                            modifier = Modifier.weight(1f)
                        )
                        StageTabButton(
                            title = "🎓 الإعدادية",
                            isSelected = listSelectedStageId == "i3dadee",
                            activeColor = RedColor,
                            activeColorLight = RedLightColor,
                            onClick = { listSelectedStageId = "i3dadee" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (listSelectedStageId == "i3dadee") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BranchTabButton(
                            title = "🔬 الفرع العلمي",
                            isSelected = listSelectedBranchId == "ilmi",
                            activeColor = PrimaryColor,
                            onClick = { listSelectedBranchId = "ilmi" },
                            modifier = Modifier.weight(1f)
                        )
                        BranchTabButton(
                            title = "📖 الفرع الأدبي",
                            isSelected = listSelectedBranchId == "adabi",
                            activeColor = PurpleColor,
                            onClick = { listSelectedBranchId = "adabi" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Classes list
                val stage = educationStages.firstOrNull { it.id == listSelectedStageId }
                if (stage != null) {
                    val classes = if (listSelectedStageId == "i3dadee") {
                        stage.branches[listSelectedBranchId] ?: emptyList()
                    } else {
                        stage.classes
                    }

                    if (classes.isEmpty()) {
                        Text("لا توجد صفوف حالياً", color = TextSoft, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        classes.forEach { cls ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryColor.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cls.emoji, fontSize = 20.sp)
                                    }
                                    Column {
                                        Text(cls.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                                        Text("${cls.subjects.size} مواد دراسية", fontSize = 12.sp, color = TextSoft)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingClassId = cls.id
                                            selectedStageIdForClass = listSelectedStageId
                                            selectedBranchIdForClass = if (listSelectedStageId == "i3dadee") listSelectedBranchId else null
                                            classNameInput = cls.name
                                            classEmojiInput = cls.emoji
                                            showClassDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("✏️", fontSize = 16.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            deleteConfirmType = "class"
                                            deleteConfirmClassId = cls.id
                                            deleteConfirmClassName = cls.name
                                            showDeleteConfirmDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("🗑️", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            editingClassId = null
                            selectedStageIdForClass = listSelectedStageId
                            selectedBranchIdForClass = if (listSelectedStageId == "i3dadee") listSelectedBranchId else null
                            classNameInput = ""
                            classEmojiInput = "🏫"
                            showClassDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("➕ إضافة صف دراسي جديد", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else if (activeSubTab == "subjects") {
                // Stage Selection for Subjects
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اختر المرحلة والصف الدراسي:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StageTabButton(
                            title = "👶 الابتدائية",
                            isSelected = listSelectedStageId == "ibtidaee",
                            activeColor = GreenColor,
                            activeColorLight = GreenLightColor,
                            onClick = { listSelectedStageId = "ibtidaee" },
                            modifier = Modifier.weight(1f)
                        )
                        StageTabButton(
                            title = "👦 المتوسطة",
                            isSelected = listSelectedStageId == "mutawasit",
                            activeColor = PrimaryColor,
                            activeColorLight = PrimaryLightColor,
                            onClick = { listSelectedStageId = "mutawasit" },
                            modifier = Modifier.weight(1f)
                        )
                        StageTabButton(
                            title = "🎓 الإعدادية",
                            isSelected = listSelectedStageId == "i3dadee",
                            activeColor = RedColor,
                            activeColorLight = RedLightColor,
                            onClick = { listSelectedStageId = "i3dadee" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (listSelectedStageId == "i3dadee") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BranchTabButton(
                            title = "🔬 الفرع العلمي",
                            isSelected = listSelectedBranchId == "ilmi",
                            activeColor = PrimaryColor,
                            onClick = { listSelectedBranchId = "ilmi" },
                            modifier = Modifier.weight(1f)
                        )
                        BranchTabButton(
                            title = "📖 الفرع الأدبي",
                            isSelected = listSelectedBranchId == "adabi",
                            activeColor = PurpleColor,
                            onClick = { listSelectedBranchId = "adabi" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val stage = educationStages.firstOrNull { it.id == listSelectedStageId }
                if (stage != null) {
                    val classes = if (listSelectedStageId == "i3dadee") {
                        stage.branches[listSelectedBranchId] ?: emptyList()
                    } else {
                        stage.classes
                    }

                    // Horizontal selector for Classes
                    if (classes.isNotEmpty()) {
                        Text("اختر الصف لتعديل مواده:", fontSize = 13.sp, color = TextSoft)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(classes) { cls ->
                                val isSelected = listSelectedClassId == cls.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) PrimaryColor else Color.White)
                                        .border(1.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(20.dp))
                                        .clickable { listSelectedClassId = cls.id }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${cls.emoji} ${cls.name}",
                                        color = if (isSelected) Color.White else TextDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Now show subjects under this selected class
                        val activeClass = classes.firstOrNull { it.id == listSelectedClassId }
                        if (activeClass != null) {
                            if (activeClass.subjects.isEmpty()) {
                                Text("لا توجد مواد مضافة لهذا الصف حالياً", color = TextSoft, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                activeClass.subjects.forEach { subject ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(PrimaryColor.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(subject.icon, fontSize = 20.sp)
                                            }
                                            Column {
                                                Text(subject.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                                                Text(if (subject.type == "book") "📚 كتاب" else "📋 ملازم", fontSize = 12.sp, color = TextSoft)
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    editingSubjectId = subject.id
                                                    selectedStageIdForSubject = listSelectedStageId
                                                    selectedBranchIdForSubject = if (listSelectedStageId == "i3dadee") listSelectedBranchId else null
                                                    selectedClassIdForSubject = listSelectedClassId
                                                    subjectNameInput = subject.name
                                                    subjectIconInput = subject.icon
                                                    subjectTypeInput = subject.type
                                                    showSubjectDialog = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Text("✏️", fontSize = 16.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    deleteConfirmType = "subject"
                                                    deleteConfirmSubjectId = subject.id
                                                    deleteConfirmSubjectName = subject.name
                                                    showDeleteConfirmDialog = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Text("🗑️", fontSize = 16.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    editingSubjectId = null
                                    selectedStageIdForSubject = listSelectedStageId
                                    selectedBranchIdForSubject = if (listSelectedStageId == "i3dadee") listSelectedBranchId else null
                                    selectedClassIdForSubject = listSelectedClassId
                                    subjectNameInput = ""
                                    subjectIconInput = "📖"
                                    subjectTypeInput = "book"
                                    showSubjectDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("➕ إضافة مادة/درس جديد", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Text("الرجاء إضافة صف أولاً.", color = TextSoft, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            } else if (activeSubTab == "notifications") {
                // Publish Notification Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "📢 نشر إعلان أو تنبيه جديد للطلاب",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        OutlinedTextField(
                            value = notificationTitle,
                            onValueChange = { notificationTitle = it },
                            label = { Text("عنوان التنبيه") },
                            placeholder = { Text("مثال: تحديث عاجل لجدول السادس الإعدادي") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notificationContent,
                            onValueChange = { notificationContent = it },
                            label = { Text("محتوى التنبيه / التفاصيل") },
                            placeholder = { Text("اكتب هنا المعلومات التي تود نشرها وتنبيه الطلاب بها...") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        OutlinedTextField(
                            value = notificationTargetUrl,
                            onValueChange = { notificationTargetUrl = it },
                            label = { Text("رابط خارجي للمتابعة (اختياري)") },
                            placeholder = { Text("https://example.com") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Type selector Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("نوع التنبيه:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val types = listOf(
                                    Triple("general", "🔔 عام", PrimaryColor),
                                    Triple("urgent", "🚨 عاجل", RedColor),
                                    Triple("new_materials", "📚 ملازم", GreenColor),
                                    Triple("info", "💡 تنويه", PurpleColor)
                                )
                                types.forEach { (typeId, typeLabel, typeColor) ->
                                    val isSelected = notificationType == typeId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) typeColor.copy(alpha = 0.12f) else Color.Transparent)
                                            .border(1.5.dp, if (isSelected) typeColor else BorderColor, RoundedCornerShape(8.dp))
                                            .clickable { notificationType = typeId }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) typeColor else TextSoft
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (notificationTitle.isNotBlank() && notificationContent.isNotBlank()) {
                                    viewModel.publishNotification(
                                        title = notificationTitle.trim(),
                                        content = notificationContent.trim(),
                                        type = notificationType,
                                        targetUrl = notificationTargetUrl.trim().ifBlank { null }
                                    )
                                    showToast("📢 تم نشر التنبيه بنجاح وارسال إشعار للنظام!")
                                    // Clear form
                                    notificationTitle = ""
                                    notificationContent = ""
                                    notificationTargetUrl = ""
                                    notificationType = "general"
                                } else {
                                    showToast("الرجاء ملء عنوان ومحتوى التنبيه!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📢", fontSize = 16.sp)
                                Text("نشر وتعميم التنبيه للطلاب", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Published Notifications List Card
                val publishedNotifications by viewModel.notifications.collectAsState()

                Text(
                    text = "📋 الإعلانات والتنبيهات النشطة (${publishedNotifications.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (publishedNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد إعلانات منشورة حالياً.", color = TextSoft, fontSize = 14.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        publishedNotifications.forEach { notif ->
                            val colorScheme = when (notif.type) {
                                "urgent" -> Pair(RedColor, RedLightColor)
                                "new_materials" -> Pair(GreenColor, GreenLightColor)
                                "info" -> Pair(PurpleColor, PurpleColor.copy(alpha = 0.08f))
                                else -> Pair(PrimaryColor, PrimaryLightColor)
                            }
                            val badgeLabel = when (notif.type) {
                                "urgent" -> "🚨 عاجل جداً"
                                "new_materials" -> "📚 ملازم جديدة"
                                "info" -> "💡 تنويه"
                                else -> "🔔 عام"
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(colorScheme.first.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = badgeLabel,
                                                color = colorScheme.first,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = notif.date,
                                                color = TextSoft,
                                                fontSize = 11.sp
                                            )

                                            IconButton(
                                                onClick = { viewModel.deleteNotification(notif.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Text("🗑️", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Text(
                                        text = notif.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )

                                    Text(
                                        text = notif.content,
                                        fontSize = 13.sp,
                                        color = TextSoft,
                                        lineHeight = 18.sp
                                    )

                                    if (notif.targetUrl != null) {
                                        Text(
                                            text = "🔗 الرابط المرفق: ${notif.targetUrl}",
                                            fontSize = 12.sp,
                                            color = PrimaryColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── CLASS EDIT/ADD DIALOG ───
    if (showClassDialog) {
        Dialog(onDismissRequest = { showClassDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        if (editingClassId == null) "🏫 إضافة صف دراسي جديد" else "✏️ تعديل الصف الدراسي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    OutlinedTextField(
                        value = classNameInput,
                        onValueChange = { classNameInput = it },
                        label = { Text("اسم الصف الدراسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = classEmojiInput,
                        onValueChange = { classEmojiInput = it },
                        label = { Text("رمز الصف (Emoji)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (classNameInput.isNotEmpty()) {
                                    viewModel.saveClass(
                                        selectedStageIdForClass,
                                        selectedBranchIdForClass,
                                        editingClassId,
                                        classNameInput,
                                        classEmojiInput
                                    )
                                    showClassDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showClassDialog = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }
                    }
                }
            }
        }
    }

    // ─── SUBJECT EDIT/ADD DIALOG ───
    if (showSubjectDialog) {
        Dialog(onDismissRequest = { showSubjectDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        if (editingSubjectId == null) "📖 إضافة مادة جديدة" else "✏️ تعديل المادة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    OutlinedTextField(
                        value = subjectNameInput,
                        onValueChange = { subjectNameInput = it },
                        label = { Text("اسم المادة (مثل: علوم أو الاحياء)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = subjectIconInput,
                        onValueChange = { subjectIconInput = it },
                        label = { Text("أيقونة المادة (Emoji)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type selection row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("نوع المادة:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (subjectTypeInput == "book") PrimaryColor.copy(alpha = 0.1f) else Color.White)
                                    .border(1.dp, if (subjectTypeInput == "book") PrimaryColor else BorderColor, RoundedCornerShape(8.dp))
                                    .clickable { subjectTypeInput = "book" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📚 كتاب", fontWeight = FontWeight.Bold, color = if (subjectTypeInput == "book") PrimaryColor else TextSoft)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (subjectTypeInput == "notes") PrimaryColor.copy(alpha = 0.1f) else Color.White)
                                    .border(1.dp, if (subjectTypeInput == "notes") PrimaryColor else BorderColor, RoundedCornerShape(8.dp))
                                    .clickable { subjectTypeInput = "notes" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📋 ملازم", fontWeight = FontWeight.Bold, color = if (subjectTypeInput == "notes") PrimaryColor else TextSoft)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (subjectNameInput.isNotEmpty()) {
                                    viewModel.saveSubject(
                                        selectedStageIdForSubject,
                                        selectedBranchIdForSubject,
                                        selectedClassIdForSubject,
                                        editingSubjectId,
                                        subjectNameInput,
                                        subjectIconInput,
                                        subjectTypeInput
                                    )
                                    showSubjectDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showSubjectDialog = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }
                    }
                }
            }
        }
    }

    // ─── DELETE CONFIRMATION DIALOG ───
    if (showDeleteConfirmDialog) {
        Dialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🗑️ تأكيد الحذف",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )

                    val itemLabel = if (deleteConfirmType == "class") "الصف الدراسي" else "المادة الدراسية"
                    val itemName = if (deleteConfirmType == "class") deleteConfirmClassName else deleteConfirmSubjectName

                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف $itemLabel؟",
                        fontSize = 16.sp,
                        color = TextDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = itemName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = "⚠️ انتبه: هذا الإجراء لا يمكن التراجع عنه وسيتم حذف جميع البيانات المرتبطة به.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (deleteConfirmType == "class") {
                                    viewModel.deleteClass(
                                        listSelectedStageId,
                                        if (listSelectedStageId == "i3dadee") listSelectedBranchId else null,
                                        deleteConfirmClassId
                                    )
                                } else {
                                    viewModel.deleteSubject(
                                        listSelectedStageId,
                                        if (listSelectedStageId == "i3dadee") listSelectedBranchId else null,
                                        listSelectedClassId,
                                        deleteConfirmSubjectId
                                    )
                                }
                                showDeleteConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حذف", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }
                    }
                }
            }
        }
    }

    // ─── RESTORE DEFAULT CONFIRMATION DIALOG ───
    if (showRestoreDefaultConfirm) {
        Dialog(onDismissRequest = { showRestoreDefaultConfirm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔄 استعادة الافتراضيات",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )

                    Text(
                        text = "هل أنت متأكد من رغبتك في استعادة المراحل والصفوف الافتراضية؟",
                        fontSize = 16.sp,
                        color = TextDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "⚠️ انتبه: هذا الإجراء سيقوم بإعادة تعيين المراحل والصفوف الدراسية فقط إلى وضعها الأصلي.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.restoreDefaultStages()
                                showRestoreDefaultConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد الاستعادة", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showRestoreDefaultConfirm = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }
                    }
                }
            }
        }
    }

    // ─── RESTORE COMPLETE BACKUP CONFIRMATION DIALOG ───
    if (showRestorePointConfirm) {
        Dialog(onDismissRequest = { showRestorePointConfirm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛡️ استعادة النسخة الشاملة",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )

                    Text(
                        text = "هل أنت متأكد من استعادة النسخة الاحتياطية الكاملة للتطبيق؟",
                        fontSize = 16.sp,
                        color = TextDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "⚠️ انتبه: هذا الإجراء سيقوم بحذف التغييرات الحالية واستعادة جميع الكتب، المراحل، الإشعارات، والتنبيهات المعتمدة في نقطة الاستعادة بنسبة 100%.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.restoreFromRestorePoint()
                                showRestorePointConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("استعادة الآن", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showRestorePointConfirm = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsTabScreen(
    viewModel: MainViewModel,
    onPreviewFile: (String, String) -> Unit,
    onDeleteFile: (com.example.data.SubjectFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedResultStage by viewModel.selectedResultStage.collectAsState()
    var expandedStage by remember { mutableStateOf<String?>(null) }
    val cityFiles by viewModel.cityFiles.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val resultsAlert by viewModel.resultsAlert.collectAsState()

    // Dialog state for Admin results upload
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadFileNameInput by remember { mutableStateOf("") }
    var selectedUriForUpload by remember { mutableStateOf<String?>(null) }
    var uploadFileSize by remember { mutableStateOf("") }

    val filePickerLauncher = rememberPlatformFilePicker { fileUri, name, size ->
        selectedUriForUpload = fileUri
        uploadFileSize = size
        if (uploadFileNameInput.isEmpty()) {
            uploadFileNameInput = name.replace(".pdf", "", ignoreCase = true)
        }
    }

    if (selectedCity == null) {
        // --- VIEW A: List of Cities ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp), // space for bottom nav & ads
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🚨 Active Admin Alert Banner for Users
            if (resultsAlert != null && resultsAlert!!.isActive && resultsAlert!!.text.trim().isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4EC)),
                    border = borderStroke(1.dp, Color(0xFFFFD1B2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7E36)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📢", fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resultsAlert!!.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC04700)
                            )
                            if (resultsAlert!!.date.isNotEmpty()) {
                                Text(
                                    text = "تحديث عاجل: ${resultsAlert!!.date}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE27C3E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 🔧 Admin Control Panel for Managing results section alerts
            if (isAdmin) {
                var isEditingAlert by remember { mutableStateOf(false) }
                var alertTextInput by remember { mutableStateOf(resultsAlert?.text ?: "") }

                LaunchedEffect(resultsAlert) {
                    resultsAlert?.let {
                        if (alertTextInput.isEmpty() && it.text.isNotEmpty()) {
                            alertTextInput = it.text
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔧", fontSize = 20.sp)
                                Column {
                                    Text(
                                        "لوحة إعداد التنبيهات للمشرفين",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        "نشر شريط تنبيهي عاجل في قسم النتائج لجميع المستخدمين",
                                        fontSize = 11.sp,
                                        color = TextSoft
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isEditingAlert = !isEditingAlert }
                            ) {
                                Text(if (isEditingAlert) "🔼 إخفاء" else "⚙️ إعداد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            }
                        }

                        if (isEditingAlert) {
                            OutlinedTextField(
                                value = alertTextInput,
                                onValueChange = { alertTextInput = it },
                                label = { Text("نص التنبيه العاجل (مثال: يتم الآن رفع النتائج..)") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = BorderColor
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Deactivate/Disable button (only shown if currently active)
                                if (resultsAlert?.isActive == true) {
                                    Button(
                                        onClick = {
                                            viewModel.updateResultsAlert(alertTextInput, false)
                                            isEditingAlert = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("🛑 إيقاف التنبيه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (alertTextInput.trim().isNotEmpty()) {
                                            viewModel.updateResultsAlert(alertTextInput, true)
                                            isEditingAlert = false
                                        } else {
                                            showToast("الرجاء إدخال نص التنبيه أولاً")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("📢 نشر وتفعيل الآن", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section Title Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 28.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نتائج الامتحانات الوزارية",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "اختر المرحلة الدراسية ثم المحافظة للاستعلام عن النتائج",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "المراحل الدراسية المتاحة 🎓",
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val stagesList = listOf(
                Triple("primary_6", "نتائج السادس الابتدائي 🎒", "نتائج امتحانات الصف السادس الابتدائي لجميع المحافظات العراقية"),
                Triple("intermediate_3", "نتائج الثالث المتوسط 🎓", "نتائج امتحانات الصف الثالث المتوسط لجميع المحافظات العراقية"),
                Triple("preparatory_6", "نتائج السادس الاعدادي (ادبي - علمي) 🏛️", "نتائج امتحانات الصف السادس الاعدادي بفرعيه العلمي والأدبي")
            )

            stagesList.forEach { (stageKey, stageTitle, stageDesc) ->
                val isExpanded = expandedStage == stageKey
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(if (isExpanded) 1.5.dp else 1.dp, if (isExpanded) PrimaryColor else BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedStage = if (isExpanded) null else stageKey
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isExpanded) PrimaryColor.copy(alpha = 0.1f) else Color(0xFFF5F7FA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (stageKey) {
                                        "primary_6" -> "🎒"
                                        "intermediate_3" -> "🎓"
                                        else -> "🏛️"
                                    }
                                    Text(icon, fontSize = 22.sp)
                                }
                                Column {
                                    Text(
                                        text = stageTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isExpanded) PrimaryColor else TextDark
                                    )
                                    Text(
                                        text = stageDesc,
                                        fontSize = 11.sp,
                                        color = TextSoft,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isExpanded) "🔼" else "🔽",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(bottom = 12.dp))
                            
                            Text(
                                text = "اختر المحافظة لاستعراض النتائج: 👇",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // Render the 18 governorates chunked 2 per row
                            val chunkedCities = IraqiCitiesData.cities.chunked(2)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                chunkedCities.forEach { rowCities ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowCities.forEach { city ->
                                            CityGridItem(
                                                city = city,
                                                onClick = {
                                                    viewModel.selectedResultStage.value = stageKey
                                                    viewModel.selectedCity.value = city
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowCities.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- VIEW B: City Details / Results PDF list ---
        val city = selectedCity!!
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 140.dp), // space for bottom nav & ads
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // City Header Row (with Back Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.selectedCity.value = null },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Text("➡️", fontSize = 16.sp) // RTL Back Arrow
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(city.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = "نتائج محافظة ${city.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            val stageName = when (selectedResultStage) {
                                "primary_6" -> "السادس الابتدائي"
                                "intermediate_3" -> "الثالث المتوسط"
                                "preparatory_6" -> "السادس الاعدادي"
                                else -> "ملفات الـ PDF المرفوعة"
                            }
                            Text(
                                text = "القسم: $stageName",
                                fontSize = 12.sp,
                                color = TextSoft,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 🚨 Active Admin Alert Banner inside City Details
            if (resultsAlert != null && resultsAlert!!.isActive && resultsAlert!!.text.trim().isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4EC)),
                    border = borderStroke(1.dp, Color(0xFFFFD1B2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📢", fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resultsAlert!!.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04700)
                            )
                        }
                    }
                }
            }

            // 🌐 Official Results Portal Button (Direct Link to Najah)
            Button(
                onClick = {
                    try { openUrl("https://najah.iq/") } catch (e: Exception) { showToast("تعذر فتح الرابط حالياً") }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔍", fontSize = 16.sp)
                    Text(
                        "عرض النتائج",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Admin Actions inside Results screen (directly in that section!)
            if (isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AccentColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.5.dp, AccentColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛡️", fontSize = 20.sp)
                            Column {
                                Text(
                                    "صلاحيات المشرف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    "رفع ملف نتائج جديد للمحافظة",
                                    fontSize = 11.sp,
                                    color = TextSoft
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedUriForUpload = null
                                uploadFileNameInput = ""
                                showUploadDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("➕ رفع ملف PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Show Upload Progress Bar
            if (uploadProgress != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("جاري رفع الملف الآن...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${uploadProgress}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                        }
                        LinearProgressIndicator(
                            progress = { uploadProgress!!.toFloat() / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryColor,
                            trackColor = BorderColor
                        )
                    }
                }
            }

            // List of PDFs
            if (cityFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📭", fontSize = 48.sp)
                        Text(
                            "لا توجد نتائج مرفوعة حالياً",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSoft
                        )
                        Text(
                            "يتم العمل على جلب ورفع نتائج الامتحان الوزاري فور صدورها",
                            fontSize = 13.sp,
                            color = TextSoft,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cityFiles) { file ->
                        ResultFileItemRow(
                            file = file,
                            isAdmin = isAdmin,
                            onPreview = { onPreviewFile(file.name, file.downloadUrl) },
                            onDownload = {
                                PlatformAdManager.showAdWithAction {
                                    viewModel.downloadRemoteFile(file)
                                }
                            },
                            onShare = { viewModel.shareLocalFile(file) },
                            onDelete = {
                                onDeleteFile(file)
                            }
                        )
                    }
                }
            }
        }
    }

    // --- UPLOAD RESULTS PDF DIALOG ---
    if (showUploadDialog) {
        Dialog(onDismissRequest = { showUploadDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = borderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val stageName = when (selectedResultStage) {
                        "primary_6" -> "السادس الابتدائي"
                        "intermediate_3" -> "الثالث المتوسط"
                        "preparatory_6" -> "السادس الاعدادي"
                        else -> ""
                    }
                    Text(
                        text = "رفع نتيجة جديدة - ${selectedCity?.name} ($stageName)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    HorizontalDivider(color = BorderColor)

                    // Step 1: Pick file
                    Button(
                        onClick = { filePickerLauncher() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedUriForUpload != null) GreenColor else PrimaryColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedUriForUpload != null) "✅ تم اختيار الملف" else "📁 اختر ملف النتائج (PDF)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (selectedUriForUpload != null) {
                        // Step 2: Name Input
                        OutlinedTextField(
                            value = uploadFileNameInput,
                            onValueChange = { uploadFileNameInput = it },
                            label = { Text("اسم النتيجة (مثال: نتائج الثالث المتوسط الدور الأول)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showUploadDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = TextSoft)
                        }

                        Button(
                            onClick = {
                                val uri = selectedUriForUpload
                                if (uri != null && uploadFileNameInput.isNotEmpty()) {
                                    viewModel.uploadResultPdf(uri, "${uploadFileNameInput.replace(".pdf", "")}.pdf", uploadFileSize)
                                    showUploadDialog = false
                                } else {
                                    showToast("يرجى اختيار ملف وإدخال الاسم")
                                }
                            },
                            enabled = selectedUriForUpload != null && uploadFileNameInput.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("رفع ونشر", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityGridItem(
    city: City,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Text(city.emoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = city.nameEn,
                    fontSize = 11.sp,
                    color = TextSoft
                )
            }

            Text("⬅️", fontSize = 12.sp) // RTL forward arrow
        }
    }
}

@Composable
fun ResultFileItemRow(
    file: SubjectFileEntity,
    isAdmin: Boolean,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RedColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📕", fontSize = 18.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name.replace(".pdf", ""),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📐 ${file.size}", fontSize = 11.sp, color = TextSoft)
                        Text("•", fontSize = 11.sp, color = TextSoft)
                        Text("📅 ${file.date}", fontSize = 11.sp, color = TextSoft)
                    }
                }
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("👁️ عرض", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenColor),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("📥 تحميل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("🔗 مشاركة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RedColor.copy(alpha = 0.1f))
                    ) {
                        Text("🗑️", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
