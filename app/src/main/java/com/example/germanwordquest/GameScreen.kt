package com.example.germanwordquest

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

// --- 1. COLORS ---
val SoftBlue = Color(0xFF5C9DFF)
val SoftPurple = Color(0xFF9C27B0)
val SoftTeal = Color(0xFF009688)
val SoftOrange = Color(0xFFFF9800)
val SoftRed = Color(0xFFE91E63)
val SoftNavy = Color(0xFF3F51B5)
val ThemeList = listOf(SoftBlue, SoftPurple, SoftTeal, SoftOrange, SoftRed, SoftNavy)

// --- 2. DATA MODELS ---
data class CloudWord(
    val id: String,
    val german: String,
    val english: String,
    val article: String = "",
    val category: String = "General"
)

enum class ScreenState { HOME, GAME, SETTINGS, DICTIONARY }

// --- 3. UTILS ---
fun simplifyForGrid(word: String): String {
    return word.uppercase()
        .replace("ß", "SS")
        .replace("Ä", "A")
        .replace("Ö", "O")
        .replace("Ü", "U")
}

@Composable
fun GameScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ScreenState.HOME) }
    var previousScreen by remember { mutableStateOf(ScreenState.HOME) }

    // --- SAVE SYSTEM ---
    val sharedPref = remember { context.getSharedPreferences("GermanQuestData", Context.MODE_PRIVATE) }
    var learnedIds by remember {
        mutableStateOf(sharedPref.getStringSet("learned_ids", emptySet()) ?: emptySet())
    }

    fun saveProgress(newId: String) {
        val newSet = learnedIds + newId
        learnedIds = newSet
        sharedPref.edit().putStringSet("learned_ids", newSet).apply()
    }

    // Settings
    var isSoundEnabled by remember { mutableStateOf(sharedPref.getBoolean("sound_enabled", true)) }
    var isRandomTheme by remember { mutableStateOf(sharedPref.getBoolean("random_theme", true)) }
    var selectedThemeIndex by remember { mutableIntStateOf(sharedPref.getInt("theme_index", 0)) }

    fun saveSettings(sound: Boolean, random: Boolean, index: Int) {
        isSoundEnabled = sound; isRandomTheme = random; selectedThemeIndex = index
        sharedPref.edit().putBoolean("sound_enabled", sound).putBoolean("random_theme", random).putInt("theme_index", index).apply()
    }

    val sessionRandomColor = remember { ThemeList.random() }
    val appColor = if (isRandomTheme) sessionRandomColor else ThemeList[selectedThemeIndex]

    // Audio
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale.GERMAN }
    }
    fun playSound(text: String) { if (isSoundEnabled) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) }

    fun navigateTo(destination: ScreenState) {
        previousScreen = currentScreen
        currentScreen = destination
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (currentScreen == ScreenState.HOME || currentScreen == ScreenState.SETTINGS) {
                NavigationBar(containerColor = Color(0xFFF8F9FA)) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") },
                        selected = currentScreen == ScreenState.HOME, onClick = { currentScreen = ScreenState.HOME },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = appColor, indicatorColor = appColor.copy(alpha = 0.2f))
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") },
                        selected = currentScreen == ScreenState.SETTINGS, onClick = { currentScreen = ScreenState.SETTINGS },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = appColor, indicatorColor = appColor.copy(alpha = 0.2f))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentScreen) {
                ScreenState.HOME -> HomeScreen(
                    appColor,
                    onPlay = { navigateTo(ScreenState.GAME) },
                    onOpenDictionary = { navigateTo(ScreenState.DICTIONARY) },
                    learnedCount = learnedIds.size
                )
                ScreenState.SETTINGS -> SettingsScreen(
                    appColor, isSoundEnabled, isRandomTheme, selectedThemeIndex,
                    onUpdate = { s, r, i -> saveSettings(s, r, i) }
                )
                ScreenState.GAME -> GameplayScreen(
                    appColor, learnedIds,
                    onWordLearned = { word -> saveProgress(word.id) },
                    onSpeak = { playSound(it) },
                    onBack = { currentScreen = ScreenState.HOME },
                    onOpenDictionary = { navigateTo(ScreenState.DICTIONARY) }
                )
                ScreenState.DICTIONARY -> DictionaryScreen(
                    appColor,
                    onBack = { currentScreen = previousScreen }
                )
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN (Clean Production Version)
// ==========================================
@Composable
fun HomeScreen(themeColor: Color, onPlay: () -> Unit, onOpenDictionary: () -> Unit, learnedCount: Int) {
    var showInfo by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Spacer(modifier = Modifier.weight(1f))
        Text("German Quest", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2C3E50))
        Text("Learn German Daily", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onPlay, colors = ButtonDefaults.buttonColors(containerColor = themeColor),
            shape = RoundedCornerShape(50), modifier = Modifier.width(200.dp).height(60.dp).shadow(10.dp, RoundedCornerShape(50), spotColor = themeColor)
        ) { Text("Play", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onOpenDictionary, border = BorderStroke(1.dp, Color.LightGray),
            shape = RoundedCornerShape(50), modifier = Modifier.height(48.dp)
        ) { Text("My Words ($learnedCount) \uD83D\uDCD6", color = Color.Gray) }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showInfo = true }.padding(8.dp)
        ) {
            Icon(Icons.Default.Info, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Grid Rules", fontSize = 12.sp, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.weight(1.5f))
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false }, containerColor = Color.White, title = { Text("Grid Rules \u2139\uFE0F") },
            text = { Column { Text("• ß \u279C SS\n• Ä \u279C A\n• Ö \u279C O\n• Ü \u279C U", fontWeight = FontWeight.Bold) } },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Got it") } }
        )
    }
}

