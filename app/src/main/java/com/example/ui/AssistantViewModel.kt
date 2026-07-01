package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiHelper
import com.example.data.CalendarEvent
import com.example.data.CalendarHelper
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantViewModel(
    private val repository: TaskRepository,
    private val calendarHelper: CalendarHelper
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents

    private val _motivationMessage = MutableStateFlow<String>("Loading motivation...")
    val motivationMessage: StateFlow<String> = _motivationMessage

    fun loadCalendarEvents() {
        viewModelScope.launch {
            _calendarEvents.value = calendarHelper.getUpcomingEvents()
            generateMotivation()
        }
    }

    private fun generateMotivation() {
        viewModelScope.launch {
            val currentTasks = tasks.value
            val currentEvents = _calendarEvents.value
            
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            
            val taskString = currentTasks.joinToString("\n") { "- ${it.title}" }
            val eventString = currentEvents.take(5).joinToString("\n") { 
                "- ${it.title} at ${sdf.format(Date(it.startTime))}" 
            }

            if (taskString.isEmpty() && eventString.isEmpty()) {
                _motivationMessage.value = "Your schedule is clear! Take a deep breath and enjoy the moment."
            } else {
                _motivationMessage.value = GeminiHelper.getMotivationalMessage(taskString, eventString)
            }
        }
    }

    fun addTask(title: String, description: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    dueDate = System.currentTimeMillis() + 86400000 // due in 1 day by default
                )
            )
            generateMotivation()
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTaskById(task.id)
            generateMotivation()
        }
    }
}
