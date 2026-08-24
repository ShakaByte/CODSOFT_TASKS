package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.theme.ToDoListTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.composable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.material3.DatePickerDefaults
import kotlinx.coroutines.flow.first

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userName: String = "",
    val title: String,
    val description: String = "",
    val dueDate: String = "",
    val priority: Priority = Priority.LOW,
    val isDone: Boolean = false
)

enum class Priority {
    LOW, MEDIUM, HIGH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoListTheme {
                val context = LocalContext.current
                val db = remember { AppDatabase.getDatabase(context) }
                val taskDao = db.taskDao()
                var userName by remember { mutableStateOf("") }
                val tasks by taskDao.getTasksForUser(userName).collectAsState(initial = emptyList())
                val navController = rememberNavController()
                var editingTask by remember { mutableStateOf<Task?>(null) }
                var startDestination by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                var snackbarIsError by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                fun showMessage(message: String, isError: Boolean = false) {
                    snackbarIsError = isError
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
                LaunchedEffect(Unit) {
                    val savedName = UserPreferences.getName(context).first()
                    val onboardingDone = UserPreferences.isOnboardingDone(context).first()
                    userName = savedName
                    startDestination = if (savedName.isNotBlank() && onboardingDone) "home" else "welcome"
                }

                if (startDestination == null) {

                    return@ToDoListTheme
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = if (snackbarIsError) Color(0xFFC62828) else Color(0xFF2E7D32),
                                contentColor = Color.White
                            )
                        }
                    }
                ) { outerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!,
                        modifier = Modifier.padding(outerPadding)
                    ) {
                        composable("welcome") {
                            WelcomeScreen(onGetStarted = { name ->
                                userName = name
                                scope.launch {
                                    UserPreferences.saveName(context, name)
                                    val alreadyOnboarded = UserPreferences.isOnboardingDone(context).first()
                                    if (alreadyOnboarded) {
                                        navController.navigate("home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("purpose")
                                    }
                                }
                            })
                        }

                        composable("purpose") {
                            PurposeScreen(onGetStarted = {
                                scope.launch {
                                    UserPreferences.setOnboardingDone(context, true)
                                }
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            })
                        }

                        composable("home") {
                            HomeScreen(
                                userName = userName,
                                tasks = tasks,
                                onAddTask = {
                                    editingTask = null
                                    navController.navigate("addTask")
                                },
                                onEdit = { task ->
                                    editingTask = task
                                    navController.navigate("addTask")
                                },
                                onToggleDone = { task ->
                                    scope.launch {
                                        taskDao.updateTask(task.copy(isDone = !task.isDone))
                                        showMessage(if (!task.isDone) "Task marked done" else "Task marked active")
                                    }
                                },
                                onDelete = { task ->
                                    scope.launch {
                                        taskDao.deleteTask(task)
                                        snackbarIsError = true
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            taskDao.insertTask(task.copy(id = 0))
                                        }
                                    }
                                },
                                onLogout = {
                                    scope.launch {
                                        UserPreferences.clearSession(context)
                                        userName = ""
                                        navController.navigate("welcome") {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("addTask") {
                            AddTaskScreen(
                                existingTask = editingTask,
                                onSave = { title, description, dueDate, priority ->
                                    scope.launch {
                                        if (editingTask != null) {
                                            taskDao.updateTask(
                                                editingTask!!.copy(
                                                    title = title, description = description,
                                                    dueDate = dueDate, priority = priority
                                                )
                                            )
                                            showMessage("Task updated")
                                        } else {
                                            taskDao.insertTask(
                                                Task(
                                                    userName = userName,
                                                    title = title,
                                                    description = description,
                                                    dueDate = dueDate,
                                                    priority = priority
                                                )
                                            )
                                            showMessage("Task added")
                                        }
                                        navController.popBackStack()
                                    }
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun WelcomeScreen(
    onGetStarted: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "What's your name?", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onGetStarted(name) }) {
            Text("Get Started")
        }
    }
}

@Composable
fun PurposeScreen(
    onGetStarted: (purpose: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPurpose by remember { mutableStateOf<String?>(null) }
    val options = listOf("Work", "Personal", "Study", "Other")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "What will you use this for?", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { option ->
            Card(
                onClick = { selectedPurpose = option },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPurpose == option)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { selectedPurpose?.let { onGetStarted(it) } },
            enabled = selectedPurpose != null
        ) {
            Text("Get Started")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onToggleDone: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hey, $userName") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log out")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onAddTask,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Task", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Task List", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    var expanded by remember { mutableStateOf(false) }
                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f)
                            )

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Task options")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (task.isDone) "Mark as Active" else "Mark as Done") },
                                        onClick = {
                                            menuExpanded = false
                                            onToggleDone(task)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            menuExpanded = false
                                            onEdit(task)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            onDelete(task)
                                        }
                                    )
                                }
                            }
                        }

                        if (expanded) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                if (task.description.isNotBlank()) {
                                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.height(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = task.dueDate.ifBlank { "No due date" }, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (task.priority) {
                                                Priority.HIGH -> Color(0xFFFFCDD2)
                                                Priority.MEDIUM -> Color(0xFFFFF9C4)
                                                Priority.LOW -> Color(0xFFC8E6C9)
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = task.priority.name,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (task.isDone) "Done" else "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (task.isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    existingTask: Task? = null,
    onSave: (title: String, description: String, dueDate: String, priority: Priority) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(existingTask?.priority ?: Priority.LOW) }
    var dueDate by remember { mutableStateOf(existingTask?.dueDate ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingTask != null) "Edit Task" else "Add Task") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(text = "Task Title", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Description (optional)", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Due Date", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = dueDate,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select a date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    shape = RoundedCornerShape(20.dp),
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                dueDate = formatter.format(Date(millis))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Priority", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEach { priority ->
                    Card(
                        onClick = { selectedPriority = priority },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPriority == priority)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) {
                        Text(text = priority.name, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onSave(title, description, dueDate, selectedPriority) },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (existingTask != null) "Update" else "Save")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ToDoListPreview() {
    ToDoListTheme {
        WelcomeScreen(onGetStarted = {})
    }
}