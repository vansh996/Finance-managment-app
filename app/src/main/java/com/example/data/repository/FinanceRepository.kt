package com.example.data.repository

import com.example.data.db.FinanceDao
import com.example.data.model.Transaction
import com.example.data.model.Budget
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = financeDao.getAllBudgets()

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        financeDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    suspend fun insertBudget(budget: Budget) {
        financeDao.insertBudget(budget)
    }

    suspend fun insertBudgets(budgets: List<Budget>) {
        financeDao.insertBudgets(budgets)
    }

    suspend fun updateBudget(budget: Budget) {
        financeDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        financeDao.deleteBudget(budget)
    }
}
