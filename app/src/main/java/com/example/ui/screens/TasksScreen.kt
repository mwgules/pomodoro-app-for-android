package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskCategory
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskPriority
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TaskUpsertDialog

enum class TaskFilterTab(val title: String) {
    ALL("Tümü"),
    ACTIVE("Aktif"),
    COMPLETED("Tamamlanan"),
    HIGH_PRIORITY("Öncelikli")
}

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    activeTaskId: Long?,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onSelectForTimer: (TaskEntity) -> Unit,
    onCreateTask: (title: String, description: String, targetPomodoros: Int, priority: TaskPriority, category: TaskCategory) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf(TaskFilterTab.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<TaskCategory?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    val filteredTasks = remember(tasks, searchQuery, selectedFilterTab, selectedCategoryFilter) {
        tasks.filter { task ->
            val matchesQuery = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedFilterTab) {
                TaskFilterTab.ALL -> true
                TaskFilterTab.ACTIVE -> !task.isCompleted
                TaskFilterTab.COMPLETED -> task.isCompleted
                TaskFilterTab.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH && !task.isCompleted
            }

            val matchesCategory = selectedCategoryFilter == null || task.category == selectedCategoryFilter

            matchesQuery && matchesTab && matchesCategory
        }
    }

    val activeCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Yeni Görev") },
                text = { Text("Yeni Görev") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_task")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Görevlerde ara...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_tasks")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                TaskFilterTab.entries.forEach { tab ->
                    val isSelected = selectedFilterTab == tab
                    val count = when (tab) {
                        TaskFilterTab.ALL -> tasks.size
                        TaskFilterTab.ACTIVE -> activeCount
                        TaskFilterTab.COMPLETED -> completedCount
                        TaskFilterTab.HIGH_PRIORITY -> tasks.count { it.priority == TaskPriority.HIGH && !it.isCompleted }
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterTab = tab },
                        label = { Text("${tab.title} ($count)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("tab_filter_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Filter Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("Tüm Kategoriler") },
                    shape = RoundedCornerShape(8.dp)
                )

                TaskCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.displayName) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List or Empty State
            if (filteredTasks.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "Aramanızla eşleşen görev bulunamadı" else "Henüz görev eklenmedi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Yeni bir görev ekleyerek pomodoro odaklanmalarınızı organize edin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isActiveTask = task.id == activeTaskId,
                            onToggleComplete = onToggleTaskCompletion,
                            onSelectForTimer = onSelectForTimer,
                            onEditTask = { taskToEdit = it },
                            onDeleteTask = onDeleteTask
                        )
                    }
                }
            }
        }
    }

    // Create Task Dialog
    if (showCreateDialog) {
        TaskUpsertDialog(
            taskToEdit = null,
            onDismiss = { showCreateDialog = false },
            onSave = { title, desc, targetPoms, priority, cat ->
                onCreateTask(title, desc, targetPoms, priority, cat)
                showCreateDialog = false
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        TaskUpsertDialog(
            taskToEdit = task,
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, targetPoms, priority, cat ->
                onUpdateTask(
                    task.copy(
                        title = title,
                        description = desc,
                        targetPomodoros = targetPoms,
                        priority = priority,
                        category = cat
                    )
                )
                taskToEdit = null
            }
        )
    }
}
