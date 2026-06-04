package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.data.model.Investment
import com.example.ui.viewmodel.CategoryBudgetStatus
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class FinanceTab {
    DASHBOARD, TRANSACTIONS, ADD, INVESTMENTS, BUDGETS, COACH
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food & Dining" -> Color(0xFFFFB300)       // Bright Orange/Amber
        "Transportation" -> Color(0xFF29B6F6)      // Ice Blue
        "Shopping & Retail" -> Color(0xFFAB47BC)    // Warm Purple
        "Housing & Bills" -> Color(0xFFEC407A)      // Coral Pink
        "Entertainment" -> Color(0xFF26A69A)        // Cozy Teal
        "Others" -> Color(0xFF78909C)               // Slate Grey
        "Income" -> Color(0xFF66BB6A)               // Minty Green
        else -> Color(0xFFB0BEC5)
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Food & Dining" -> Icons.Default.ShoppingCart
        "Transportation" -> Icons.Default.PlayArrow
        "Shopping & Retail" -> Icons.Default.Star
        "Housing & Bills" -> Icons.Default.Home
        "Entertainment" -> Icons.Default.Face
        "Income" -> Icons.Default.Check
        else -> Icons.Default.Info
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTrackerApp(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(FinanceTab.DASHBOARD) }
    
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    
    // Derived overall financial stats
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
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
                            .clip(CircleShape)
                            .background(Color(0xFFDBE1FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "JD", 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001452),
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            "Good morning,",
                            fontSize = 11.sp,
                            color = Color(0xFF44464F)
                        )
                        Text(
                            "John Doe",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1F)
                        )
                    }
                }
                
                IconButton(
                    onClick = { /* notification target */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1B1B1F)
                    )
                }
            }
        },
        bottomBar = {
            Column {
                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE1E2EC)))
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = Color(0xFFF3F3F7),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.DASHBOARD,
                        onClick = { activeTab = FinanceTab.DASHBOARD },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.TRANSACTIONS,
                        onClick = { activeTab = FinanceTab.TRANSACTIONS },
                        icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                        label = { Text("Ledger", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_transactions")
                    )
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.ADD,
                        onClick = { activeTab = FinanceTab.ADD },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Transaction") },
                        label = { Text("Log Tx", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_add")
                    )
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.INVESTMENTS,
                        onClick = { activeTab = FinanceTab.INVESTMENTS },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Investments") },
                        label = { Text("Invest", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_investments")
                    )
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.BUDGETS,
                        onClick = { activeTab = FinanceTab.BUDGETS },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Budgets") },
                        label = { Text("Limits", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_budgets")
                    )
                    NavigationBarItem(
                        selected = activeTab == FinanceTab.COACH,
                        onClick = { activeTab = FinanceTab.COACH },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Insights") },
                        label = { Text("AI Coach", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_coach")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Conditionally show header card based on whether we are viewing investments or checkings/wallets
            if (activeTab == FinanceTab.INVESTMENTS) {
                PortfolioHeaderCard(investments)
            } else {
                SummaryHeaderCard(balance, totalIncome, totalExpense)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tab Content Switcher with fade animation
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(90))
                    },
                    label = "tab_fade"
                ) { targetTab ->
                    when (targetTab) {
                        FinanceTab.DASHBOARD -> DashboardScreen(categoryBudgets, transactions, onSeeDetailsClick = { activeTab = FinanceTab.BUDGETS })
                        FinanceTab.TRANSACTIONS -> TransactionsScreen(transactions, onDelete = { viewModel.deleteTransaction(it) })
                        FinanceTab.ADD -> AddTransactionScreen(
                            onAdd = { title, amt, type, cat, notes ->
                                viewModel.insertTransaction(title, amt, type, cat, notes)
                                activeTab = FinanceTab.DASHBOARD
                            },
                            viewModel = viewModel
                        )
                        FinanceTab.INVESTMENTS -> InvestmentsScreen(viewModel)
                        FinanceTab.BUDGETS -> BudgetsScreen(categoryBudgets, onSaveLimit = { cat, limit ->
                            viewModel.updateBudgetLimit(cat, limit)
                        })
                        FinanceTab.COACH -> AICoachScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryHeaderCard(balance: Double, income: Double, expense: Double) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEADDFF) // Beautiful light lavender-purple banner
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        fontSize = 14.sp,
                        color = Color(0xFF21005D),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currencyFormat.format(balance),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D),
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Delta badge matching design Tailwind
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "+2.4%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income detail block with translucent highlight
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "INCOME", 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF21005D).copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            currencyFormat.format(income), 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF006E1C)
                        )
                    }
                }
                
                // Expense detail block with translucent highlight
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "EXPENSES", 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF21005D).copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            currencyFormat.format(expense), 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFFBA1A1A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(categoryBudgets: List<CategoryBudgetStatus>, transactions: List<Transaction>, onSeeDetailsClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    val totalCategoryExpense = categoryBudgets.sumOf { it.spent }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // High fidelity custom donut spending chart card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Expense Distribution",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (totalCategoryExpense == 0.0) {
                    // Empty state helper for first-launch or clean states
                    Box(
                        modifier = Modifier.height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = "Empty", 
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No expenses yet this month.\nDraw outstanding tx is empty.", 
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Drawing Donut Chart on custom canvas
                        Canvas(modifier = Modifier.size(140.dp)) {
                            var startAngle = -90f
                            categoryBudgets.forEach { status ->
                                val percentage = if (totalCategoryExpense > 0) (status.spent / totalCategoryExpense).toFloat() else 0f
                                val sweepAngle = percentage * 360f
                                if (sweepAngle > 0) {
                                    drawArc(
                                        color = getCategoryColor(status.category),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // Add minor angle offset so segments are separated neatly
                                    startAngle += sweepAngle
                                }
                            }
                        }
                        
                        // Mini category color legend
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            categoryBudgets.filter { it.spent > 0 }.sortedByDescending { it.spent }.take(4).forEach { status ->
                                val percentage = (status.spent / totalCategoryExpense * 100).toInt()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(getCategoryColor(status.category))
                                    )
                                    Text(
                                        text = "${status.category} (${percentage}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Area title and see-details action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Budgets",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1B1B1F)
            )
            Text(
                text = "See details",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6750A4),
                modifier = Modifier.clickable { onSeeDetailsClick() }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryBudgets.forEach { status ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFC7C6CA), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(getCategoryColor(status.category).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(status.category),
                                        contentDescription = status.category,
                                        tint = getCategoryColor(status.category),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = status.category, 
                                    fontSize = 14.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1F)
                                )
                            }
                            Text(
                                text = "${currencyFormat.format(status.spent)} / ${currencyFormat.format(status.limit)}",
                                fontSize = 12.sp,
                                color = if (status.spent > status.limit) Color(0xFFBA1A1A) else Color(0xFF44464F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Progress bar height 6dp, tracks in e1e2ec filled in 6750a4
                        LinearProgressIndicator(
                            progress = { status.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (status.spent > status.limit) Color(0xFFBA1A1A) else Color(0xFF6750A4),
                            trackColor = Color(0xFFE1E2EC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsScreen(transactions: List<Transaction>, onDelete: (Transaction) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    val filteredTransactions = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || 
                            tx.note.contains(searchQuery, ignoreCase = true) ||
                            tx.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || tx.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }
    
    val categories = listOf("All", "Food & Dining", "Transportation", "Shopping & Retail", "Housing & Bills", "Entertainment", "Others", "Income")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search & Filtering Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search transactions...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("ledger_search"),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Horizontal category chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceIn(0, categories.size - 1),
            edgePadding = 0.dp,
            divider = {},
            indicator = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            categories.forEach { categoryName ->
                val isSelected = selectedCategoryFilter == categoryName
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { selectedCategoryFilter = categoryName }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = categoryName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Transaction rows
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = "Search empty", 
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No transactions match your filter.", 
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemRow(tx, currencyFormat, onDeleteClick = { onDelete(tx) })
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(tx: Transaction, formatter: NumberFormat, onDeleteClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFC7C6CA), RoundedCornerShape(16.dp))
            .testTag("transaction_item_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left category icon circular layout with high-density light slate bg
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(tx.category),
                    contentDescription = tx.category,
                    tint = Color(0xFF44464F),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Middle section (Name, note, and date)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1B1B1F),
                    maxLines = 1
                )
                Text(
                    text = if (tx.note.isNotBlank()) "${tx.category} • ${tx.note}" else tx.category,
                    fontSize = 11.sp,
                    color = Color(0xFF44464F),
                    maxLines = 1
                )
                Text(
                    text = sdf.format(Date(tx.timestamp)),
                    fontSize = 10.sp,
                    color = Color(0xFF44464F).copy(alpha = 0.7f)
                )
            }
            
            // Right section (Amount & Delete option)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (tx.type == "INCOME") "+${formatter.format(tx.amount)}" else "-${formatter.format(tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (tx.type == "INCOME") Color(0xFF006E1C) else Color(0xFFBA1A1A),
                    modifier = Modifier.padding(end = 4.dp)
                )
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete, 
                        contentDescription = "Delete",
                        tint = Color(0xFFBA1A1A).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onAdd: (String, Double, String, String, String) -> Unit,
    viewModel: FinanceViewModel
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    
    val isCategorizing by viewModel.isCategorizing.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    
    val categories = listOf("Food & Dining", "Transportation", "Shopping & Retail", "Housing & Bills", "Entertainment", "Others")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Log New Transaction", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        
        // Income vs Expense Segment Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (type == "EXPENSE") MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { 
                        type = "EXPENSE" 
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Expense", 
                    fontWeight = FontWeight.Bold, 
                    color = if (type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (type == "INCOME") getCategoryColor("Income").copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { 
                        type = "INCOME" 
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Income", 
                    fontWeight = FontWeight.Bold, 
                    color = if (type == "INCOME") getCategoryColor("Income") else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Details Field Input card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Description field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What did you buy/receive?") },
                    placeholder = { Text("e.g. Starbucks Mocha, paycheck, Uber...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                // AI Categorization flow triggers if Expense
                if (type == "EXPENSE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.categorizeDescriptionWithAI(title) { category ->
                                    selectedCategory = category
                                }
                            },
                            enabled = title.isNotBlank() && !isCategorizing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier.testTag("ai_categorize_button")
                        ) {
                            if (isCategorizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Detecting...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Info, contentDescription = "AI symbol", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Auto-Categorize ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
                
                // Optional Notes
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Notes / Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        }
        
        // Manual Category selector row (ignored if type == INCOME, shown if EXPENSE)
        if (type == "EXPENSE") {
            Text("Select Expense Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isCatSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                if (isCatSelected) getCategoryColor(cat).copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(cat))
                            )
                            Text(
                                text = cat, 
                                fontSize = 12.sp, 
                                color = if (isCatSelected) getCategoryColor(cat) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Save Transaction button
        val isAmountValid = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0.0
        val isFormValid = title.isNotBlank() && isAmountValid
        
        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                onAdd(title, amt, type, selectedCategory, note)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("save_transaction_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (type == "EXPENSE") MaterialTheme.colorScheme.error else getCategoryColor("Income")
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Confirm Transaction", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun BudgetsScreen(
    categoryBudgets: List<CategoryBudgetStatus>,
    onSaveLimit: (String, Double) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Customize Monthly Budgets", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            "Slide to realign limits based on your living constraints. Values optimize in real-time, matching budget calculations.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        
        categoryBudgets.forEach { status ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_adjuster_${status.category}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(status.category))
                            )
                            Text(status.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            currencyFormat.format(status.limit),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Sliders config: up to default $2,000 for sliding adjustments
                    Slider(
                        value = status.limit.toFloat(),
                        onValueChange = { newValue ->
                            // Round limits to increments of 10
                            val roundedProgress = (newValue / 10).toInt() * 10
                            onSaveLimit(status.category, roundedProgress.toDouble().coerceIn(10.0, 3000.0))
                        },
                        valueRange = 10f..3000f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = getCategoryColor(status.category),
                            activeTrackColor = getCategoryColor(status.category)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Min: $10", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text("Max: $3,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun AICoachScreen(viewModel: FinanceViewModel) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Gemini Personal Wealth Coach", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            "Our Gemini-3.5-Flash assistant scans your local database metrics to produce visual insights, saving suggestions, and personal spending coaching reviews.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (aiInsights == null) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = "Coach logo", 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Your localized finance coach is ready.",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "We will pass your spending breakdown securely to analyze limits and deliver visual savings strategies.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = aiInsights ?: "",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().testTag("ai_insights_output")
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.generateAIFinancialTips() },
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_coach_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Reading Ledger...", color = Color.Black)
                    } else {
                        Text(
                            if (aiInsights == null) "Request Coaching Review 🌿" else "Refresh Coaching Report 🔄",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                
                if (aiInsights != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.clearInsights() }) {
                        Text("Clear Coaching Report", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Caution Warning Card as instructed by android-secret-management skill
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Warning, 
                    contentDescription = "Warning", 
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text("Security Notice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    Text(
                        "We run Gemini API calls locally for prototyping. Do not export this preview APK on unsecured global networks.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioHeaderCard(investments: List<Investment>) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    val totalCost = investments.sumOf { it.purchasePrice * it.quantity }
    val totalValue = investments.sumOf { it.currentPrice * it.quantity }
    val gainLoss = totalValue - totalCost
    val gainPercent = if (totalCost > 0) (gainLoss / totalCost) * 100.0 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEADDFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Portfolio Valuation",
                        fontSize = 12.sp,
                        color = Color(0xFF21005D),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = currencyFormat.format(totalValue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D),
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                val badgeColor = if (gainLoss >= 0) Color(0xFF006E1C) else Color(0xFFBA1A1A)
                val badgeText = if (gainLoss >= 0) "+${"%.2f".format(gainPercent)}%" else "${"%.2f".format(gainPercent)}%"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        "COST BASIS", 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF21005D).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        currencyFormat.format(totalCost), 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF21005D)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        "TOTAL RETURNS", 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF21005D).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        (if (gainLoss >= 0) "+" else "") + currencyFormat.format(gainLoss), 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = if (gainLoss >= 0) Color(0xFF006E1C) else Color(0xFFBA1A1A)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvestmentsScreen(viewModel: FinanceViewModel) {
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingPrices.collectAsStateWithLifecycle()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("STOCK") }
    
    val types = listOf("STOCK", "CRYPTO", "BOND", "MUTUAL_FUND", "OTHER")
    val focusManager = LocalFocusManager.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFC7C6CA), RoundedCornerShape(16.dp))
                    .testTag("add_investment_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log Asset Purchase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1B1B1F)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF3F3F7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW ENTRY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF44464F)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Ticker (e.g. AAPL, BTC)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("investment_name_input"),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Asset Type", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF44464F).copy(alpha = 0.8f),
                                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                types.forEach { currType ->
                                    val isSelected = type == currType
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF6750A4) else Color(0xFFF3F3F7))
                                            .clickable { type = currType }
                                            .border(
                                                1.dp, 
                                                if (isSelected) Color(0xFF6750A4) else Color(0xFFE1E2EC), 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currType,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF44464F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("investment_quantity_input"),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        
                        OutlinedTextField(
                            value = buyPrice,
                            onValueChange = { buyPrice = it },
                            label = { Text("Paid Price ($)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("investment_price_input"),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            val qty = quantity.toDoubleOrNull() ?: 0.0
                            val prc = buyPrice.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && qty > 0.0 && prc > 0.0) {
                                viewModel.insertInvestment(name, type, prc, qty)
                                name = ""
                                quantity = ""
                                buyPrice = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_investment_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Text("Incorporate Asset", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Holdings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "Updated with Intelligent Pricing",
                        fontSize = 10.sp,
                        color = Color(0xFF44464F)
                    )
                }
                
                Button(
                    onClick = { viewModel.refreshInvestmentPrices() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isRefreshing && investments.isNotEmpty(),
                    modifier = Modifier.testTag("refresh_prices_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Color(0xFF21005D)
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(14.dp), tint = Color(0xFF21005D))
                        }
                        Text("Sync Prices", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    }
                }
            }
            if (isRefreshing) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = Color(0xFF6750A4)
                )
            }
        }
        
        if (investments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.dp, Color(0xFFE1E2EC), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No investments",
                            tint = Color(0xFF44464F).copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No assets being tracked yet.",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF1B1B1F)
                        )
                        Text(
                            text = "Enter a premium stock, cryptocurrency, or bond symbol above to track costs & live return percentages.",
                            fontSize = 11.sp,
                            color = Color(0xFF44464F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(investments, key = { holding -> holding.id }) { holding ->
                val cost = holding.purchasePrice * holding.quantity
                val currentVal = holding.currentPrice * holding.quantity
                val profitLoss = currentVal - cost
                val profitPercent = if (cost > 0.0) (profitLoss / cost) * 100.0 else 0.0
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFC7C6CA), RoundedCornerShape(16.dp))
                        .testTag("investment_item_${holding.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F3F7)),
                            contentAlignment = Alignment.Center
                        ) {
                            val assetIcon = when(holding.type) {
                                "STOCK" -> Icons.Default.Star
                                "CRYPTO" -> Icons.Default.Favorite
                                "BOND" -> Icons.Default.Home
                                else -> Icons.Default.Info
                            }
                            Icon(
                                imageVector = assetIcon,
                                contentDescription = holding.type,
                                tint = Color(0xFF44464F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    holding.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1F)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEADDFF))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        holding.type,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF21005D)
                                    )
                                }
                            }
                            Text(
                                text = "Units: ${holding.quantity} • Cost: ${currencyFormat.format(holding.purchasePrice)}",
                                fontSize = 11.sp,
                                color = Color(0xFF44464F)
                            )
                            Text(
                                text = "Market Price: ${currencyFormat.format(holding.currentPrice)}",
                                fontSize = 10.sp,
                                color = Color(0xFF44464F).copy(alpha = 0.7f)
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currencyFormat.format(currentVal),
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1F)
                            )
                            
                            val statusColor = if (profitLoss >= 0.0) Color(0xFF006E1C) else Color(0xFFBA1A1A)
                            val profitSign = if (profitLoss >= 0.0) "+" else ""
                            
                            Text(
                                text = "$profitSign${currencyFormat.format(profitLoss)} (${"%.1f".format(profitPercent)}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        IconButton(
                            onClick = { viewModel.deleteInvestment(holding) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete holding",
                                tint = Color(0xFFBA1A1A).copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
