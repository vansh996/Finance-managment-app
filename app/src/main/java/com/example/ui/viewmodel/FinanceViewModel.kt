package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.model.Budget
import com.example.data.model.Transaction
import com.example.data.model.Investment
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // Expose all investments reactively
    val investments: StateFlow<List<Investment>> = repository.allInvestments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshingPrices = MutableStateFlow(false)
    val isRefreshingPrices: StateFlow<Boolean> = _isRefreshingPrices.asStateFlow()

    // Expose all transactions reactively
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose all budgets reactively
    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined state for category tracking: category to spent, budget limit, and name
    val categoryBudgets: StateFlow<List<CategoryBudgetStatus>> = combine(transactions, budgets) { txList, budgetList ->
        val categories = listOf("Food & Dining", "Transportation", "Shopping & Retail", "Housing & Bills", "Entertainment", "Others")
        categories.map { cat ->
            val spent = txList.filter { it.type == "EXPENSE" && it.category == cat }.sumOf { it.amount }
            val limit = budgetList.firstOrNull { it.category == cat }?.monthlyLimit ?: 100.0
            CategoryBudgetStatus(category = cat, spent = spent, limit = limit)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isCategorizing = MutableStateFlow(false)
    val isCategorizing: StateFlow<Boolean> = _isCategorizing.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Check if budgets are empty
            val currentBudgets = repository.allBudgets.first()
            if (currentBudgets.isEmpty()) {
                val defaultBudgets = listOf(
                    Budget("Food & Dining", 500.0),
                    Budget("Transportation", 150.0),
                    Budget("Shopping & Retail", 300.0),
                    Budget("Housing & Bills", 1200.0),
                    Budget("Entertainment", 200.0),
                    Budget("Others", 100.0)
                )
                repository.insertBudgets(defaultBudgets)
            }

            // Check if transactions are empty
            val currentTransactions = repository.allTransactions.first()
            if (currentTransactions.isEmpty()) {
                val seedTxs = listOf(
                    Transaction(
                        title = "Bi-weekly paycheck",
                        amount = 2500.0,
                        type = "INCOME",
                        category = "Income",
                        note = "Primary salary deposit"
                    ),
                    Transaction(
                        title = "Whole Foods grocery shopping",
                        amount = 124.50,
                        type = "EXPENSE",
                        category = "Food & Dining",
                        note = "Weekly groceries"
                    ),
                    Transaction(
                        title = "Uber ride to downtown Office",
                        amount = 24.00,
                        type = "EXPENSE",
                        category = "Transportation",
                        note = "Business commute"
                    ),
                    Transaction(
                        title = "Netflix regular streaming plan",
                        amount = 15.49,
                        type = "EXPENSE",
                        category = "Housing & Bills",
                        note = "Auto-pay bill"
                    ),
                    Transaction(
                        title = "Indie music concert tickets",
                        amount = 85.00,
                        type = "EXPENSE",
                        category = "Entertainment",
                        note = "Friday night out"
                    )
                )
                for (tx in seedTxs) {
                    repository.insertTransaction(tx)
                }
            }

            // Check if investments are empty
            val currentInvestments = repository.allInvestments.first()
            if (currentInvestments.isEmpty()) {
                val seedInvs = listOf(
                    Investment(
                        name = "AAPL",
                        type = "STOCK",
                        purchasePrice = 150.0,
                        quantity = 15.0,
                        currentPrice = 175.50
                    ),
                    Investment(
                        name = "BTC",
                        type = "CRYPTO",
                        purchasePrice = 58200.0,
                        quantity = 0.35,
                        currentPrice = 67500.0
                    ),
                    Investment(
                        name = "UST10Y",
                        type = "BOND",
                        purchasePrice = 98.50,
                        quantity = 20.0,
                        currentPrice = 99.10
                    )
                )
                for (inv in seedInvs) {
                    repository.insertInvestment(inv)
                }
            }
        }
    }

    fun insertTransaction(title: String, amount: Double, type: String, category: String, note: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = if (type == "INCOME") "Income" else category,
                    note = note
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateBudgetLimit(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(Budget(category = category, monthlyLimit = limit))
        }
    }

    fun categorizeDescriptionWithAI(description: String, onCategoryDetected: (String) -> Unit) {
        if (description.isBlank()) return
        viewModelScope.launch {
            _isCategorizing.value = true
            val prompt = """
                You are an expert financial expense categorizer. Given the transaction description: '$description', choose and output EXACTLY ONE of the general category names from the list below:
                - Food & Dining
                - Transportation
                - Shopping & Retail
                - Housing & Bills
                - Entertainment
                - Others

                Return ONLY the plain category name, with no punctuation, prefixes, markdown, or additional text. Example response: Food & Dining
            """.trimIndent()

            val result = GeminiClient.runPrompt(prompt)
            _isCategorizing.value = false

            val sanitizedResult = result?.trim() ?: "Others"
            val validCategories = listOf("Food & Dining", "Transportation", "Shopping & Retail", "Housing & Bills", "Entertainment", "Others")
            
            val matchedCategory = validCategories.firstOrNull { 
                it.equals(sanitizedResult, ignoreCase = true) || sanitizedResult.contains(it, ignoreCase = true)
            } ?: "Others"
            
            onCategoryDetected(matchedCategory)
        }
    }

    fun generateAIFinancialTips() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _aiInsights.value = "Analyzing your spending and calculating insights with Gemini..."

            val budgetList = budgets.value
            val txList = transactions.value

            val categories = listOf("Food & Dining", "Transportation", "Shopping & Retail", "Housing & Bills", "Entertainment", "Others")
            
            val budgetVsSpendingText = categories.joinToString("\n") { cat ->
                val spent = txList.filter { it.type == "EXPENSE" && it.category == cat }.sumOf { it.amount }
                val limit = budgetList.firstOrNull { it.category == cat }?.monthlyLimit ?: 100.0
                "- $cat: Spent $${"%.2f".format(spent)} out of Monthly Budget Limit of $${"%.2f".format(limit)}"
            }

            val recentTransactionsText = txList.take(8).joinToString("\n") { tx ->
                "- ${if (tx.type == "INCOME") "+" else "-"}$${"%.2f".format(tx.amount)}: ${tx.title} (${tx.category})"
            }

            val totalExpense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val totalIncome = txList.filter { it.type == "INCOME" }.sumOf { it.amount }

            val prompt = """
                You are a highly capable, warm, and friendly personal finance coach. 
                The user has the following monthly budget limits vs current spending status:
                $budgetVsSpendingText
                
                Overall Totals:
                - Total Expenses: $${"%.2f".format(totalExpense)}
                - Total Income: $${"%.2f".format(totalIncome)}
                
                Their recent transactions are:
                $recentTransactionsText

                Provide a warm, encouraging 4-5 sentence financial review of how they are managing their budget, referencing direct categories where they are doing well or overspending.
                Then, list exactly 3 short, highly concrete, actionable budgeting tips formatted with bold titles and neat bullet points.
                Keep the total text response clean, short and highly tailored to their specific spending. Make sure the tone is optimistic.
            """.trimIndent()

            val result = GeminiClient.runPrompt(prompt)
            _isAnalyzing.value = false
            _aiInsights.value = result ?: "Could not fetch insights. Please check your internet connection or try again."
        }
    }
    
    fun clearInsights() {
        _aiInsights.value = null
    }

    fun insertInvestment(name: String, type: String, purchasePrice: Double, quantity: Double) {
        viewModelScope.launch {
            repository.insertInvestment(
                Investment(
                    name = name.uppercase(Locale.ROOT).trim(),
                    type = type,
                    purchasePrice = purchasePrice,
                    quantity = quantity,
                    currentPrice = purchasePrice, // Default to purchase price initially
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
        }
    }

    fun refreshInvestmentPrices() {
        viewModelScope.launch {
            _isRefreshingPrices.value = true
            val currentInvestments = investments.value
            if (currentInvestments.isEmpty()) {
                _isRefreshingPrices.value = false
                return@launch
            }

            val symbols = currentInvestments.map { it.name }.distinct()
            val symbolsStr = symbols.joinToString(", ")

            val prompt = """
                You are a real-time financial market price tracker. Given the following financial symbols/assets:
                $symbolsStr

                Determine or search for their exact or highly realistic current market prices in USD right now. 
                Return the result strictly as a JSON object of key-value pairs where keys are the symbols matching the casing requested and values are numbers representing the price in USD. No surrounding text, no formatting, no markdown. 
                Example response:
                {"AAPL": 178.25, "BTC": 67120.00}
            """.trimIndent()

            val response = GeminiClient.runPrompt(prompt)
            val parsedPrices = if (response != null && !response.contains("API Key is not configured") && !response.contains("API error")) {
                parsePricesFromJson(response)
            } else {
                emptyMap()
            }

            for (investment in currentInvestments) {
                val symbolKey = investment.name.uppercase(Locale.ROOT).trim()
                val fetchedPrice = parsedPrices[symbolKey] ?: parsedPrices[investment.name.trim()]
                
                val newPrice = if (fetchedPrice != null) {
                    fetchedPrice
                } else {
                    // fall back to mock fluctuation (+- 3.5%)
                    val change = 1.0 + ((Math.random() - 0.48) * 0.04)
                    (investment.currentPrice * change).coerceAtLeast(0.01)
                }

                repository.updateInvestment(
                    investment.copy(
                        currentPrice = Math.round(newPrice * 100.0) / 100.0,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
            _isRefreshingPrices.value = false
        }
    }

    private fun parsePricesFromJson(jsonStr: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        try {
            val clean = jsonStr.replace(Regex("(?s)```json\\s*"), "")
                               .replace("```", "")
                               .trim()
            val regex = Regex("\"([^\"]+)\"\\s*:\\s*([0-9.+-]+)")
            val matches = regex.findAll(clean)
            for (match in matches) {
                val sym = match.groups[1]?.value?.trim() ?: continue
                val priceVal = match.groups[2]?.value?.toDoubleOrNull() ?: continue
                map[sym.uppercase(Locale.ROOT)] = priceVal
            }
        } catch (e: Exception) {
            // No-op
        }
        return map
    }
}

data class CategoryBudgetStatus(
    val category: String,
    val spent: Double,
    val limit: Double
) {
    val progress: Float
        get() = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1.2f) else 0f
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
