package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CanteenFoodItem
import com.example.data.SchoolOrder
import com.example.data.SchoolRepository
import com.example.data.StoreProductItem
import com.example.data.SubjectGrade
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SchoolRepository(database)
        
        // Populating initial data in background
        viewModelScope.launch {
            repository.populateDefaultGradesIfEmpty()
            repository.populateDefaultCanteenAndStoreIfEmpty()
        }
    }

    // Reactive Profile Flow
    val profile: StateFlow<UserProfile?> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Reactive Orders Flow
    val allOrders: StateFlow<List<SchoolOrder>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic Items from Database
    val canteenItems: StateFlow<List<CanteenFoodItem>> = repository.allCanteenItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val storeItems: StateFlow<List<StoreProductItem>> = repository.allStoreItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected Term State (Initially "1º Trimestre")
    private val _selectedTerm = MutableStateFlow("1º Trimestre")
    val selectedTerm: StateFlow<String> = _selectedTerm.asStateFlow()

    // Grades filtered by selected term
    val termGrades: StateFlow<List<SubjectGrade>> = _selectedTerm
        .flatMapLatest { term ->
            repository.getGradesForTerm(term)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cafeteria Shopping Cart State Map of: ItemName to Pair(Price, Quantity)
    private val _cafeteriaCart = MutableStateFlow<Map<String, Pair<Double, Int>>>(emptyMap())
    val cafeteriaCart: StateFlow<Map<String, Pair<Double, Int>>> = _cafeteriaCart.asStateFlow()

    // School Store Shopping Cart State Map of: ItemName to Pair(Price, Quantity)
    private val _storeCart = MutableStateFlow<Map<String, Pair<Double, Int>>>(emptyMap())
    val storeCart: StateFlow<Map<String, Pair<Double, Int>>> = _storeCart.asStateFlow()

    // Notification / UI Events
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    fun selectTerm(term: String) {
        _selectedTerm.value = term
    }

    // --- Cafeteria Cart Operations ---
    fun addToCafeteriaCart(itemName: String, price: Double) {
        val current = _cafeteriaCart.value.toMutableMap()
        val existing = current[itemName]
        if (existing == null) {
            current[itemName] = Pair(price, 1)
        } else {
            current[itemName] = Pair(price, existing.second + 1)
        }
        _cafeteriaCart.value = current
    }

    fun decreaseCafeteriaCart(itemName: String) {
        val current = _cafeteriaCart.value.toMutableMap()
        val existing = current[itemName] ?: return
        if (existing.second <= 1) {
            current.remove(itemName)
        } else {
            current[itemName] = Pair(existing.first, existing.second - 1)
        }
        _cafeteriaCart.value = current
    }

    fun clearCafeteriaCart() {
        _cafeteriaCart.value = emptyMap()
    }

    val cafeteriaTotal: Double
        get() = _cafeteriaCart.value.values.sumOf { it.first * it.second }

    // --- Store Cart Operations ---
    fun addToStoreCart(itemName: String, price: Double) {
        val current = _storeCart.value.toMutableMap()
        val existing = current[itemName]
        if (existing == null) {
            current[itemName] = Pair(price, 1)
        } else {
            current[itemName] = Pair(price, existing.second + 1)
        }
        _storeCart.value = current
    }

    fun decreaseStoreCart(itemName: String) {
        val current = _storeCart.value.toMutableMap()
        val existing = current[itemName] ?: return
        if (existing.second <= 1) {
            current.remove(itemName)
        } else {
            current[itemName] = Pair(existing.first, existing.second - 1)
        }
        _storeCart.value = current
    }

    fun clearStoreCart() {
        _storeCart.value = emptyMap()
    }

    val storeTotal: Double
        get() = _storeCart.value.values.sumOf { it.first * it.second }

    // --- Profile Operations ---
    fun saveUserProfile(name: String, gradeClass: String) {
        viewModelScope.launch {
            repository.saveProfile(UserProfile(studentName = name, gradeClass = gradeClass))
            _uiEvents.emit("Seja bem-vindo, $name! Perfil atualizado com sucesso.")
        }
    }

    // --- Order Submissions ---
    fun checkoutCafeteria(studentName: String) {
        if (studentName.trim().isEmpty()) {
            viewModelScope.launch {
                _uiEvents.emit("Erro: Nome do aluno é obrigatório para fazer o pedido!")
            }
            return
        }
        if (_cafeteriaCart.value.isEmpty()) {
            viewModelScope.launch {
                _uiEvents.emit("O seu carrinho da Cantina está vazio!")
            }
            return
        }

        viewModelScope.launch {
            val details = _cafeteriaCart.value.entries.joinToString(", ") { "${it.key} (x${it.value.second})" }
            val amount = cafeteriaTotal
            val order = SchoolOrder(
                studentName = studentName,
                orderType = "Cantina",
                itemDetails = details,
                totalAmount = amount,
                status = "Em Preparação"
            )
            repository.placeOrder(order)
            clearCafeteriaCart()
            _uiEvents.emit("Pedido na Cantina enviado com sucesso para $studentName!")
        }
    }

    fun checkoutStore(studentName: String) {
        if (studentName.trim().isEmpty()) {
            viewModelScope.launch {
                _uiEvents.emit("Erro: Nome do aluno é obrigatório para encomendar!")
            }
            return
        }
        if (_storeCart.value.isEmpty()) {
            viewModelScope.launch {
                _uiEvents.emit("O seu carrinho da Papelaria está vazio!")
            }
            return
        }

        viewModelScope.launch {
            val details = _storeCart.value.entries.joinToString(", ") { "${it.key} (x${it.value.second})" }
            val amount = storeTotal
            val order = SchoolOrder(
                studentName = studentName,
                orderType = "Papelaria",
                itemDetails = details,
                totalAmount = amount,
                status = "Pendente"
            )
            repository.placeOrder(order)
            clearStoreCart()
            _uiEvents.emit("Encomenda de Papelaria realizada com sucesso para $studentName!")
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            _uiEvents.emit("Pedido/Encomenda cancelado com sucesso.")
        }
    }

    // --- Admin Operations ---
    fun saveCanteenItem(item: CanteenFoodItem) {
        viewModelScope.launch {
            repository.saveCanteenItem(item)
            _uiEvents.emit("Item da Cantina '${item.name}' guardado!")
        }
    }

    fun deleteCanteenItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCanteenItem(id)
            _uiEvents.emit("Item da Cantina removido.")
        }
    }

    fun saveStoreItem(item: StoreProductItem) {
        viewModelScope.launch {
            repository.saveStoreItem(item)
            _uiEvents.emit("Artigo de Papelaria '${item.name}' guardado!")
        }
    }

    fun deleteStoreItem(id: Int) {
        viewModelScope.launch {
            repository.deleteStoreItem(id)
            _uiEvents.emit("Artigo de Papelaria removido.")
        }
    }

    fun saveGrade(grade: SubjectGrade) {
        viewModelScope.launch {
            repository.saveGrade(grade)
            _uiEvents.emit("Nota de '${grade.subjectName}' guardada!")
        }
    }

    fun deleteGrade(id: Int) {
        viewModelScope.launch {
            repository.deleteGrade(id)
            _uiEvents.emit("Nota eliminada.")
        }
    }

    fun updateOrderStatus(order: SchoolOrder, newStatus: String) {
        viewModelScope.launch {
            repository.placeOrder(order.copy(status = newStatus))
            _uiEvents.emit("Estado do pedido atualizado para '$newStatus'.")
        }
    }
}