// ==========================================
// 2. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(themeColor: Color, isSound: Boolean, isRandom: Boolean, themeIdx: Int, onUpdate: (Boolean, Boolean, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sound Effects", fontSize = 18.sp, color = Color.DarkGray)
            Switch(checked = isSound, onCheckedChange = { onUpdate(it, isRandom, themeIdx) }, colors = SwitchDefaults.colors(checkedThumbColor = themeColor))
        }
        Divider(color = Color(0xFFEEEEEE)); Spacer(modifier = Modifier.height(24.dp))

        Text("Appearance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray); Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUpdate(isSound, true, themeIdx) }.padding(8.dp)) {
            RadioButton(selected = isRandom, onClick = { onUpdate(isSound, true, themeIdx) }, colors = RadioButtonDefaults.colors(selectedColor = themeColor))
            Text("Random Color", fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUpdate(isSound, false, themeIdx) }.padding(8.dp)) {
            RadioButton(selected = !isRandom, onClick = { onUpdate(isSound, false, themeIdx) }, colors = RadioButtonDefaults.colors(selectedColor = themeColor))
            Text("Fixed Color", fontSize = 16.sp)
        }

        if (!isRandom) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ThemeList.forEachIndexed { index, color ->
                    Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(color).clickable { onUpdate(isSound, false, index) }, contentAlignment = Alignment.Center) {
                        if (themeIdx == index) Icon(Icons.Default.Check, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. GAMEPLAY SCREEN
// ==========================================
@Composable
fun GameplayScreen(themeColor: Color, learnedIds: Set<String>, onWordLearned: (CloudWord) -> Unit, onSpeak: (String) -> Unit, onBack: () -> Unit, onOpenDictionary: () -> Unit) {
    var allWords by remember { mutableStateOf<List<CloudWord>>(emptyList()) }
    var currentWords by remember(allWords, learnedIds) {
        mutableStateOf(allWords.filter { !learnedIds.contains(it.id) && !learnedIds.contains(it.german) }.shuffled().take(5))
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val gridChars = remember(currentWords, refreshTrigger) {
        if (currentWords.isNotEmpty()) PuzzleLogic.generateGrid(currentWords.map { simplifyForGrid(it.german) }) else List(100) { '?' }
    }

    var foundWordsInLevel by remember { mutableStateOf(setOf<CloudWord>()) }
    var permanentLines by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var currentDragWord by remember { mutableStateOf("") }
    var gridSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var currentDragStart by remember { mutableStateOf<Offset?>(null) }
    var currentDragEnd by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("words").get().addOnSuccessListener { res ->
            allWords = res.map { CloudWord(it.id, it.getString("german")?:"?", it.getString("english")?:"?", it.getString("article")?:"", it.getString("category")?:"General") }
        }
    }

    fun nextLevel() {
        foundWordsInLevel.forEach { onWordLearned(it) }
        foundWordsInLevel = emptySet()
        permanentLines = emptyList()
        currentDragWord = ""
        refreshTrigger++
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.Gray) }
            Text("Level ${learnedIds.size / 5 + 1}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = themeColor)
            Row {
                IconButton(onClick = onOpenDictionary) { Icon(Icons.Default.Star, null, tint = themeColor) }
                IconButton(onClick = { nextLevel() }) { Icon(Icons.Default.Refresh, null, tint = themeColor) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (currentWords.isEmpty()) {
            if (allWords.isNotEmpty()) Text("You learned every word!", color = Color.Gray) else Text("Loading...", color = Color.Gray)
        } else {
            OptIn(ExperimentalLayoutApi::class)
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentWords.forEach { word ->
                    val isFound = foundWordsInLevel.contains(word)
                    Surface(modifier = Modifier.padding(horizontal = 4.dp), shape = RoundedCornerShape(50), color = if (isFound) themeColor else Color(0xFFF0F0F0)) {
                        Text(text = word.german, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (isFound) Color.White else Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        Text(text = currentDragWord, style = MaterialTheme.typography.headlineMedium, color = themeColor, fontWeight = FontWeight.Bold, modifier = Modifier.height(40.dp))
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(Color(0xFFFAFAFA))
            .onGloballyPositioned { gridSize = it.size }
            .pointerInput(gridChars) {
                detectDragGestures(
                    onDragStart = { currentDragStart = it; currentDragEnd = it },
                    onDrag = { change, _ -> change.consume(); currentDragEnd = change.position; if (gridSize.width > 0) currentDragWord = getSelectedText(gridChars, gridSize, currentDragStart!!, currentDragEnd!!).first },
                    onDragEnd = {
                        val (selectedText, _) = getSelectedText(gridChars, gridSize, currentDragStart!!, currentDragEnd!!)
                        val match = currentWords.find { simplifyForGrid(it.german) == selectedText }
                        if (match != null && !foundWordsInLevel.contains(match)) {
                            foundWordsInLevel = foundWordsInLevel + match
                            permanentLines = permanentLines + getLineCoords(gridSize, currentDragStart!!, currentDragEnd!!)
                            onSpeak(match.german)
                        }
                        currentDragStart = null; currentDragEnd = null; currentDragWord = ""
                    }
                )
            }) {
            LazyVerticalGrid(columns = GridCells.Fixed(10), userScrollEnabled = false) {
                items(gridChars.size) { index -> Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(1.dp).aspectRatio(1f).background(Color.White, RoundedCornerShape(8.dp))) { Text(text = gridChars[index].toString(), fontWeight = FontWeight.Bold, color = themeColor, fontSize = 18.sp) } }
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / 10f; val cellH = size.height / 10f
                fun getCenter(i: Int) = Offset((i % 10) * cellW + cellW / 2, (i / 10) * cellH + cellH / 2)
                permanentLines.forEach { (s, e) -> drawLine(color = themeColor, start = getCenter(s), end = getCenter(e), strokeWidth = 50f, cap = StrokeCap.Round, alpha = 0.3f) }
                if (currentDragStart != null && currentDragEnd != null) drawLine(color = themeColor, start = currentDragStart!!, end = currentDragEnd!!, strokeWidth = 50f, cap = StrokeCap.Round, alpha = 0.2f)
            }
        }

        if (currentWords.isNotEmpty() && foundWordsInLevel.size == currentWords.size) {
            AlertDialog(
                onDismissRequest = {}, containerColor = Color.White, shape = RoundedCornerShape(20.dp),
                title = { Text("Level Complete! \uD83C\uDF89") },
                text = { Column { currentWords.forEach { Text("✅ ${it.german} = ${it.english}", modifier = Modifier.padding(4.dp)) } } },
                confirmButton = { Button(onClick = { nextLevel() }, colors = ButtonDefaults.buttonColors(containerColor = themeColor)) { Text("Next Level") } }
            )
        }
    }
}

// ==========================================
// 4. DICTIONARY SCREEN
// ==========================================
@Composable
fun DictionaryScreen(color: Color, onBack: () -> Unit) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) { tts = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale.GERMAN } }

    var learnedDisplay by remember { mutableStateOf<List<CloudWord>>(emptyList()) }
    val sharedPref = context.getSharedPreferences("GermanQuestData", Context.MODE_PRIVATE)
    val savedIds = sharedPref.getStringSet("learned_ids", emptySet()) ?: emptySet()

    LaunchedEffect(Unit) {
        if (savedIds.isNotEmpty()) {
            Firebase.firestore.collection("words").get().addOnSuccessListener { res ->
                learnedDisplay = res.map {
                    val cat = it.getString("category") ?: "General"
                    CloudWord(it.id, it.getString("german")?:"", it.getString("english")?:"", it.getString("article")?:"", cat)
                }
                    .filter { savedIds.contains(it.id) || savedIds.contains(it.german) }
            }
        }
    }

    val groupedWords = learnedDisplay.groupBy { it.category }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("My Dictionary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }

        if (savedIds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No words learned yet!", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                groupedWords.forEach { (category, words) ->
                    item {
                        Text(
                            text = category.uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(words) { word ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { tts?.speak(word.german, TextToSpeech.QUEUE_FLUSH, null, null) }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🔊", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("${word.article} ${word.german}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))
                                    Text(word.english, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Functions
fun getSelectedText(grid: List<Char>, size: androidx.compose.ui.unit.IntSize, start: Offset, end: Offset): Pair<String, List<Int>> {
    val cellW = size.width / 10f; val cellH = size.height / 10f
    val r1 = (start.y/cellH).toInt().coerceIn(0,9); val c1 = (start.x/cellW).toInt().coerceIn(0,9)
    val r2 = (end.y/cellH).toInt().coerceIn(0,9); val c2 = (end.x/cellW).toInt().coerceIn(0,9)
    val indices = mutableListOf<Int>(); val sb = StringBuilder()
    val dr = r2 - r1; val dc = c2 - c1; val steps = max(abs(dr), abs(dc))
    if (steps == 0) return Pair("", emptyList())
    val rStep = dr.toFloat() / steps; val cStep = dc.toFloat() / steps
    for (i in 0..steps) {
        val r = kotlin.math.round(r1 + i * rStep).toInt(); val c = kotlin.math.round(c1 + i * cStep).toInt()
        val index = r * 10 + c
        if (index in grid.indices) { indices.add(index); sb.append(grid[index]) }
    }
    return Pair(sb.toString(), indices)
}
fun getLineCoords(size: androidx.compose.ui.unit.IntSize, start: Offset, end: Offset): List<Pair<Int, Int>> {
    val cellW = size.width / 10f; val cellH = size.height / 10f
    val r1 = (start.y/cellH).toInt().coerceIn(0,9); val c1 = (start.x/cellW).toInt().coerceIn(0,9)
    val r2 = (end.y/cellH).toInt().coerceIn(0,9); val c2 = (end.x/cellW).toInt().coerceIn(0,9)
    return listOf((r1*10 + c1) to (r2*10 + c2))
}