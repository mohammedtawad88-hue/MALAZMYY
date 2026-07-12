package com.example

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.FullScreenContentCallback
import android.app.Activity
import android.telephony.SmsManager
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.EmailSignInDialog
import com.example.ui.AdminLoginDialog
import com.example.ui.AppLogo
import com.example.ui.AppLogoHeaderBar
import com.example.ui.QiCardDonationDialog
import com.example.viewmodel.MainViewModel

// CSS Palette translation
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Pre-create WebView cache directories to prevent Chromium E/chromium directory creation error
        try {
            val cache = cacheDir
            if (cache != null) {
                val webViewCacheDir = java.io.File(cache, "WebView/Default/HTTP Cache/Code Cache/js")
                if (!webViewCacheDir.exists()) {
                    webViewCacheDir.mkdirs()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to pre-create WebView cache directory: ${e.message}")
        }

        super.onCreate(savedInstanceState)
        try {
            MobileAds.initialize(this) {
                try {
                    InterstitialAdManager.loadAd(this)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Failed to load interstitial ad on startup: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize MobileAds: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force Right-to-Left (RTL) layout direction matching Arabic dir="rtl"
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: MainViewModel = viewModel()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = BgColor
                    ) { innerPadding ->
                        MainAppScreen(modifier = Modifier.padding(innerPadding), viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
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

    // Dynamic Color Palette
    val currentBgColor = BgColor
    val currentCardBgColor = Color.White
    val currentTextDark = TextDark
    val currentTextSoft = TextSoft
    val currentBorderColor = BorderColor

    // Preview File Dialog states
    var previewFileUrl by remember { mutableStateOf<String?>(null) }
    var previewFileName by remember { mutableStateOf<String?>(null) }

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
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val (name, size) = getFileNameAndSize(context, uri)
            viewModel.uploadPdf(uri, name, size)
        }
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
                            if (isAdmin) {
                                Button(
                                    onClick = {
                                        viewModel.logoutAdmin()
                                        Toast.makeText(context, "👋 تم تسجيل الخروج للمشرف", Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(context, "👋 تم تسجيل خروج المستخدم", Toast.LENGTH_SHORT).show()
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

                                // Admin login button
                                Button(
                                    onClick = { showAdminLoginDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentColor.copy(alpha = 0.2f),
                                        contentColor = AccentLightColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        AccentLightColor.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("🔐 دخول المشرف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
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
                                        themeColor = when {
                                            currentStageId == "ibtidaee" -> GreenColor
                                            currentStageId == "mutawasit" -> PrimaryColor
                                            currentBranchId == "ilmi" -> PrimaryColor
                                            else -> PurpleColor
                                        },
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
                        // Local/Saved files screen
                        var savedSubTab by remember { mutableStateOf("downloads") }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column {
                                Text(
                                    "المكتبة الشخصية والمفضلة",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextDark
                                )
                                Text(
                                    "تصفح الكتب والملازم التي قمت بتحميلها أو إضافتها إلى المفضلة",
                                    fontSize = 13.sp,
                                    color = currentTextSoft
                                )
                            }

                            // Sub-tabs chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomFilterChip(
                                    selected = savedSubTab == "downloads",
                                    label = "📥 تحميلاتي (${savedFiles.size})",
                                    onClick = { savedSubTab = "downloads" },
                                    borderColor = currentBorderColor
                                )
                                CustomFilterChip(
                                    selected = savedSubTab == "favorites",
                                    label = "❤️ المفضلة (${favoriteFiles.size})",
                                    onClick = { savedSubTab = "favorites" },
                                    borderColor = currentBorderColor
                                )
                            }

                            val filesToShow = if (savedSubTab == "downloads") savedFiles else favoriteFiles

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
                                        Text(if (savedSubTab == "downloads") "📥" else "❤️", fontSize = 44.sp)
                                        Text(
                                            if (savedSubTab == "downloads") "لا توجد ملفات محملة بعد" else "لا توجد ملفات في المفضلة بعد",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTextSoft
                                        )
                                        Text(
                                            if (savedSubTab == "downloads") "الملفات التي ستقوم بتحميلها ستظهر هنا تلقائياً" else "اضغط على زر القلب لحفظ ملفاتك المفضلة والوصول إليها لاحقاً",
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
                                        SavedFileItemRow(
                                            file = file,
                                            cardBgColor = currentCardBgColor,
                                            borderColor = currentBorderColor,
                                            textDark = currentTextDark,
                                            textSoft = currentTextSoft,
                                            onPreview = {
                                                previewFileName = file.name
                                                previewFileUrl = file.downloadUrl
                                            },
                                            onDownloadClick = {
                                                InterstitialAdManager.showAdWithAction(context) {
                                                    downloadFileWithManager(context, file.name, file.downloadUrl)
                                                }
                                            },
                                            onPrintClick = {
                                                shareFileLink(context, file.name, file.downloadUrl)
                                            },
                                            onFavoriteToggle = {
                                                viewModel.toggleFavorite(file)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "results" -> {
                        ResultsTabScreen(
                            viewModel = viewModel,
                            onPreviewFile = { name, url ->
                                previewFileName = name
                                previewFileUrl = url
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

                        // Files List header with filter chips
                        var modalFilterType by remember { mutableStateOf("all") }
                        val filteredModalFiles = remember(subjectFiles, modalFilterType) {
                            when (modalFilterType) {
                                "book" -> subjectFiles.filter { it.name.contains("كتاب") }
                                "notes" -> subjectFiles.filter { !it.name.contains("كتاب") }
                                else -> subjectFiles
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "الملفات المتاحة (${filteredModalFiles.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextDark
                                )
                                
                                // Mini chips
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CustomFilterChip(
                                        selected = modalFilterType == "all",
                                        label = "الكل",
                                        onClick = { modalFilterType = "all" },
                                        borderColor = currentBorderColor
                                    )
                                    CustomFilterChip(
                                        selected = modalFilterType == "book",
                                        label = "الكتب",
                                        onClick = { modalFilterType = "book" },
                                        borderColor = currentBorderColor
                                    )
                                    CustomFilterChip(
                                        selected = modalFilterType == "notes",
                                        label = "الملازم",
                                        onClick = { modalFilterType = "notes" },
                                        borderColor = currentBorderColor
                                    )
                                }
                            }
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
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📭", fontSize = 36.sp)
                                    Text(
                                        "لا توجد ملفات تطابق الفلتر المختار حالياً",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentTextSoft
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
                                                InterstitialAdManager.showAdWithAction(context) {
                                                    downloadFileWithManager(context, file.name, file.downloadUrl)
                                                }
                                            },
                                            onPrint = {
                                                shareFileLink(context, file.name, file.downloadUrl)
                                            },
                                            onFavoriteToggle = {
                                                viewModel.toggleFavorite(file)
                                            },
                                            onDelete = {
                                                viewModel.deleteFile(file)
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
                                    filePickerLauncher.launch("*/*")
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
                    InterstitialAdManager.showAdWithAction(context) {
                        downloadFileWithManager(context, previewFileName!!, previewFileUrl!!)
                    }
                }
            )
        }

        // ─── 6. LOGIN DIALOG MODALS ───
        if (showAdminLoginDialog) {
            AdminLoginDialog(
                viewModel = viewModel,
                onDismiss = { showAdminLoginDialog = false },
                onSuccess = {
                    showAdminLoginDialog = false
                    viewModel.selectTab("admin")
                    Toast.makeText(context, "✅ مرحباً بك يا مشرف المنصة المعتمد!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showUserLoginDialog) {
            EmailSignInDialog(
                viewModel = viewModel,
                onDismiss = { showUserLoginDialog = false },
                onSuccess = {
                    showUserLoginDialog = false
                    Toast.makeText(context, "✅ تم تسجيل الدخول بنجاح كعضو مميز!", Toast.LENGTH_SHORT).show()
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
                        title = "المحفوظات",
                        iconStr = "❤️",
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
            Divider(color = BorderColor.copy(alpha = 0.5f))
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
                            text = file.size,
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
                        text = "تاريخ الإضافة: ${file.date}",
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
            // Left actions: Favorite & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(if (file.isFavorite) "❤️" else "🤍", fontSize = 20.sp)
                }
                
                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("🗑️", fontSize = 18.sp)
                    }
                }
            }
            
            // Right actions: Download & Print
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        Text("تحميل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = onPrint,
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
                        Text("طباعة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    val unitId = "ca-app-pub-2442487595791215/5598362487" // اعلان بنر ملازمي
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                try {
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = unitId
                        loadAd(AdRequest.Builder().build())
                    }
                } catch (e: Exception) {
                    Log.e("AdmobBanner", "Failed to create AdView: ${e.message}")
                    android.view.View(context)
                }
            }
        )
    }
}

object InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    
    // Replace with your production AdMob Interstitial Unit ID when ready
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Official Google Test Interstitial ID

    fun loadAd(context: Context) {
        if (mInterstitialAd != null || isAdLoading) return
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdManager", "Ad failed to load: ${adError.message}")
                    mInterstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Ad loaded successfully.")
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }
            }
        )
    }

    fun showAdWithAction(context: Context, onAdDismissedOrFailed: () -> Unit) {
        val activity = context as? Activity
        if (activity != null && mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAdManager", "Ad dismissed.")
                    mInterstitialAd = null
                    // Reload ad for next time
                    loadAd(context)
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.d("InterstitialAdManager", "Ad failed to show.")
                    mInterstitialAd = null
                    // Reload ad for next time
                    loadAd(context)
                    onAdDismissedOrFailed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "Ad not ready or context is not Activity. Executing action directly.")
            // Try to load again for future clicks
            loadAd(context)
            onAdDismissedOrFailed()
        }
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
fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, String> {
    var name = "file.pdf"
    var sizeStr = "1.2 MB"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    val sizeBytes = cursor.getLong(sizeIndex)
                    val sizeKb = sizeBytes / 1024.0
                    sizeStr = if (sizeKb > 1024) {
                        String.format(java.util.Locale.US, "%.1f MB", sizeKb / 1024.0)
                    } else {
                        String.format(java.util.Locale.US, "%.1f KB", sizeKb)
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Utils", "Error resolving filename/size: ${e.message}")
    }
    return Pair(name, sizeStr)
}

// Standard file downloader helper
fun downloadFileWithManager(context: Context, name: String, rawUrl: String) {
    val url = rawUrl
        .replace("%D9%8I", "%D9%8A")
        .replace("%D2%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA", "%D8%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA")
        .replace("%D2%AA%D9%86%D9%83%D9%84", "%D8%A6%D9%86%D9%83%D9%84")
        .replace("%D5%A5%D9%84%D8%B5%D9%81", "%D9%84%D9%84%D8%B5%D9%81")
        .replace("%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B4", "%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B7")
        .replace("%D9%8F.pdf", "%D9%8A.pdf")

    if (url.startsWith("/")) {
        // Fallback local file sandbox path
        Toast.makeText(context, "الملف متوفر محلياً في ذاكرة التطبيق: $name", Toast.LENGTH_SHORT).show()
        // Try opening it
        try {
            val file = java.io.File(url)
            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, if (name.endsWith(".pdf", true)) "application/pdf" else "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "لم يتم العثور على قارئ ملفات لعرض: $name", Toast.LENGTH_SHORT).show()
        }
        return
    }
    
    // Check if the URL is a direct PDF link or an external webpage
    val isDirectPdf = url.endsWith(".pdf", ignoreCase = true) || 
                      url.contains(".pdf?") || 
                      url.contains("firebasestorage.googleapis.com")
                      
    if (!isDirectPdf) {
        // It's a webpage (like mlazemna.com)! Opening with DownloadManager will fail or download HTML.
        // We must open it in the browser so the user can easily view and download the book.
        try {
            Toast.makeText(context, "جاري فتح صفحة الكتاب لتحميله مباشرة...", Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "فشل فتح الرابط: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return
    }
    
    val fileNameWithExt = if (!name.endsWith(".pdf", ignoreCase = true)) {
        "$name.pdf"
    } else {
        name
    }
    val cleanFileName = fileNameWithExt.replace(Regex("[\\\\/:*?\"<>|]"), "_")

    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri).apply {
            setTitle(cleanFileName)
            setDescription("جاري تحميل ملزمة/كتاب من تطبيق ملازمي...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName)
            setMimeType("application/pdf")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, "بدأ تحميل: $cleanFileName. تفقد شريط الإشعارات.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "فشل التحميل: ${e.message}", Toast.LENGTH_SHORT).show()
        // If download manager fails, share the download link instead
        shareFileLink(context, cleanFileName, url)
    }
}

// Printer / share fallback helper
fun shareFileLink(context: Context, name: String, url: String) {
    try {
        if (url.startsWith("/")) {
            // Share local file binary directly
            val file = java.io.File(url)
            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (name.endsWith(".pdf", true)) "application/pdf" else "image/*"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة الملف"))
        } else {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "مشاركة ملزمة/كتاب من تطبيق ملازمي")
                putExtra(Intent.EXTRA_TEXT, "تحميل ملزمة/كتاب ($name) عبر هذا الرابط المباشر:\n$url")
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة رابط التحميل"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في مشاركة الملف: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

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

                Divider(color = BorderColor, thickness = 1.dp)

                // WebView embedding Google Docs Viewer
                val encodedUrl = android.net.Uri.encode(url)
                val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                }
                                webViewClient = WebViewClient()
                                loadUrl(googleViewerUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    "تنبيه: يتم تحميل المعاينة عبر الإنترنت. إذا لم تظهر، يمكنك تحميل الملف وقراءته محلياً.",
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
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    Text("Hello $name", modifier = modifier)
}

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onTriggerLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdmin by viewModel.isAdmin.collectAsState()
    val educationStages by viewModel.educationStages.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()

    var activeSubTab by remember { mutableStateOf("classes") } // "classes" or "subjects"

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

                    Divider(color = Color.White.copy(alpha = 0.2f))

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

                    Divider(color = Color.White.copy(alpha = 0.2f))

                    Button(
                        onClick = { viewModel.restoreDefaultStages() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
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
                                "استعادة المراحل والصفوف الافتراضية",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                        "🏫 إدارة الصفوف والمراحل",
                        fontSize = 14.sp,
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
                        "📖 إدارة المواد والدروس",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == "subjects") PrimaryColor else TextSoft
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
                                            viewModel.deleteClass(listSelectedStageId, if (listSelectedStageId == "i3dadee") listSelectedBranchId else null, cls.id)
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
            } else {
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
                                                    viewModel.deleteSubject(
                                                        listSelectedStageId,
                                                        if (listSelectedStageId == "i3dadee") listSelectedBranchId else null,
                                                        listSelectedClassId,
                                                        subject.id
                                                    )
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsTabScreen(
    viewModel: MainViewModel,
    onPreviewFile: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
    var selectedUriForUpload by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUriForUpload = uri
            val (name, _) = getFileNameAndSize(context, uri)
            if (uploadFileNameInput.isEmpty()) {
                uploadFileNameInput = name.replace(".pdf", "", ignoreCase = true)
            }
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
                                            Toast.makeText(context, "الرجاء إدخال نص التنبيه أولاً", Toast.LENGTH_SHORT).show()
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
                                InterstitialAdManager.showAdWithAction(context) {
                                    downloadFileWithManager(context, file.name, file.downloadUrl)
                                }
                            },
                            onShare = { shareFileLink(context, file.name, file.downloadUrl) },
                            onDelete = { viewModel.deleteFile(file) }
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
                        onClick = { filePickerLauncher.launch("application/pdf") },
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
                                    val (_, sizeStr) = getFileNameAndSize(context, uri)
                                    viewModel.uploadResultPdf(uri, "${uploadFileNameInput.replace(".pdf", "")}.pdf", sizeStr)
                                    showUploadDialog = false
                                } else {
                                    Toast.makeText(context, "يرجى اختيار ملف وإدخال الاسم", Toast.LENGTH_SHORT).show()
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
