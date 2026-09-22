package com.example.echorollv2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.echorollv2.data.local.EchoDatabase
import com.example.echorollv2.data.local.entity.AttendanceRecordEntity
import com.example.echorollv2.data.local.entity.ExamEntity
import com.example.echorollv2.data.local.entity.ExamSubjectEntity
import com.example.echorollv2.data.local.entity.HolidayEntity
import com.example.echorollv2.data.local.entity.RoutineEntity
import com.example.echorollv2.data.local.entity.SubjectEntity
import com.example.echorollv2.data.preferences.UserPreferences
import com.example.echorollv2.data.repository.EchoRepository
import com.example.echorollv2.services.DailyCheckWorker
import com.example.echorollv2.services.NotificationScheduler
import com.example.echorollv2.services.NotificationHelper
import com.example.echorollv2.ui.screens.setup.AddSubjectScreen
import com.example.echorollv2.ui.screens.setup.DaySchedule
import com.example.echorollv2.ui.theme.EchoRollV2Theme
import com.example.echorollv2.ui.theme.ExamPurple
import com.example.echorollv2.ui.theme.HolidayYellow
import com.example.echorollv2.ui.theme.LocalAppColors
import com.example.echorollv2.ui.theme.PrimaryBlue
import com.example.echorollv2.ui.theme.PrimaryGreen
import com.example.echorollv2.ui.theme.PrimaryOrange
import com.example.echorollv2.ui.theme.PrimaryPurple
import com.example.echorollv2.ui.theme.PrimaryRed

import com.example.echorollv2.ui.viewmodels.EchoViewModel
import com.example.echorollv2.ui.viewmodels.EchoViewModelFactory
import com.example.echorollv2.utils.DateTimeUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


val SubjectColors = listOf(
    PrimaryBlue, PrimaryGreen, PrimaryRed, PrimaryOrange, PrimaryPurple,
    Color(0xFF26A69A), Color(0xFFEC407A), Color(0xFFFF5722), Color(0xFFE91E63),
    Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFF4CAF50),
    Color(0xFFFFC107), Color(0xFF673AB7), Color(0xFF03A9F4), Color(0xFFCDDC39)
)

fun getSubjectColor(subjectCode: String): Color {
    val index = kotlin.math.abs(subjectCode.hashCode()) % SubjectColors.size
    return SubjectColors[index]
}

/**
 * Returns the minimum number of future classes that must be attended so that
 * one subsequent missed class still keeps attendance at/above the target.
 *
 * A 100% target can never safely absorb a missed class, so null is returned.
 */
