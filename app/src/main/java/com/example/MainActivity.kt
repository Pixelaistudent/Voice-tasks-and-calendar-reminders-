package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.CalendarHelper
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.ui.AssistantViewModel
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java, "tasks-db"
        ).build()
        
        val repository = TaskRepository(db.taskDao())
        val calendarHelper = CalendarHelper(applicationContext)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AssistantViewModel(repository, calendarHelper) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val calendarPermissionState = rememberPermissionState(
                    android.Manifest.permission.READ_CALENDAR
                )
                
                val assistantViewModel: AssistantViewModel = viewModel(factory = factory)
                
                LaunchedEffect(calendarPermissionState.status) {
                    if (!calendarPermissionState.status.isGranted) {
                        calendarPermissionState.launchPermissionRequest()
                    } else {
                        assistantViewModel.loadCalendarEvents()
                    }
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(viewModel = assistantViewModel)
                }
            }
        }
    }
}
