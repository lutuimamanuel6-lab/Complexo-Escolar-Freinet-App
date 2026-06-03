package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CanteenFoodItem
import com.example.data.SchoolOrder
import com.example.data.StoreProductItem
import com.example.data.SubjectGrade
import com.example.data.UserProfile
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

fun formatKz(amount: Double): String {
    return String.format(Locale("pt", "PT"), "%,.2f Kz", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolApp(
    viewModel: SchoolViewModel = viewModel(),
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemColorScheme = MaterialTheme.colorScheme

    // States from VM
    val profileState by viewModel.profile.collectAsStateWithLifecycle()
    val ordersState by viewModel.allOrders.collectAsStateWithLifecycle()
    val selectedTermState by viewModel.selectedTerm.collectAsStateWithLifecycle()
    val termGradesState by viewModel.termGrades.collectAsStateWithLifecycle()
    val cafeteriaCartState by viewModel.cafeteriaCart.collectAsStateWithLifecycle()
    val storeCartState by viewModel.storeCart.collectAsStateWithLifecycle()

    // Dynamic Database Lists
    val canteenFoods by viewModel.canteenItems.collectAsStateWithLifecycle()
    val storeProducts by viewModel.storeItems.collectAsStateWithLifecycle()

    // Navigation state: "cafeteria", "store", "academics", "profile", "admin"
    var currentTab by remember { mutableStateOf("cafeteria") }

    // Admin login lock states
    var adminPasswordInput by remember { mutableStateOf("") }
    var isAdminLogged by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Inputs for profile setup
    var tempNameInput by remember { mutableStateOf("") }
    var tempGradeInput by remember { mutableStateOf("11º Ano - Artes e Ciências") }
    var showsNameSetupDialog by remember { mutableStateOf(false) }
    var showsScheduleDialog by remember { mutableStateOf(false) }

    // Init inputs when profile loads
    LaunchedEffect(profileState) {
        profileState?.let {
            tempNameInput = it.studentName
            tempGradeInput = it.gradeClass
        } ?: run {
            // Show setup dialog if profile is empty
            showsNameSetupDialog = true
        }
    }

    // Listens to VM events like success/error toasts
    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                systemColorScheme.primary,
                                systemColorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_school_logo_1780473092687),
                            contentDescription = "Logótipo do Complexo Freinet",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COMPLEXO ESCOLAR FREINET",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "Portal Oficial do Estudante",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }

                    // Theme toggle + Activities Schedule + Profile Avatar button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showsScheduleDialog = true }
                                .padding(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Rotina Escolar e Intervalo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleDarkTheme() }
                                .padding(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Alternar Tema",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = systemColorScheme.secondary,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { currentTab = "profile" }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = profileState?.studentName?.take(2)?.uppercase(Locale.ROOT) ?: "??",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Welcome Profile Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = systemColorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (profileState != null && profileState!!.studentName.isNotBlank()) {
                                "Aluno: ${profileState!!.studentName}"
                            } else {
                                "Por favor, registe-se"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                    Text(
                        text = profileState?.gradeClass ?: "Configurar Perfil",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clickable { showsNameSetupDialog = true }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("app_navigation_bar"),
                tonalElevation = 8.dp,
                containerColor = systemColorScheme.surface
            ) {
                // Cafeteria Tab
                NavigationBarItem(
                    selected = currentTab == "cafeteria",
                    onClick = { currentTab = "cafeteria" },
                    label = { Text("Cantina", fontWeight = FontWeight.Bold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Separador Cantina"
                        )
                    },
                    modifier = Modifier.testTag("tab_cafeteria"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = systemColorScheme.primary,
                        selectedTextColor = systemColorScheme.primary,
                        indicatorColor = systemColorScheme.secondary.copy(alpha = 0.25f)
                    )
                )

                // Store Tab
                NavigationBarItem(
                    selected = currentTab == "store",
                    onClick = { currentTab = "store" },
                    label = { Text("Papelaria", fontWeight = FontWeight.Bold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Separador Papelaria"
                        )
                    },
                    modifier = Modifier.testTag("tab_store"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = systemColorScheme.primary,
                        selectedTextColor = systemColorScheme.primary,
                        indicatorColor = systemColorScheme.secondary.copy(alpha = 0.25f)
                    )
                )

                // Academic Tab
                NavigationBarItem(
                    selected = currentTab == "academics",
                    onClick = { currentTab = "academics" },
                    label = { Text("Avaliações", fontWeight = FontWeight.Bold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Separador Boletim"
                        )
                    },
                    modifier = Modifier.testTag("tab_academics"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = systemColorScheme.primary,
                        selectedTextColor = systemColorScheme.primary,
                        indicatorColor = systemColorScheme.secondary.copy(alpha = 0.25f)
                    )
                )

                // Profile Tab
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    label = { Text("Perfil", fontWeight = FontWeight.Bold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Separador Perfil"
                        )
                    },
                    modifier = Modifier.testTag("tab_profile"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = systemColorScheme.primary,
                        selectedTextColor = systemColorScheme.primary,
                        indicatorColor = systemColorScheme.secondary.copy(alpha = 0.25f)
                    )
                )

                // Admin Tab
                NavigationBarItem(
                    selected = currentTab == "admin",
                    onClick = { currentTab = "admin" },
                    label = { Text("Admin", fontWeight = FontWeight.Bold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Separador Admin"
                        )
                    },
                    modifier = Modifier.testTag("tab_admin"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = systemColorScheme.primary,
                        selectedTextColor = systemColorScheme.primary,
                        indicatorColor = systemColorScheme.secondary.copy(alpha = 0.25f)
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(systemColorScheme.background)
                .padding(paddingValues)
        ) {
            when (currentTab) {
                "cafeteria" -> CafeteriaScreen(
                    menuItems = canteenFoods,
                    cart = cafeteriaCartState,
                    total = viewModel.cafeteriaTotal,
                    studentProfile = profileState,
                    onAddToCart = { item -> viewModel.addToCafeteriaCart(item.name, item.price) },
                    onRemoveFromCart = { item -> viewModel.decreaseCafeteriaCart(item.name) },
                    onCheckout = {
                        profileState?.let {
                            if (it.studentName.isBlank()) {
                                showsNameSetupDialog = true
                            } else {
                                viewModel.checkoutCafeteria(it.studentName)
                            }
                        } ?: run {
                            showsNameSetupDialog = true
                        }
                    },
                    onOpenProfileSetup = { showsNameSetupDialog = true }
                )

                "store" -> StoreScreen(
                    products = storeProducts,
                    cart = storeCartState,
                    total = viewModel.storeTotal,
                    studentProfile = profileState,
                    onAddToCart = { product -> viewModel.addToStoreCart(product.name, product.price) },
                    onRemoveFromCart = { product -> viewModel.decreaseStoreCart(product.name) },
                    onCheckout = {
                        profileState?.let {
                            if (it.studentName.isBlank()) {
                                showsNameSetupDialog = true
                            } else {
                                viewModel.checkoutStore(it.studentName)
                            }
                        } ?: run {
                            showsNameSetupDialog = true
                        }
                    },
                    onOpenProfileSetup = { showsNameSetupDialog = true }
                )

                "academics" -> AcademicsScreen(
                    selectedTerm = selectedTermState,
                    grades = termGradesState,
                    onSelectTerm = { viewModel.selectTerm(it) },
                    onOpenSchedule = { showsScheduleDialog = true }
                )

                "profile" -> ProfileScreen(
                    profile = profileState,
                    orders = ordersState,
                    onUpdateProfile = { name, grade -> viewModel.saveUserProfile(name, grade) },
                    onCancelOrder = { orderId -> viewModel.cancelOrder(orderId) },
                    onOpenSetup = { showsNameSetupDialog = true }
                )

                "admin" -> {
                    if (!isAdminLogged) {
                        AdminPasswordScreen(
                            passwordInput = adminPasswordInput,
                            onPasswordChange = { adminPasswordInput = it },
                            isPasswordVisible = isPasswordVisible,
                            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                            onSubmit = {
                                if (adminPasswordInput == "freinet2026") {
                                    isAdminLogged = true
                                    Toast.makeText(context, "Sessão iniciada como Administrador!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        AdminDashboardScreen(
                            orders = ordersState,
                            canteenFoods = canteenFoods,
                            storeProducts = storeProducts,
                            grades = termGradesState,
                            selectedTerm = selectedTermState,
                            onSelectTerm = { viewModel.selectTerm(it) },
                            onUpdateOrderStatus = { order, status -> viewModel.updateOrderStatus(order, status) },
                            onDeleteOrder = { id -> viewModel.cancelOrder(id) },
                            onSaveCanteenItem = { item -> viewModel.saveCanteenItem(item) },
                            onDeleteCanteenItem = { id -> viewModel.deleteCanteenItem(id) },
                            onSaveStoreItem = { item -> viewModel.saveStoreItem(item) },
                            onDeleteStoreItem = { id -> viewModel.deleteStoreItem(id) },
                            onSaveGrade = { grade -> viewModel.saveGrade(grade) },
                            onDeleteGrade = { id -> viewModel.deleteGrade(id) },
                            onLogout = {
                                isAdminLogged = false
                                adminPasswordInput = ""
                            }
                        )
                    }
                }
            }

            // Student profile prompt dialog if blank name
            if (showsNameSetupDialog) {
                AlertDialog(
                    onDismissRequest = {
                        // Allow dismissing only if they already have some profile setup
                        if (profileState != null && profileState!!.studentName.isNotBlank()) {
                            showsNameSetupDialog = false
                        }
                    },
                    title = {
                        Text(
                            text = "Registo do Estudante",
                            fontWeight = FontWeight.Bold,
                            color = systemColorScheme.primary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "É necessário um nome de estudante válido para encomendar refeições ou artigos escolares no Complexo Freinet.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            OutlinedTextField(
                                value = tempNameInput,
                                onValueChange = { tempNameInput = it },
                                label = { Text("Nome Completo do Aluno") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                placeholder = { Text("Ex: Manuel Freinet") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("setup_name_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = systemColorScheme.secondary,
                                    focusedLabelColor = systemColorScheme.secondary
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = tempGradeInput,
                                onValueChange = { tempGradeInput = it },
                                label = { Text("Ano / Turma") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                placeholder = { Text("Ex: 11º Ano - Artes e Ciências") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = systemColorScheme.secondary,
                                    focusedLabelColor = systemColorScheme.secondary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempNameInput.trim().isBlank()) {
                                    Toast.makeText(context, "O nome do aluno é obrigatório!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveUserProfile(tempNameInput.trim(), tempGradeInput.trim())
                                    showsNameSetupDialog = false
                                }
                            },
                            modifier = Modifier.testTag("setup_confirm_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = systemColorScheme.secondary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Guardar Perfil")
                        }
                    },
                    dismissButton = {
                        if (profileState != null && profileState!!.studentName.isNotBlank()) {
                            TextButton(onClick = { showsNameSetupDialog = false }) {
                                Text("Cancelar", color = systemColorScheme.primary)
                            }
                        }
                    }
                )
            }

            // Activity schedule and Recess Break (Intervalo) Popup
            if (showsScheduleDialog) {
                AlertDialog(
                    onDismissRequest = { showsScheduleDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = systemColorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Horários e Atividades",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = systemColorScheme.primary
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Abaixo encontra-se a rotina diária e as principais dinâmicas de aprendizagem cooperativa do Complexo Freinet:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 12.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            
                            val dayDetails = listOf(
                                Triple("08:30 - 09:15", "Reunião de Planeamento", "Co-planeamento autónomo das tarefas semanais."),
                                Triple("09:15 - 12:30", "Trabalho Individual Autónomo", "Planos de trabalho individuais com tutoria."),
                                Triple("12:30 - 14:00", "Almoço Cooperativo & Descanso", "Momento de pausa e cooperação comunitária."),
                                Triple("14:00 - 15:15", "Oficinas e Ateliês de Expressão", "Práticas artísticas, ciências e imprensa escolar."),
                                // Requested interval time (15:15 recess) highlighted!
                                Triple("15:15 - 15:45", "INTERVALO / GRANDE RECREIO (15h15) ⏰", "A nossa hora oficial do recreio e de repor energias!"),
                                Triple("15:45 - 16:30", "Sessão de Partilha Geral", "Apresentação de projetos e auto-avaliação do dia.")
                            )
                            
                            dayDetails.forEach { (time, name, desc) ->
                                val isIntervalo = time.contains("15:15")
                                val cardBg = if (isIntervalo) systemColorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                val borderCol = if (isIntervalo) systemColorScheme.secondary else Color.Transparent
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.2.dp, borderCol),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = if (isIntervalo) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (isIntervalo) systemColorScheme.secondary else systemColorScheme.primary
                                            )
                                            Text(
                                                text = time,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (isIntervalo) systemColorScheme.secondary else systemColorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showsScheduleDialog = false }) {
                            Text("Fechar", color = systemColorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. CAFETERIA COMPONENT (A TOCA DO GRILO)
// ==========================================
@Composable
fun CafeteriaScreen(
    menuItems: List<CanteenFoodItem>,
    cart: Map<String, Pair<Double, Int>>,
    total: Double,
    studentProfile: UserProfile?,
    onAddToCart: (CanteenFoodItem) -> Unit,
    onRemoveFromCart: (CanteenFoodItem) -> Unit,
    onCheckout: () -> Unit,
    onOpenProfileSetup: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    // Obtain categories from database items dynamically, prepending "Todos"
    val categories = remember(menuItems) {
        listOf("Todos") + menuItems.map { it.category }.distinct()
    }

    val filteredItems = remember(selectedCategory, menuItems) {
        if (selectedCategory == "Todos") menuItems else menuItems.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CanteenHeroBanner()

        // Categories selector
        if (categories.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val background = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                    Surface(
                        modifier = Modifier
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(20.dp),
                        color = background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        // List of foods
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum produto cadastrado na Cantina.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    val quantityInCart = cart[item.name]?.second ?: 0
                    CanteenCardItem(
                        item = item,
                        qty = quantityInCart,
                        onAdd = { onAddToCart(item) },
                        onRemove = { onRemoveFromCart(item) }
                    )
                }
            }
        }

        // Checkout panel at bottom
        AnimatedVisibility(
            visible = cart.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Resumo do Pedido da Cantina",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${cart.values.sumOf { it.second }} produtos selecionados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = formatKz(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (studentProfile == null || studentProfile.studentName.isBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable { onOpenProfileSetup() }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nome do Aluno Obrigatório. Configure o perfil aqui.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("checkout_cafeteria"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Enviar Pedido para ${studentProfile.studentName}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CanteenHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "🌮 CANTINA TOCA DO GRILO",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            Text(
                text = "Bolinhos de chouriço caseiros, bolas de Berlim frescas, hambúrgueres e snacks cozinhados com amor.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
fun CanteenCardItem(
    item: CanteenFoodItem,
    qty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (qty > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = item.emoji, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatKz(item.price),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Plus/Minus counters
            if (qty == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_canteen_${item.name.replace(" ", "_")}")
                ) {
                    Text("Pedir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "reduzir quantidade",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = qty.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "aumentar quantidade",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. SCHOOL STORE COMPONENT (PAPELARIA)
// ==========================================
@Composable
fun StoreScreen(
    products: List<StoreProductItem>,
    cart: Map<String, Pair<Double, Int>>,
    total: Double,
    studentProfile: UserProfile?,
    onAddToCart: (StoreProductItem) -> Unit,
    onRemoveFromCart: (StoreProductItem) -> Unit,
    onCheckout: () -> Unit,
    onOpenProfileSetup: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(selectedCategory, products) {
        if (selectedCategory == "Todos") products else products.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        StoreHeroBanner()

        if (categories.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary

                    Surface(
                        modifier = Modifier
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(20.dp),
                        color = background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum artigo cadastrado na Papelaria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { item ->
                    val quantityInCart = cart[item.name]?.second ?: 0
                    StoreProductCard(
                        product = item,
                        qty = quantityInCart,
                        onAdd = { onAddToCart(item) },
                        onRemove = { onRemoveFromCart(item) }
                    )
                }
            }
        }

        // Persistent checkout panel
        AnimatedVisibility(
            visible = cart.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Resumo da Compra - Papelaria",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${cart.values.sumOf { it.second }} artigos escolares",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = formatKz(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (studentProfile == null || studentProfile.studentName.isBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable { onOpenProfileSetup() }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nome do Aluno Obrigatório. Configure o perfil aqui.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("checkout_store"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Encomendar Artigos para ${studentProfile.studentName}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "🎒 PAPELARIA & SUPORTES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            Text(
                text = "Cadernos especiais para o método Freinet, estojos de lápis, kits de geometria e vestuário escolar cívico.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
fun StoreProductCard(
    product: StoreProductItem,
    qty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (qty > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = product.emoji, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (product.category == "Uniformes") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Em Breve",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatKz(product.price),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (product.category == "Uniformes") {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Brevemente", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            } else if (qty == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_store_${product.name.replace(" ", "_")}")
                ) {
                    Text("Comprar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "reduzir quantidade",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = qty.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "aumentar quantidade",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. REPORT CARDS COMPONENT (NOTAS)
// ==========================================
@Composable
fun AcademicsScreen(
    selectedTerm: String,
    grades: List<SubjectGrade>,
    onSelectTerm: (String) -> Unit,
    onOpenSchedule: () -> Unit
) {
    val systemColors = MaterialTheme.colorScheme
    val terms = listOf("1º Trimestre", "2º Trimestre", "3º Trimestre")

    // Calculations
    val weightedSum = remember(grades) { grades.sumOf { it.score * it.coeff } }
    val coefficientsSum = remember(grades) { grades.sumOf { it.coeff } }
    val termAverage = remember(weightedSum, coefficientsSum) {
        if (coefficientsSum > 0) weightedSum / coefficientsSum else 0.0
    }

    val evaluation = remember(termAverage) {
        when {
            termAverage >= 18.0 -> Pair("Menção de Progresso: Excelente", systemColors.secondary)
            termAverage >= 16.0 -> Pair("Menção de Progresso: Muito Bom", systemColors.primary)
            termAverage >= 14.0 -> Pair("Incentivo: Excelente Esforço", Color(0xFF4CAF50))
            termAverage >= 12.0 -> Pair("Estatuto: Satisfaz Bastante", Color(0xFF00BCD4))
            else -> Pair("Acompanhamento: Progresso Regular", Color(0xFFE65100))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(systemColors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            systemColors.primary,
                            systemColors.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "ACOMPANHAMENTO PEDAGÓGICO",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = "Boletim de Avaliação Freinet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = "Foco na autonomia cooperativa, auto-organização e auto-avaliação do estudante.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }

        // Custom Term Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            terms.forEach { term ->
                val isSelected = selectedTerm == term
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) systemColors.primary else systemColors.surface,
                    label = "term color animation"
                )
                val textTint = if (isSelected) Color.White else systemColors.primary

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTerm(term) },
                    shape = RoundedCornerShape(12.dp),
                    color = animatedColor,
                    border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.15f)),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = term,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        color = textTint
                    )
                }
            }
        }

        // Summary Statistics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = systemColors.surface),
            border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    systemColors.primary,
                                    systemColors.secondary,
                                    systemColors.primary
                                )
                            )
                        )
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(systemColors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.ROOT, "%.2f", termAverage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = systemColors.primary,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "/20",
                                style = MaterialTheme.typography.labelSmall,
                                color = systemColors.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Média Geral do Período",
                        style = MaterialTheme.typography.bodySmall,
                        color = systemColors.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = evaluation.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = evaluation.second
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "Disciplinas: ${grades.size}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = systemColors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(systemColors.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Soma Coef: $coefficientsSum",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = systemColors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(systemColors.secondary.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive "Horário de Atividades" shortcut card in Academics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onOpenSchedule() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = systemColors.secondary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, systemColors.secondary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = systemColors.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ver Atividades e Recessos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = systemColors.secondary
                    )
                    Text(
                        text = "Consulte quando ocorrem as aulas, oficinas e o grande intervalo das 15h15.",
                        style = MaterialTheme.typography.bodySmall,
                        color = systemColors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = systemColors.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Detalhes por Disciplina",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = systemColors.primary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )

        // Grades lists
        if (grades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda sem notas lançadas para este trimestre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = systemColors.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades) { gradeItem ->
                    GradeReportRow(gradeItem = gradeItem)
                }
            }
        }
    }
}

@Composable
fun GradeReportRow(gradeItem: SubjectGrade) {
    val systemColors = MaterialTheme.colorScheme
    val progress = (gradeItem.score / gradeItem.maxScore).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = systemColors.surface),
        border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = gradeItem.subjectName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = systemColors.primary
                    )
                    Text(
                        text = "Coeficiente: ${gradeItem.coeff}",
                        style = MaterialTheme.typography.labelSmall,
                        color = systemColors.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.ROOT, "%.1f", gradeItem.score),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (gradeItem.score >= 14.0) systemColors.secondary else systemColors.primary
                    )
                    Text(
                        text = "/${gradeItem.maxScore.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = systemColors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (progress >= 0.7f) systemColors.secondary else systemColors.primary,
                trackColor = systemColors.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val remark = when {
                gradeItem.score >= 18 -> "Resultados brilhantes. Excelente espírito de reflexão e pesquisa cooperativa."
                gradeItem.score >= 15 -> "Competência demonstrada muito acima da média e relatórios de pesquisa rigorosos."
                gradeItem.score >= 12 -> "Trabalho consistente. Colaborador constante em prol do grupo."
                else -> "Precisa de focar-se mais no trabalho de registos e pesquisa individual."
            }

            Text(
                text = "« $remark »",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = systemColors.onSurface.copy(alpha = 0.65f)
                )
            )
        }
    }
}

// ==========================================
// 4. PORTAL PROFILE & HISTORY
// ==========================================
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    orders: List<SchoolOrder>,
    onUpdateProfile: (String, String) -> Unit,
    onCancelOrder: (Int) -> Unit,
    onOpenSetup: () -> Unit
) {
    val systemColors = MaterialTheme.colorScheme

    var nameInput by remember { mutableStateOf("") }
    var classInput by remember { mutableStateOf("") }

    // Synchronize edits
    LaunchedEffect(profile) {
        profile?.let {
            nameInput = it.studentName
            classInput = it.gradeClass
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(systemColors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StudentIdCard(profile = profile)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = systemColors.surface),
                border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modificar Perfil do Aluno",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = systemColors.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome Completo do Aluno") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = systemColors.secondary,
                            focusedLabelColor = systemColors.secondary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = classInput,
                        onValueChange = { classInput = it },
                        label = { Text("Ano / Turma") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = systemColors.secondary,
                            focusedLabelColor = systemColors.secondary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nameInput.trim().isNotBlank()) {
                                onUpdateProfile(nameInput.trim(), classInput.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = systemColors.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar Alterações", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "As Minhas Encomendas (${orders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = systemColors.primary
                )

                if (orders.isNotEmpty()) {
                    Text(
                        text = "Sincronização Ativa",
                        style = MaterialTheme.typography.bodySmall,
                        color = systemColors.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = systemColors.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛍️",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nenhum pedido efetuado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = systemColors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Visite a Cantina ou a Papelaria para realizar as suas encomendas.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = systemColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(orders) { order ->
                OrderHistoryRow(order = order, onCancel = { onCancelOrder(order.id) })
            }
        }
    }
}

@Composable
fun StudentIdCard(profile: UserProfile?) {
    val systemColors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, systemColors.secondary.copy(alpha = 0.25f)),
        color = systemColors.primary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            systemColors.primary,
                            systemColors.primary.copy(alpha = 0.85f),
                            systemColors.secondary.copy(alpha = 0.61f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "COMPLEXO ESCOLAR FREINET",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Cartão de Identidade Pedagógica",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🎓", fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(2.dp, systemColors.secondary, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.studentName?.take(1)?.uppercase(Locale.ROOT) ?: "F",
                            fontWeight = FontWeight.Black,
                            color = systemColors.primary,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = profile?.studentName ?: "SEM PERFIL ATIVO",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile?.gradeClass ?: "ESTUDANTE REGISTADO",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FSC - ${profile?.studentName?.hashCode()?.coerceAtLeast(1000)?.toString()?.take(5) ?: "57102"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado: ALUNO ATIVO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = systemColors.secondary
                    )

                    Text(
                        text = "|||| | |||| | ||| || | || ||",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderHistoryRow(order: SchoolOrder, onCancel: () -> Unit) {
    val systemColors = MaterialTheme.colorScheme
    val formattedDate = remember(order.timestamp) {
        val date = Date(order.timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        format.format(date)
    }

    val isCancellable = remember(order.status) {
        order.status == "Pendente" || order.status == "Em Preparação"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = systemColors.surface),
        border = BorderStroke(1.dp, systemColors.primary.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = if (order.orderType == "Cantina") {
                                systemColors.secondary.copy(alpha = 0.12f)
                            } else {
                                systemColors.primary.copy(alpha = 0.12f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (order.orderType == "Cantina") "🍎" else "🎒",
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.orderType,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = systemColors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = systemColors.onSurface.copy(alpha = 0.5f)
                    )
                }

                val statusColor = when (order.status) {
                    "Pendente" -> Color(0xFFE65100)
                    "Em Preparação" -> systemColors.secondary
                    "Pronto para Levantamento" -> Color(0xFF4CAF50)
                    else -> systemColors.primary
                }

                Text(
                    text = order.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = order.itemDetails,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = systemColors.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(
                        text = "Valor Pago: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = systemColors.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatKz(order.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = systemColors.secondary
                    )
                }

                if (isCancellable) {
                    Text(
                        text = "Cancelar Pedido",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                            .clickable { onCancel() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. ADMIN/DIRECTOR AUTH SCREEN
// ==========================================
@Composable
fun AdminPasswordScreen(
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit
) {
    val systemColorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = systemColorScheme.surface),
            border = BorderStroke(1.dp, systemColorScheme.primary.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = systemColorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = systemColorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Acesso Reservado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = systemColorScheme.primary
                )

                Text(
                    text = "Administração e Direção Geral",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = systemColorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Insira a palavra-passe para gerir os pedidos, produtos da cantina/papelaria e registos de notas.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = systemColorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    label = { Text("Palavra-passe administrativa") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = "Mostrar senha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = systemColorScheme.primary)
                ) {
                    Text(
                        text = "Iniciar Sessão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Simple Visual Transformation helper to mask private digits
private val PasswordTransformation = PasswordVisualTransformation()


// ==========================================
// 6. ADMIN DASHBOARD PANEL (GERIR TUDO)
// ==========================================
@Composable
fun AdminDashboardScreen(
    orders: List<SchoolOrder>,
    canteenFoods: List<CanteenFoodItem>,
    storeProducts: List<StoreProductItem>,
    grades: List<SubjectGrade>,
    selectedTerm: String,
    onSelectTerm: (String) -> Unit,
    onUpdateOrderStatus: (SchoolOrder, String) -> Unit,
    onDeleteOrder: (Int) -> Unit,
    onSaveCanteenItem: (CanteenFoodItem) -> Unit,
    onDeleteCanteenItem: (Int) -> Unit,
    onSaveStoreItem: (StoreProductItem) -> Unit,
    onDeleteStoreItem: (Int) -> Unit,
    onSaveGrade: (SubjectGrade) -> Unit,
    onDeleteGrade: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val systemColorScheme = MaterialTheme.colorScheme

    // Local admin navigation: "orders", "canteen", "store", "grades"
    var adminTab by remember { mutableStateOf("orders") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(systemColorScheme.background)
    ) {
        // Administrative Top Control Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(systemColorScheme.secondary.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = systemColorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PAINEL DA DIREÇÃO",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = systemColorScheme.secondary
                )
            }

            TextButton(
                onClick = onLogout,
                colors = ButtonDefaults.textButtonColors(contentColor = systemColorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sair")
            }
        }

        // Sub-tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Pair("orders", "Encomendas"),
                Pair("canteen", "Cantina"),
                Pair("store", "Papelaria"),
                Pair("grades", "Lançar Notas")
            )

            tabs.forEach { (route, label) ->
                val isSelected = adminTab == route
                Button(
                    onClick = { adminTab = route },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) systemColorScheme.primary else systemColorScheme.surface,
                        contentColor = if (isSelected) Color.White else systemColorScheme.primary
                    ),
                    border = BorderStroke(1.dp, systemColorScheme.primary.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-panels
        when (adminTab) {
            "orders" -> AdminOrdersPanel(
                orders = orders,
                onUpdateStatus = onUpdateOrderStatus,
                onDelete = onDeleteOrder
            )

            "canteen" -> AdminCanteenPanel(
                canteenFoods = canteenFoods,
                onSave = onSaveCanteenItem,
                onDelete = onDeleteCanteenItem
            )

            "store" -> AdminStorePanel(
                storeProducts = storeProducts,
                onSave = onSaveStoreItem,
                onDelete = onDeleteStoreItem
            )

            "grades" -> AdminGradesPanel(
                grades = grades,
                selectedTerm = selectedTerm,
                onSelectTerm = onSelectTerm,
                onSave = onSaveGrade,
                onDelete = onDeleteGrade
            )
        }
    }
}

// ==========================================
// ADMIN SUB-PANELS IMPLEMENTATION
// ==========================================

@Composable
fun AdminOrdersPanel(
    orders: List<SchoolOrder>,
    onUpdateStatus: (SchoolOrder, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Sem encomendas de alunos no momento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Estudante: ${order.studentName}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${order.orderType} • ${order.status}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = formatKz(order.totalAmount),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))

                        Text(
                            text = "Itens: ${order.itemDetails}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Action pills to update status
                        Text(
                            text = "Atualizar Estado:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val states = listOf("Pendente", "Em Preparação", "Pronto para Levantamento", "Concluído")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            states.forEach { state ->
                                val scoreIsThisState = order.status == state
                                Button(
                                    onClick = { onUpdateStatus(order, state) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (scoreIsThisState) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (scoreIsThisState) Color.White else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(state, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        IconButton(
                            onClick = { onDelete(order.id) },
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apagar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCanteenPanel(
    canteenFoods: List<CanteenFoodItem>,
    onSave: (CanteenFoodItem) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salgados") }
    var emoji by remember { mutableStateOf("🍩") }
    
    // For tracking which item is being edited, if any
    var editingId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create/Edit Widget Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingId == null) "Adicionar Novo Item à Cantina" else "Editar Item da Cantina",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Alimento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Preço (Kz)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category radio selector row
                    Text("Categoria:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    val categories = listOf("Salgados", "Refeições", "Sobremesas", "Laticínios", "Bebidas", "Guloseimas")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val active = category == cat
                            FilterChip(
                                selected = active,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingId != null) {
                            OutlinedButton(
                                onClick = {
                                    editingId = null
                                    name = ""
                                    description = ""
                                    priceInput = ""
                                    emoji = "🍩"
                                    category = "Salgados"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar")
                            }
                        }

                        Button(
                            onClick = {
                                val price = priceInput.toDoubleOrNull() ?: 0.0
                                if (name.trim().isNotEmpty() && price > 0) {
                                    onSave(
                                        CanteenFoodItem(
                                            id = editingId ?: 0,
                                            name = name.trim(),
                                            description = description.trim(),
                                            price = price,
                                            category = category,
                                            emoji = emoji.trim()
                                        )
                                    )
                                    // Reset fields
                                    editingId = null
                                    name = ""
                                    description = ""
                                    priceInput = ""
                                    emoji = "🍩"
                                    category = "Salgados"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(if (editingId == null) "Adicionar" else "Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Lista de Alimentos Atuais (${canteenFoods.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(canteenFoods) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${item.category} • ${formatKz(item.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    IconButton(onClick = {
                        editingId = item.id
                        name = item.name
                        description = item.description
                        priceInput = item.price.toString()
                        emoji = item.emoji
                        category = item.category
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStorePanel(
    storeProducts: List<StoreProductItem>,
    onSave: (StoreProductItem) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Material") }
    var emoji by remember { mutableStateOf("📓") }

    // Edit ID tracking
    var editingId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingId == null) "Adicionar Novo Artigo à Papelaria" else "Editar Artigo da Papelaria",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Artigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Preço (Kz)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Categoria:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    val categories = listOf("Material", "Vestuário", "Artigos", "Uniformes")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val active = category == cat
                            FilterChip(
                                selected = active,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingId != null) {
                            OutlinedButton(
                                onClick = {
                                    editingId = null
                                    name = ""
                                    description = ""
                                    priceInput = ""
                                    emoji = "📓"
                                    category = "Material"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar")
                            }
                        }

                        Button(
                            onClick = {
                                val price = priceInput.toDoubleOrNull() ?: 0.0
                                if (name.trim().isNotEmpty() && price > 0) {
                                    onSave(
                                        StoreProductItem(
                                            id = editingId ?: 0,
                                            name = name.trim(),
                                            description = description.trim(),
                                            price = price,
                                            category = category,
                                            emoji = emoji.trim()
                                        )
                                    )
                                    editingId = null
                                    name = ""
                                    description = ""
                                    priceInput = ""
                                    emoji = "📓"
                                    category = "Material"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(if (editingId == null) "Adicionar" else "Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Lista de Artigos Atuais (${storeProducts.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(storeProducts) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(product.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${product.category} • ${formatKz(product.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    IconButton(onClick = {
                        editingId = product.id
                        name = product.name
                        description = product.description
                        priceInput = product.price.toString()
                        emoji = product.emoji
                        category = product.category
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { onDelete(product.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminGradesPanel(
    grades: List<SubjectGrade>,
    selectedTerm: String,
    onSelectTerm: (String) -> Unit,
    onSave: (SubjectGrade) -> Unit,
    onDelete: (Int) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var scoreInput by remember { mutableStateOf("") }
    var maxScoreInput by remember { mutableStateOf("20") }
    var coeffInput by remember { mutableStateOf("2") }

    val terms = listOf("1º Trimestre", "2º Trimestre", "3º Trimestre")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Custom Term Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                terms.forEach { term ->
                    val isSelected = selectedTerm == term
                    Button(
                        onClick = { onSelectTerm(term) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(term, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lançar Nota de Disciplina ($selectedTerm)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Nome da Disciplina (Ex: Geografia)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scoreInput,
                            onValueChange = { scoreInput = it },
                            label = { Text("Nota Obtida") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = maxScoreInput,
                            onValueChange = { maxScoreInput = it },
                            label = { Text("Escala (Máx)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = coeffInput,
                        onValueChange = { coeffInput = it },
                        label = { Text("Coeficiente de Impacto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val score = scoreInput.toDoubleOrNull() ?: 0.0
                            val max = maxScoreInput.toDoubleOrNull() ?: 20.0
                            val coeff = coeffInput.toIntOrNull() ?: 1
                            if (subjectName.trim().isNotEmpty() && score >= 0) {
                                onSave(
                                    SubjectGrade(
                                        subjectName = subjectName.trim(),
                                        score = score,
                                        maxScore = max,
                                        coeff = coeff,
                                        term = selectedTerm
                                    )
                                )
                                // Clear
                                subjectName = ""
                                scoreInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Registar Nota", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                "Notas Registadas neste Trimestre (${grades.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(grades) { gradeItem ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(gradeItem.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Coef: ${gradeItem.coeff} • Nota: ${gradeItem.score}/${gradeItem.maxScore.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    IconButton(onClick = { onDelete(gradeItem.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