fun classesToSafelyMissOne(
    attended: Int,
    total: Int,
    requiredPercentage: Int
): Int? {
    if (requiredPercentage >= 100) return null
    if (requiredPercentage <= 0) return 0

    val denominator = 100 - requiredPercentage
    val numerator = requiredPercentage * (total + 1) - (attended * 100)

    if (numerator <= 0) return 0

    return (numerator + denominator - 1) / denominator
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        // Register the OS-owned daily scheduling trigger. After this setup,
        // daily notification scheduling does not depend on reopening the app.
        NotificationScheduler.scheduleNextDailyCheck(this)

        val initialDelay = com.example.echorollv2.utils.DateTimeUtils.getDelayUntilNextSixAM()
        val workRequest = PeriodicWorkRequestBuilder<DailyCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyCheck", 
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE, 
            workRequest
        )

        // Trigger an immediate check as well, to ensure today's alarms are set if we just started/updated
        val immediateData = androidx.work.Data.Builder().putBoolean("SILENT_CHECK", true).build()
        val immediateRequest = OneTimeWorkRequestBuilder<DailyCheckWorker>()
            .setInputData(immediateData)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "DailyCheckImmediate",
            androidx.work.ExistingWorkPolicy.REPLACE,
            immediateRequest
        )

        val database = EchoDatabase.getDatabase(this)
        val repository = EchoRepository(database.echoDao())
        val preferences = UserPreferences(this)

        setContent {
            var isDarkMode by androidx.compose.runtime.remember { mutableStateOf(true) }

            // Notification Permission (API 33+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { /* handle */ }
                
                LaunchedEffect(Unit) {
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            EchoRollV2Theme(isDark = isDarkMode) {
                val viewModel: EchoViewModel = viewModel(
                    factory = EchoViewModelFactory(repository, preferences)
                )

                MainScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: EchoViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = androidx.compose.ui.platform.LocalContext.current

    // Trigger update check on startup
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }

    // Update Dialog state
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val latestRelease by viewModel.latestRelease.collectAsState()

    if (updateAvailable && latestRelease != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdate() },
            title = {
                Column {
                    Text("New Update Available!🚀", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Version ${latestRelease!!.tagName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 200.dp)) {
                    Text(latestRelease!!.body, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(latestRelease!!.htmlUrl))
                        context.startActivity(intent)
                        viewModel.dismissUpdate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Update Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdate() }) {
                    Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    var currentScreen by remember { mutableStateOf("Today") }
    
    // Notification Intent Handler
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    LaunchedEffect(activity?.intent) {
        if (activity?.intent?.getBooleanExtra("OPEN_TODAY", false) == true) {
            currentScreen = "Today"
        }
    }

    var navigationStack by remember { mutableStateOf(emptyList<String>()) }
    
    // Scroll States
    val attendanceScrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val todayScrollState = androidx.compose.foundation.lazy.rememberLazyListState()

    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
    var editRoutines by remember { mutableStateOf<List<DaySchedule>>(emptyList()) }
    var selectedSubjectCode by remember { mutableStateOf("") }
    var selectedExamId by remember { mutableStateOf(0) }

    val navigateTo: (String) -> Unit = { screen ->
        navigationStack = navigationStack + currentScreen
        currentScreen = screen
    }

    val goBack: () -> Unit = {
        if (navigationStack.isNotEmpty()) {
            currentScreen = navigationStack.last()
            navigationStack = navigationStack.dropLast(1)
        }
    }

    androidx.activity.compose.BackHandler(enabled = navigationStack.isNotEmpty()) {
        goBack()
    }

    Scaffold(
        bottomBar = {
            if (currentScreen in listOf("Attendance", "Routine", "Today", "Exams", "Holidays", "Settings")) {
                BottomNavigationBar(currentScreen) { 
                    if (it != currentScreen) {
                        navigationStack = emptyList() // Clear stack when switching tabs
                        currentScreen = it 
                    }
                }
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            when (currentScreen) {
                "Today" -> TodayScreen(
                    viewModel = viewModel, 
                    isDarkMode = isDarkMode, 
                    onThemeToggle = onThemeToggle,
                    scrollState = todayScrollState,
                    onNavigateToStickyNotes = { code ->
                        selectedSubjectCode = code
                        navigateTo("StickyNotes")
                    }
                )
                "Attendance" -> AttendanceScreen(
                    viewModel = viewModel,
                    scrollState = attendanceScrollState,
                    onNavigateToAddSubject = { navigateTo("AddSubject") },
                    onEditSubject = { subject ->
                        subjectToEdit = subject
                        editRoutines = viewModel.getRoutinesForSubject(subject.subjectCode).map {
                            DaySchedule(it.dayOfWeek, isEnabled = true, it.startTime, it.endTime)
                        }
                        navigateTo("EditSubject")
                    },
                    onNavigateToStickyNotes = { code ->
                        selectedSubjectCode = code
                        navigateTo("StickyNotes")
                    },
                    onNavigateToSubjectDetail = { code ->
                        selectedSubjectCode = code
                        navigateTo("SubjectDetail")
                    },
                    isDarkMode = isDarkMode,
                    onThemeToggle = onThemeToggle
                )
                "Routine" -> RoutineScreen(viewModel, isDarkMode, onThemeToggle)

                "AddSubject" -> AddSubjectScreen(
                    onNavigateBack = goBack,
                    onSaveSubject = { code, name, category, professor, attended, missed, req, schedule ->
                        viewModel.saveSubjectAndRoutine(
                            code, name, category, professor, attended, missed, req, schedule
                        )
                        // Trigger immediate alarm refresh (SILENT)
                        val silentData = androidx.work.Data.Builder().putBoolean("SILENT_CHECK", true).build()
                        val refreshRequest = OneTimeWorkRequestBuilder<com.example.echorollv2.services.DailyCheckWorker>()
                            .setInputData(silentData)
                            .build()
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "DailyCheckRefresh",
                            androidx.work.ExistingWorkPolicy.REPLACE,
                            refreshRequest
                        )
                        goBack()
                    }
                )
                
                "EditSubject" -> {
                    subjectToEdit?.let { subject ->
                        AddSubjectScreen(
                            onNavigateBack = goBack,
                            onSaveSubject = { code, name, category, professor, attended, missed, req, schedule ->
                                viewModel.saveSubjectAndRoutine(
                                    code, name, category, professor, attended, missed, req, schedule
                                )
                                // Trigger immediate alarm refresh (SILENT)
                                val silentData = androidx.work.Data.Builder().putBoolean("SILENT_CHECK", true).build()
                                val refreshRequest = OneTimeWorkRequestBuilder<com.example.echorollv2.services.DailyCheckWorker>()
                                    .setInputData(silentData)
                                    .build()
                                WorkManager.getInstance(context).enqueueUniqueWork(
                                    "DailyCheckRefresh",
                                    androidx.work.ExistingWorkPolicy.REPLACE,
                                    refreshRequest
                                )
                                goBack()
                            },
                            onDeleteSubject = {
                                viewModel.deleteSubject(it)
                                goBack()
                            },
                            initialSubject = subject,
                            initialRoutines = editRoutines
                        )
                    }
                }

                "StickyNotes" -> StickyNotesScreen(
                    subjectCode = selectedSubjectCode,
                    viewModel = viewModel,
                    onNavigateBack = goBack,
                    onNavigateToAddNote = { navigateTo("AddStickyNote") }
                )

                "AddStickyNote" -> AddStickyNoteScreen(
                    subjectCode = selectedSubjectCode,
                    viewModel = viewModel,
                    onNavigateBack = goBack
                )

                "SubjectDetail" -> SubjectDetailScreen(
                    subjectCode = selectedSubjectCode,
                    viewModel = viewModel,
                    onNavigateBack = goBack
                )

                "Settings" -> com.example.echorollv2.ui.screens.features.SettingsScreen(
                    currentCountryCode = viewModel.countryCode.collectAsState().value ?: "",
                    currentSubdivisionCode = viewModel.subdivisionCode.collectAsState().value ?: "",
                    fetchStatus = viewModel.fetchStatus.collectAsState().value,
                    errorMessage = viewModel.errorMessage.collectAsState().value,
                    onSaveRegion = { country, state -> viewModel.saveRegion(country, state) },
                    onNavigateBack = goBack,
                    onFetchHolidays = { country, state -> 
                        viewModel.fetchHolidays(Calendar.getInstance().get(Calendar.YEAR), country, state) 
                    },
                    onResetFetchStatus = { viewModel.resetFetchStatus() }
                )

                "Holidays" -> com.example.echorollv2.ui.screens.features.HolidaysScreen(
                    holidays = viewModel.allHolidays.collectAsState().value,
                    onNavigateBack = goBack,
                    onDeleteHoliday = { viewModel.deleteHoliday(it) },
                    onDeleteAllHolidays = { viewModel.deleteAllHolidays() },
                    onAddManualHoliday = { date, name -> viewModel.addManualHoliday(date, name) },
                    onUpdateHoliday = { viewModel.saveHoliday(it) }
                )

                "Exams" -> ExamsScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onThemeToggle = onThemeToggle,
                    onExamClick = { id ->
                        selectedExamId = id
                        navigateTo("ExamSubjects")
                    }
                )

                "ExamSubjects" -> ExamSubjectsScreen(
                    examId = selectedExamId,
                    viewModel = viewModel,
                    onNavigateBack = goBack
                )
            }
        }
    }
}

@Composable
fun TodayScreen(
    viewModel: EchoViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onNavigateToStickyNotes: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var selectedDate by remember { mutableStateOf(Date()) }
    
    val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate)
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isEditable = dateFormatted == todayDateStr

    val isFutureDate = remember(selectedDate) {
        val today = Calendar.getInstance().apply {
            time = Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val target = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        target.after(today)
    }
    
    val routines by viewModel.allRoutines.collectAsState()
    val attendanceRecords by viewModel.getAttendanceRecordsForDate(dateFormatted).collectAsState(initial = emptyList())
    val replacements by viewModel.getReplacementsForDate(dateFormatted).collectAsState(initial = emptyList())
    val extraClasses by viewModel.getExtraClassesForDate(dateFormatted).collectAsState(initial = emptyList())
    val subjects by viewModel.allSubjects.collectAsState()
    val allExamSubjects by viewModel.allExamSubjects.collectAsState()
    val allExams by viewModel.allExams.collectAsState()

    val todayExamSubject = allExamSubjects.find { it.examDate == dateFormatted }
    val todayExam = todayExamSubject?.let { es -> allExams.find { it.id == es.examId } }
    val classesHeld = todayExam?.classesHeldDuringExams ?: true

    val todayRoutines = routines.filter { it.dayOfWeek == dayName }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = colors.textPrimary,
                modifier = Modifier.clickable { onThemeToggle() }
            )
            Text("Today's Schedule", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Calendar Row
        CalendarHeader(
            selectedDate = selectedDate, 
            onDateSelected = { selectedDate = it },
            allExamSubjects = allExamSubjects
        )

        Spacer(modifier = Modifier.height(16.dp))

        val allHolidays by viewModel.allHolidays.collectAsState()
        val todayHoliday = allHolidays.find { it.date == dateFormatted }

        if (isEditable) {
            var showAddExtraClass by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterEnd) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showAddExtraClass = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Extra Class", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Text("Extra Class", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showAddExtraClass,
                    onDismissRequest = { showAddExtraClass = false },
                    containerColor = colors.surfaceVariant
                ) {
                    if (subjects.isEmpty()) {
                        DropdownMenuItem(text = { Text("No Subjects", color = colors.textSecondary) }, onClick = { showAddExtraClass = false })
                    } else {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name, color = colors.textPrimary) },
                                onClick = {
                                    showAddExtraClass = false
                                    viewModel.addExtraClass(sub.subjectCode, dateFormatted)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (todayHoliday != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${todayHoliday.name}", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(com.example.echorollv2.utils.HumorUtils.getHolidayMessage(), color = colors.textSecondary, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        } else if (todayRoutines.isEmpty() && extraClasses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No classes scheduled!", color = colors.textSecondary)
            }
        } else if (!classesHeld) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${todayExam?.name ?: "Exam"} Ongoing", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Classes are suspended during this exam.", color = colors.textSecondary, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            val displayItems = remember(todayRoutines, attendanceRecords, replacements, extraClasses) {
                val items = mutableListOf<Triple<RoutineEntity, AttendanceRecordEntity?, String?>>()
                val usedRecordIds = mutableSetOf<Int>()

                todayRoutines.forEach { routine ->
                    val replacement = replacements.find { it.routineId == routine.id }
                    
                    if (replacement != null) {
                        // Original Card (Cancelled)
                        val originalRecord = attendanceRecords.find { 
                            it.routineId == routine.id && 
                            it.subjectCode == routine.subjectCode &&
                            it.id !in usedRecordIds
                        }
                        if (originalRecord != null) usedRecordIds.add(originalRecord.id)
                        items.add(Triple(routine, originalRecord, null))

                        // Replacement Card
                        val replacementRecord = attendanceRecords.find {
                            it.routineId == routine.id &&
                            it.subjectCode == replacement.replacementSubjectCode &&
                            it.id !in usedRecordIds
                        }
                        if (replacementRecord != null) usedRecordIds.add(replacementRecord.id)
                        items.add(Triple(routine, replacementRecord, replacement.replacementSubjectCode))
                    } else {
                        // Regular Card
                        var record = attendanceRecords.find { it.routineId == routine.id && it.id !in usedRecordIds }

                        if (record == null) {
                            record = attendanceRecords.find {
                                it.subjectCode == routine.subjectCode &&
                                it.routineId == -1 &&
                                it.id !in usedRecordIds
                            }
                        }

                        if (record != null) {
                            usedRecordIds.add(record.id)
                        }
                        items.add(Triple(routine, record, null))
                    }
                }

                // Add Extra Classes
                extraClasses.forEach { extraClass ->
                    val pseudoRoutine = RoutineEntity(
                        id = -(extraClass.id),
                        subjectCode = extraClass.subjectCode,
                        dayOfWeek = dayName,
                        startTime = "Extra",
                        endTime = "Class"
                    )
                    
                    val record = attendanceRecords.find { 
                        it.routineId == pseudoRoutine.id 
                    }
                    if (record != null) usedRecordIds.add(record.id)
                    items.add(Triple(pseudoRoutine, record, null))
                }

                // Sorting
                val allMarked = items.all { it.second != null }
                if (allMarked) {
                    items.sortedBy { DateTimeUtils.timeToMinutes(it.first.startTime) }
                } else {
                    val unmarked = items.filter { it.second == null }.sortedBy { DateTimeUtils.timeToMinutes(it.first.startTime) }
                    val marked = items.filter { it.second != null }.sortedBy { DateTimeUtils.timeToMinutes(it.first.startTime) }
                    unmarked + marked
                }
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayItems) { (routine, record, replacementCode) ->
                    val subject = subjects.find { it.subjectCode == (replacementCode ?: routine.subjectCode) }
                    
                    if (subject != null) {
                        TodayClassCard(
                            subject = subject,
                            routine = routine,
                            attendanceStatus = record?.status,
                            isEditable = isEditable,
                            isFuture = isFutureDate,
                            allSubjects = subjects,
                            isReplacement = replacementCode != null,
                            hasReplacement = replacements.any { it.routineId == routine.id && replacementCode == null },
                            onMarkAttendance = { status ->
                                viewModel.markAttendance(routine, status, replacementCode)
                            },
                            onReplace = { newCode ->
                                viewModel.replaceClass(routine, dateFormatted, newCode)
                            },
                            onUndoReplace = {
                                viewModel.undoReplacement(routine, dateFormatted)
                            },
                            onStickyNotesClick = {
                                onNavigateToStickyNotes(subject.subjectCode)
                            },
                            onDeleteExtraClass = if (routine.id < 0) {
                                {
                                    val extraClassFind = extraClasses.find { it.id == -(routine.id) }
                                    if (extraClassFind != null) {
                                        viewModel.deleteExtraClass(extraClassFind)
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: Date, 
    onDateSelected: (Date) -> Unit,
    allExamSubjects: List<ExamSubjectEntity> = emptyList()
) {
    val colors = LocalAppColors.current
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    val dates = remember {
        val list = java.util.ArrayList<Date>(14601)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7300) // approx 20 years back
        for (i in 0..14600) {
            list.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = 7300 - 3)

    val currentMonthYear by remember {
        androidx.compose.runtime.derivedStateOf {
            val idx = listState.firstVisibleItemIndex + 2
            val safeIdx = idx.coerceIn(0, dates.lastIndex.coerceAtLeast(0))
            val d = if (dates.isNotEmpty()) dates[safeIdx] else selectedDate
            monthYearFormat.format(d)
        }
    }

    Surface(
        color = colors.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = currentMonthYear, 
                color = colors.textPrimary, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.fillMaxWidth(), 
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.foundation.lazy.LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dates) { date ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                    val isSelected = dateStr == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                    val isToday = dateStr == todayDateStr
                    val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(date)
                    val dayNameStr = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                    val isExamDate = allExamSubjects.any { it.examDate == dateStr }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onDateSelected(date) }.padding(horizontal = 4.dp)
                    ) {
                        if (isSelected) {
                            val bgColor = if (isToday) PrimaryBlue else PrimaryGreen
                            Box(
                                modifier = Modifier.size(40.dp).background(bgColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dayNum, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dayNameStr, color = if (isToday) PrimaryBlue else PrimaryGreen, fontSize = 12.sp)
                        } else {
                            val textColor = if (isExamDate) ExamPurple else if (isToday) PrimaryBlue else colors.textPrimary
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .then(if (isToday) Modifier.background(PrimaryBlue.copy(alpha = 0.2f), CircleShape) else Modifier)
                                    .then(if (isExamDate) Modifier.border(1.dp, ExamPurple, CircleShape) else if (isToday) Modifier.border(1.dp, PrimaryBlue, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dayNum, color = textColor, fontSize = 16.sp, fontWeight = if (isExamDate || isToday) FontWeight.Bold else FontWeight.Normal)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dayNameStr, color = if (isExamDate) ExamPurple else if (isToday) PrimaryBlue else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayClassCard(
    subject: SubjectEntity,
    routine: RoutineEntity,
    attendanceStatus: String?,
    isEditable: Boolean,
    isFuture: Boolean,
    allSubjects: List<SubjectEntity>,
    isReplacement: Boolean = false,
    hasReplacement: Boolean = false,
    onReplace: (String) -> Unit,
    onUndoReplace: () -> Unit,
    onMarkAttendance: (String) -> Unit,
    onStickyNotesClick: () -> Unit,
    onDeleteExtraClass: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val attended = subject.attended
    val missed = subject.missed
    val total = attended + missed
    val req = subject.requiredPercentage
    val percentage = if (total > 0) (attended * 100 / total) else 100

    var statusText = ""
    var statusColor = Color.White
    
    if (total == 0) {
        statusText = "No classes yet"
        statusColor = PrimaryBlue
    } else {
        val currentPct = (attended.toDouble() / total.toDouble()) * 100.0
        if (currentPct >= req) {
            var canMiss = 0
            while (((attended.toDouble() / (total + canMiss + 1).toDouble()) * 100.0) >= req) {
                canMiss++
            }
            if (canMiss > 0) {
                statusText = "Can miss $canMiss class${if (canMiss > 1) "es" else ""}"
                statusColor = Color(0xFF8BC34A)
            } else {
                val moreToAttend = classesToSafelyMissOne(attended, total, req)
                statusText = if (moreToAttend != null) {
                    "Attend $moreToAttend more class${if (moreToAttend == 1) "" else "es"} to safely miss 1 class"
                } else {
                    "Cannot safely miss 1 class"
                }
                statusColor = Color(0xFFF39C12)
            }
        } else {
            var mustAttend = 0
            while ((((attended + mustAttend).toDouble() / (total + mustAttend).toDouble()) * 100.0) < req) {
                mustAttend++
            }
            statusText = "Attend $mustAttend class${if (mustAttend > 1) "es" else ""}"
            statusColor = Color(0xFFEA4335)
        }
    }

    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Progress Circle
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(
                        progress = { percentage.toFloat() / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryBlue,
                        strokeWidth = 4.dp,
                        trackColor = PrimaryRed
                    )
                    Text(if (total == 0) "N/A" else "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(subject.subjectCode, color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(subject.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (subject.professorName.isNotEmpty()) {
                                Text(subject.professorName, color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isEditable && !hasReplacement) {
                                var showMenu by remember { mutableStateOf(false) }
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Replace",
                                        tint = PrimaryBlue,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showMenu = true }
                                    )
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        containerColor = colors.surfaceVariant
                                    ) {
                                        if (isReplacement) {
                                            DropdownMenuItem(
                                                text = { Text("Undo Replacement", color = PrimaryRed) },
                                                onClick = {
                                                    showMenu = false
                                                    onUndoReplace()
                                                }
                                            )
                                        } else {
                                            Text(
                                                "Replace with:",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            allSubjects.filter { it.subjectCode != subject.subjectCode }.forEach { other ->
                                                DropdownMenuItem(
                                                    text = { Text(other.name, color = colors.textPrimary) },
                                                    onClick = {
                                                        showMenu = false
                                                        onReplace(other.subjectCode)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.StickyNote2, 
                                contentDescription = "Sticky Notes", 
                                tint = PrimaryOrange, 
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onStickyNotesClick() }
                            )
                            if (onDeleteExtraClass != null) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Extra Class",
                                    tint = PrimaryRed,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onDeleteExtraClass.invoke() }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isReplacement) {
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "REPLACEMENT",
                                    color = PrimaryBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else if (onDeleteExtraClass != null) {
                            Surface(
                                color = PrimaryGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "EXTRA CLASS",
                                    color = PrimaryGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Attended: $attended", color = PrimaryBlue, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Missed: $missed", color = PrimaryRed, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Req.: ${req}%", color = colors.textSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (attendanceStatus == null) {
                if (isEditable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttendanceButton("Present", PrimaryGreen, Modifier.weight(1f)) { onMarkAttendance("Present") }
                        AttendanceButton("Absent", PrimaryRed, Modifier.weight(1f)) { onMarkAttendance("Absent") }
                        AttendanceButton("Cancelled", colors.textSecondary, Modifier.weight(1f)) { onMarkAttendance("Cancelled") }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(45.dp).background(colors.surfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isFuture) "Unlocks on this day" else "Not Marked", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            } else {
                val color = when (attendanceStatus) {
                    "Present" -> PrimaryGreen
                    "Absent" -> PrimaryRed
                    else -> colors.textSecondary
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, color, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(attendanceStatus, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AttendanceButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        modifier = modifier.height(45.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StickyNotesScreen(
    subjectCode: String,
    viewModel: EchoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddNote: () -> Unit
) {
    val colors = LocalAppColors.current
    val notes by viewModel.getStickyNotesForSubject(subjectCode).collectAsState(initial = emptyList())
    var showDeleteSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Text("Sticky Notes", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNavigateToAddNote) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = colors.textPrimary, modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = colors.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                         // Drawing overlapping squares to mimic the icons
                         Box(modifier = Modifier.size(80.dp).background(Color(0xFFE91E63), RoundedCornerShape(8.dp)))
                         Box(modifier = Modifier.size(80.dp).padding(start = 20.dp, top = 20.dp).background(Color(0xFFFFC107), RoundedCornerShape(8.dp)))
                         Box(modifier = Modifier.size(80.dp).padding(start = 40.dp, top = 40.dp).background(Color(0xFF8BC34A), RoundedCornerShape(8.dp)))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Capture your thoughts before\nthey fly away!", color = colors.textPrimary, textAlign = TextAlign.Center, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToAddNote,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get started", color = Color.White)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notes) { note ->
                        Surface(
                            color = colors.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier.width(180.dp).height(180.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Column {
                                    Text(note.title, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(note.description, color = colors.textSecondary, fontSize = 14.sp)
                                }
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = PrimaryRed,
                                    modifier = Modifier.align(Alignment.BottomEnd).clickable {
                                        viewModel.deleteStickyNote(note)
                                        showDeleteSuccess = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            if (showDeleteSuccess) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
                    color = colors.background,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sticky note deleted successfully!", color = colors.textPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    }
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showDeleteSuccess = false
                }
            }
        }
    }
}

@Composable
fun AddStickyNoteScreen(
    subjectCode: String,
    viewModel: EchoViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalAppColors.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Text("Add Sticky Note", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text("Enter title", color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title", color = Color.DarkGray) },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column {
                Text("Enter description", color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Description", color = Color.DarkGray) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        viewModel.saveStickyNote(subjectCode, title, description)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)) // Mimicking the add list icon
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: String, onScreenSelected: (String) -> Unit) {
    val colors = LocalAppColors.current
    val items = listOf(
        NavigationItem("Attendance", Icons.Default.BarChart),
        NavigationItem("Today", Icons.Default.Today),
        NavigationItem("Routine", Icons.Outlined.CalendarMonth),
        NavigationItem("Exams", Icons.Default.MenuBook),
        NavigationItem("Holidays", Icons.Default.Celebration),
        NavigationItem("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = colors.background,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.title,
                onClick = { onScreenSelected(item.title) },
                icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp)) },
                label = { Text(item.title, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, softWrap = false) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4A90E2),
                    selectedTextColor = Color(0xFF4A90E2),
                    unselectedIconColor = Color(0xFF555555),
                    unselectedTextColor = Color(0xFF555555),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class NavigationItem(val title: String, val icon: ImageVector)

@Composable
fun AttendanceScreen(
    viewModel: EchoViewModel,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onNavigateToAddSubject: () -> Unit,
    onEditSubject: (SubjectEntity) -> Unit,
    onNavigateToStickyNotes: (String) -> Unit,
    onNavigateToSubjectDetail: (String) -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    val selectedTab by viewModel.selectedAttendanceTab.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()

    val displayDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())
    val todayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp).clickable { onThemeToggle() }
            )
            Text("Attendance Tracker", fontSize = 20.sp, fontWeight = FontWeight.Normal, color = colors.textPrimary)
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "Add",
                tint = colors.textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigateToAddSubject() }
            )
        }

        // Date Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text(
                    displayDate,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text(
                    todayName,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theory/Lab Toggle
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
        ) {
            listOf("Theory", "Lab").forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (selectedTab == tab) Color(0xFF444444) else Color.Transparent)
                        .clickable { viewModel.setAttendanceTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab, color = colors.textPrimary, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Empty",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Your presence matters more\nthan you think!",
                        color = colors.textPrimary, fontSize = 16.sp, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onNavigateToAddSubject() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get started", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            ) {
                items(subjects.filter { it.category == selectedTab }) { subject ->
                    SubjectCard(
                        subject = subject, 
                        onEdit = { onEditSubject(subject) },
                        onDelete = { viewModel.deleteSubject(subject) },
                        onStickyNotesClick = { onNavigateToStickyNotes(subject.subjectCode) },
                        onCalendarClick = { onNavigateToSubjectDetail(subject.subjectCode) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCard(
    subject: SubjectEntity, 
    onEdit: () -> Unit, 
    onDelete: () -> Unit,
    onStickyNotesClick: () -> Unit, 
    onCalendarClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val attended = subject.attended
    val missed = subject.missed
    val total = attended + missed
    val req = subject.requiredPercentage
    
    val percentage = if (total > 0) (attended * 100 / total) else 100
    
    // Status Logic
    var statusText = ""
    var statusColor = Color.White
    
    if (total == 0) {
        statusText = "No classes yet"
        statusColor = PrimaryBlue
    } else {
        val currentPct = (attended.toDouble() / total.toDouble()) * 100.0
        if (currentPct >= req) {
            // Can miss logic
            var canMiss = 0
            while (((attended.toDouble() / (total + canMiss + 1).toDouble()) * 100.0) >= req) {
                canMiss++
            }
            if (canMiss > 0) {
                statusText = "Can miss $canMiss class${if (canMiss > 1) "es" else ""}"
                statusColor = Color(0xFF8BC34A) // Greenish
            } else {
                val moreToAttend = classesToSafelyMissOne(attended, total, req)
                statusText = if (moreToAttend != null) {
                    "Attend $moreToAttend more class${if (moreToAttend == 1) "" else "es"} to safely miss 1 class"
                } else {
                    "Cannot safely miss 1 class"
                }
                statusColor = Color(0xFFF39C12) // Orange
            }
        } else {
            // Must attend logic
            var mustAttend = 0
            while ((((attended + mustAttend).toDouble() / (total + mustAttend).toDouble()) * 100.0) < req) {
                mustAttend++
            }
            statusText = "Attend $mustAttend class${if (mustAttend > 1) "es" else ""}"
            statusColor = Color(0xFFEA4335) // Red
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            false
        }
    )

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.cardBackground,
            title = {
                Text(text = "Are you sure?", color = Color.White, fontSize = 20.sp)
            },
            text = {
                Text("Do you want to delete this?", color = Color.White)
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Yes", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No", color = Color.White)
                }
            }
        )
    }

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = Color(0xFF8B0000)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(color)
                    .padding(24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        content = {
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Progress Circle
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                    CircularProgressIndicator(
                        progress = { percentage.toFloat() / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF4A90E2),
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFFEA4335)
                    )
                    Text(if (total == 0) "N/A" else "$percentage%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(subject.subjectCode, color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                subject.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (subject.professorName.isNotEmpty()) {
                                Text(subject.professorName, color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                        Row {
                            Icon(
                                Icons.Default.StickyNote2, 
                                contentDescription = "Sticky Notes", 
                                tint = PrimaryOrange, 
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onStickyNotesClick() }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                Icons.Default.CalendarToday, 
                                contentDescription = "Calendar", 
                                tint = PrimaryGreen, 
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onCalendarClick() }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                Icons.Default.Edit, 
                                contentDescription = "Edit", 
                                tint = Color(0xFF4A90E2), 
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onEdit() }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Surface(
                            color = colors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Attended: ${subject.attended}",
                                color = Color(0xFF4A90E2),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = colors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Missed: ${subject.missed}",
                                color = Color(0xFFEA4335),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = colors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Req.: ${subject.requiredPercentage} %",
                                color = Color(0xFF555555),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 86.dp)
            )
        }
    }
    })
}

@Composable
fun RoutineScreen(
    viewModel: EchoViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val displayDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val routines by viewModel.allRoutines.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()

    // Extract all unique start times from the database to make it dynamic
    val timeSlots = remember(routines) {
        routines.map { it.startTime }
            .distinct()
            .sortedBy { DateTimeUtils.timeToMinutes(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = colors.textPrimary,
                modifier = Modifier.clickable { onThemeToggle() }
            )
            Text("Routine", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        val horizontalScrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
            // Day Headers
            Row(modifier = Modifier.background(Color.Transparent)) {
                Box(modifier = Modifier.width(80.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Time", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                displayDays.forEach { day ->
                    Box(modifier = Modifier.width(100.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(day, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (timeSlots.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No routine set. Add subjects with schedule first.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                // Dynamic Time Grid
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(timeSlots) { time ->
                        val currentMinutes = DateTimeUtils.timeToMinutes(time)
                        Row(modifier = Modifier.height(80.dp)) {
                            // Dynamic Time Column
                            Box(
                                modifier = Modifier.width(80.dp).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(time, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }

                            // Subject Slots
                            days.forEach { day ->
                                // Find any routine that covers this specific time slot
                                val coveringRoutine = routines.find { routine ->
                                    routine.dayOfWeek == day &&
                                    DateTimeUtils.timeToMinutes(routine.startTime) <= currentMinutes &&
                                    DateTimeUtils.timeToMinutes(routine.endTime) > currentMinutes
                                }

                                val subject = coveringRoutine?.let { r -> subjects.find { it.subjectCode == r.subjectCode } }

                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .border(0.5.dp, Color.DarkGray.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (subject != null) {
                                        Surface(
                                            color = getSubjectColor(subject.subjectCode),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                                Text(
                                                    text = subject.name,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 3
                                                )
                                            }
                                        }
                                    } else {
                                        // Empty slot
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(subjectCode: String, viewModel: EchoViewModel, onNavigateBack: () -> Unit) {
    val colors = LocalAppColors.current
    val subjects by viewModel.allSubjects.collectAsState()
    val subject = subjects.find { it.subjectCode == subjectCode } ?: return
    
    val allRecords by viewModel.getAllAttendanceRecordsForSubject(subjectCode).collectAsState(initial = emptyList())
    
    var showMarkingSheet by remember { mutableStateOf(false) }
    var selectedDateForMarking by remember { mutableStateOf<Date?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(subject.name, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            // Stats Row
            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem("${subject.attended + subject.missed}", "Total", colors.textPrimary)
                    StatItem("${subject.attended}", "Attended", PrimaryGreen)
                    StatItem("${subject.missed}", "Missed", PrimaryRed)
                    StatItem("${allRecords.count { it.status == "Cancelled" }}", "Cancelled", colors.textSecondary)
                    
                    val pct = if (subject.attended + subject.missed > 0) 
                        (subject.attended * 100 / (subject.attended + subject.missed)) 
                        else 100
                    StatItem("$pct%", "Current", PrimaryGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Attendance Activity", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            // Legend
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("Present", PrimaryGreen)
                LegendItem("Absent", PrimaryRed)
                LegendItem("Cancelled", colors.textSecondary)
                LegendItem("Holiday", HolidayYellow)
                LegendItem("Exam", ExamPurple)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tap on date to enter attendance", 
                color = colors.textSecondary, 
                fontSize = 12.sp, 
                modifier = Modifier.fillMaxWidth(), 
                textAlign = TextAlign.Center
            )

            val allHolidays by viewModel.allHolidays.collectAsState()
            val routinesForSubject = remember(subjectCode, viewModel.allRoutines.collectAsState().value) {
                viewModel.allRoutines.value.filter { it.subjectCode == subjectCode }
            }

            val context = androidx.compose.ui.platform.LocalContext.current

            // Calendar Grid
            AttendanceCalendarGrid(
                records = allRecords,
                holidays = allHolidays,
                routines = routinesForSubject,
                allExams = viewModel.allExams.collectAsState().value,
                allExamSubjects = viewModel.allExamSubjects.collectAsState().value,
                onDateClick = { date ->
                    selectedDateForMarking = date
                    showMarkingSheet = true
                },
                onMarkingRestricted = { message ->
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showMarkingSheet && selectedDateForMarking != null) {
        ModalBottomSheet(
            onDismissRequest = { showMarkingSheet = false },
            sheetState = sheetState,
            containerColor = colors.surfaceVariant,
            dragHandle = { Spacer(modifier = Modifier.height(24.dp)) }
        ) {
            AttendanceMarkingSheet(
                date = selectedDateForMarking!!,
                existingRecord = allRecords.find { 
                    it.date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDateForMarking!!)
                },
                onSave = { isOffDay, attendedCount, missedCount ->
                    viewModel.updateManualAttendance(subjectCode, selectedDateForMarking!!, isOffDay, attendedCount, missedCount)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showMarkingSheet = false
                    }
                },
                onCancel = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showMarkingSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = colors.textSecondary, fontSize = 10.sp)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = colors.textSecondary, fontSize = 12.sp)
    }
}

@Composable
fun AttendanceCalendarGrid(
    records: List<AttendanceRecordEntity>,
    holidays: List<HolidayEntity>,
    routines: List<RoutineEntity>,
    allExams: List<ExamEntity>,
    allExamSubjects: List<ExamSubjectEntity>,
    onDateClick: (Date) -> Unit,
    onMarkingRestricted: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 1200,
        pageCount = { 2400 }
    )

    androidx.compose.foundation.pager.HorizontalPager(state = pagerState) { page ->
        val monthOffset = page - 1200
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, monthOffset)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dates = remember(monthOffset) {
            val list = mutableListOf<Date?>()
            // Add padding for start of month
            repeat(firstDayOfWeek - 1) { list.add(null) }
            // Add actual dates
            for (i in 1..daysInMonth) {
                val c = calendar.clone() as Calendar
                c.set(Calendar.DAY_OF_MONTH, i)
                list.add(c.time)
            }
            list
        }

        val rows = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Header
                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                Text(monthName, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                // Day Labels horizontal
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rows.forEach {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(it, color = colors.textPrimary, fontSize = 12.sp)
                        }
                    }
                }

                // Grid of dates
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(dates) { date ->
                        if (date != null) {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                            val dayRecords = records.filter { it.date == dateStr }
                            val attendanceCount = dayRecords.count {
                                it.status == "Present" || it.status == "Absent"
                            }
                            val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(date)
                            val dayNameFull = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                            
                            val isHoliday = holidays.any { it.date == dateStr }
                            val examSubject = allExamSubjects.find { it.examDate == dateStr }
                            val exam = examSubject?.let { es -> allExams.find { it.id == es.examId } }
                            val isExamDay = examSubject != null
                            val classesHeldDuringExam = exam?.classesHeldDuringExams ?: true
                            
                            // Date Comparison for Future
                            val todayCal = Calendar.getInstance().apply {
                                time = Date()
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val targetCal = Calendar.getInstance().apply {
                                time = date
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val isFuture = targetCal.after(todayCal)
                            val isClassDay = routines.any { it.dayOfWeek == dayNameFull }

                            val bgColor = if (isHoliday) {
                                HolidayYellow
                            } else if (isExamDay) {
                                ExamPurple
                            } else if (dayRecords.any { it.status == "Cancelled" }) {
                                colors.textSecondary
                            } else if (dayRecords.any { it.status == "Present" }) {
                                PrimaryGreen
                            } else if (dayRecords.any { it.status == "Absent" }) {
                                PrimaryRed
                            } else if (isFuture) {
                                Color.DarkGray // Locked future color
                            } else {
                                colors.surfaceVariant
                            }
                            
                            val contentAlpha = if (isFuture || (!isClassDay && !isHoliday && !isExamDay && dayRecords.isEmpty())) 0.5f else 1.0f

                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            bgColor.copy(alpha = contentAlpha),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            if (isFuture) {
                                                onMarkingRestricted("Attendance cannot be marked for future dates!")
                                            } else if (isHoliday) {
                                                onMarkingRestricted("Attendance cannot be marked on holidays!")
                                            } else if (isExamDay && !classesHeldDuringExam) {
                                                onMarkingRestricted("Classes suspended due to ${exam?.name ?: "Exam"}")
                                            } else if (!isClassDay && dayRecords.isEmpty()) {
                                                onMarkingRestricted("No class scheduled for $dayNameFull")
                                            } else {
                                                onDateClick(date)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        dayOfMonth,
                                        color = (if (isHoliday) Color.Black else colors.textPrimary).copy(alpha = contentAlpha),
                                        fontSize = 12.sp
                                    )
                                }

                                if (attendanceCount > 0) {
                                    Surface(
                                        color = colors.surfaceVariant.copy(alpha = contentAlpha),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                    ) {
                                        Text(
                                            "$attendanceCount",
                                            color = colors.textPrimary.copy(alpha = contentAlpha),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceMarkingSheet(
    date: Date,
    existingRecord: AttendanceRecordEntity?,
    onSave: (Boolean, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalAppColors.current
    var isOffDay by remember { mutableStateOf(existingRecord?.status == "Cancelled") }
    var selectedStatus by remember { 
        mutableStateOf(
            if (existingRecord?.status == "Absent") "Absent" else "Present"
        ) 
    }

    val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

    Column(
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(dayName, color = colors.textSecondary, fontSize = 14.sp)
        Text(dateFormatted, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mark as Cancelled", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            Switch(
                checked = isOffDay, 
                onCheckedChange = { isOffDay = it },
                colors = SwitchDefaults.colors(checkedThumbColor = colors.textPrimary, checkedTrackColor = PrimaryBlue)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val presentColor = if (isOffDay) Color.DarkGray else if (selectedStatus == "Present") PrimaryGreen else Color.Gray
            val absentColor = if (isOffDay) Color.DarkGray else if (selectedStatus == "Absent") PrimaryRed else Color.Gray

            AttendanceButton(
                text = "Present", 
                color = presentColor, 
                modifier = Modifier.weight(1f)
            ) {
                if (!isOffDay) selectedStatus = "Present"
            }

            AttendanceButton(
                text = "Absent", 
                color = absentColor, 
                modifier = Modifier.weight(1f)
            ) {
                if (!isOffDay) selectedStatus = "Absent"
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Cancel", color = colors.textSecondary)
            }
            Button(
                onClick = {
                    val attended = if (!isOffDay && selectedStatus == "Present") 1 else 0
                    val missed = if (!isOffDay && selectedStatus == "Absent") 1 else 0
                    onSave(isOffDay, attended, missed)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Save", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExamsScreen(
    viewModel: EchoViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onExamClick: (Int) -> Unit
) {
    val colors = LocalAppColors.current
    val exams by viewModel.allExams.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<com.example.echorollv2.data.local.entity.ExamEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = colors.textPrimary,
                modifier = Modifier.clickable { onThemeToggle() }
            )
            Text("Exams", color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "Add Exam",
                tint = colors.textPrimary,
                modifier = Modifier.size(28.dp).clickable { 
                    examToEdit = null
                    showAddDialog = true 
                }
            )
        }

        if (exams.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No exams added yet!", color = colors.textSecondary)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(exams) { exam ->
                    ExamCard(
                        exam = exam,
                        onClick = { onExamClick(exam.id) },
                        onEdit = { 
                            examToEdit = exam
                            showAddDialog = true 
                        },
                        onDelete = { viewModel.deleteExam(exam) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExamDialog(
            initialExam = examToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { name, classesHeld ->
                viewModel.saveExam(name, classesHeld, examToEdit?.id ?: 0)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExamCard(
    exam: com.example.echorollv2.data.local.entity.ExamEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        onClick = onClick,
        color = colors.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(160.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Icon(Icons.Default.Edit, "Edit", tint = PrimaryBlue, modifier = Modifier.size(18.dp).clickable { onEdit() })
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Delete, "Delete", tint = PrimaryRed, modifier = Modifier.size(18.dp).clickable { onDelete() })
            }
            Text(exam.name, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (exam.classesHeldDuringExams) Icons.Default.Check else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (exam.classesHeldDuringExams) PrimaryGreen else PrimaryOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (exam.classesHeldDuringExams) "Classes Active" else "Classes Suspended",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ExamSubjectsScreen(
    examId: Int,
    viewModel: EchoViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val exams by viewModel.allExams.collectAsState()
    val exam = exams.find { it.id == examId } ?: return
    val examSubjects by viewModel.allExamSubjects.collectAsState()
    val subjects = examSubjects.filter { it.examId == examId }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<com.example.echorollv2.data.local.entity.ExamSubjectEntity?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(exam.name, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(if (exam.classesHeldDuringExams) "Classes Active" else "Classes Suspended", color = colors.textSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Add Subject",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(28.dp).clickable { 
                        subjectToEdit = null
                        showAddDialog = true 
                    }
                )
            }
        },
        containerColor = colors.background
    ) { padding ->
        if (subjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No subjects added to this exam.", color = colors.textSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subjects) { subject ->
                    ExamSubjectCard(
                        subject = subject,
                        onEdit = {
                            subjectToEdit = subject
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteExamSubject(subject) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddExamSubjectDialog(
            examId = examId,
            initialSubject = subjectToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { code, name, date, marks, note ->
                viewModel.saveExamSubject(examId, code, name, date, marks, note, subjectToEdit?.id ?: 0)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExamSubjectCard(
    subject: com.example.echorollv2.data.local.entity.ExamSubjectEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val stickyNoteColor = remember(subject.subjectCode) {
        val hash = subject.subjectCode.hashCode()
        val colors = listOf(
            Color(0xFFFFF9C4), // Light Yellow
            Color(0xFFF1F8E1), // Light Green
            Color(0xFFE1F5FE), // Light Blue
            Color(0xFFF3E5F5), // Light Purple
            Color(0xFFFFF3E0)  // Light Orange
        )
        colors[kotlin.math.abs(hash % colors.size)]
    }

    Surface(
        color = stickyNoteColor,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
    ) {
        // Sticky Note Aesthetic
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(subject.subjectCode, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row {
                        Icon(Icons.Default.Edit, "Edit", tint = Color.DarkGray, modifier = Modifier.size(18.dp).clickable { onEdit() })
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Delete, "Delete", tint = Color.DarkGray, modifier = Modifier.size(18.dp).clickable { onDelete() })
                    }
                }
                Text(subject.subjectName, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val prettyDate = try {
                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(subject.examDate)
                        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d!!)
                    } catch (e: Exception) { subject.examDate }
                    Text(prettyDate, color = Color.DarkGray, fontSize = 13.sp)
                }

                if (subject.marksScored.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Score: ${subject.marksScored}", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (subject.stickyNote.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(subject.stickyNote, color = Color.Black, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    initialExam: com.example.echorollv2.data.local.entity.ExamEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    var name by remember { mutableStateOf(initialExam?.name ?: "") }
    var classesHeld by remember { mutableStateOf(initialExam?.classesHeldDuringExams ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExam == null) "New Exam" else "Edit Exam", color = colors.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exam Name (e.g. Mid Term)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Classes will be held", color = colors.textPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = classesHeld, 
                        onCheckedChange = { classesHeld = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.textPrimary, checkedTrackColor = PrimaryBlue)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, classesHeld) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save", color = colors.textPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        containerColor = colors.surfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamSubjectDialog(
    examId: Int,
    initialSubject: com.example.echorollv2.data.local.entity.ExamSubjectEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    val colors = LocalAppColors.current
    var code by remember { mutableStateOf(initialSubject?.subjectCode ?: "") }
    var name by remember { mutableStateOf(initialSubject?.subjectName ?: "") }
    var date by remember { mutableStateOf(initialSubject?.examDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var marks by remember { mutableStateOf(initialSubject?.marksScored ?: "") }
    var note by remember { mutableStateOf(initialSubject?.stickyNote ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSubject == null) "Add Subject to Exam" else "Edit Exam Subject", color = colors.textPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    label = { Text("Subject Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date Picker Button
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    color = Color.Transparent
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = colors.textSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(date.ifEmpty { "Select Date" }, color = colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = marks, onValueChange = { marks = it },
                    label = { Text("Marks Scored (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Sticky Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (code.isNotBlank() && name.isNotBlank()) onSave(code, name, date, marks, note) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save", color = colors.textPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        containerColor = colors.surfaceVariant
    )

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState()
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}
