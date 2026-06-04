package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.FinanceTrackerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room Database components
    val database = AppDatabase.getDatabase(this)
    val financeDao = database.financeDao()
    val repository = FinanceRepository(financeDao)
    
    // Retrieve ViewModel via custom Factory constructor injection
    val viewModel: FinanceViewModel by viewModels {
      FinanceViewModelFactory(repository)
    }
    
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FinanceTrackerApp(viewModel)
      }
    }
  }
}

