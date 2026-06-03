package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchoolRepository(private val database: AppDatabase) {

    private val userProfileDao = database.userProfileDao()
    private val schoolOrderDao = database.schoolOrderDao()
    private val subjectGradeDao = database.subjectGradeDao()
    private val canteenFoodItemDao = database.canteenFoodItemDao()
    private val storeProductItemDao = database.storeProductItemDao()

    val profile: Flow<UserProfile?> = userProfileDao.getProfile()
    val allOrders: Flow<List<SchoolOrder>> = schoolOrderDao.getAllOrders()
    val allGrades: Flow<List<SubjectGrade>> = subjectGradeDao.getAllGrades()
    
    val allCanteenItems: Flow<List<CanteenFoodItem>> = canteenFoodItemDao.getAllCanteenItems()
    val allStoreItems: Flow<List<StoreProductItem>> = storeProductItemDao.getAllStoreItems()

    fun getGradesForTerm(term: String): Flow<List<SubjectGrade>> {
        return subjectGradeDao.getGradesByTerm(term)
    }

    suspend fun saveProfile(profile: UserProfile) {
        userProfileDao.insertProfile(profile)
    }

    suspend fun placeOrder(order: SchoolOrder) {
        schoolOrderDao.insertOrder(order)
    }

    suspend fun deleteOrder(orderId: Int) {
        schoolOrderDao.deleteOrderById(orderId)
    }

    // --- Admin editable tools ---
    suspend fun saveCanteenItem(item: CanteenFoodItem) {
        canteenFoodItemDao.insertCanteenItem(item)
    }

    suspend fun deleteCanteenItem(id: Int) {
        canteenFoodItemDao.deleteCanteenItemById(id)
    }

    suspend fun saveStoreItem(item: StoreProductItem) {
        storeProductItemDao.insertStoreItem(item)
    }

    suspend fun deleteStoreItem(id: Int) {
        storeProductItemDao.deleteStoreItemById(id)
    }

    suspend fun saveGrade(grade: SubjectGrade) {
        subjectGradeDao.insertGrade(grade)
    }

    suspend fun deleteGrade(id: Int) {
        subjectGradeDao.deleteGradeById(id)
    }

    suspend fun populateDefaultGradesIfEmpty() {
        val currentGrades = allGrades.first()
        if (currentGrades.isEmpty()) {
            val defaultGrades = listOf(
                // 1º Trimestre
                SubjectGrade(subjectName = "Matemática", score = 17.5, maxScore = 20.0, coeff = 5, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Língua Portuguesa", score = 15.0, maxScore = 20.0, coeff = 4, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Física e Química", score = 16.0, maxScore = 20.0, coeff = 4, term = "1º Trimestre"),
                SubjectGrade(subjectName = "História e Geografia", score = 14.5, maxScore = 20.0, coeff = 3, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Biologia e Geologia", score = 15.5, maxScore = 20.0, coeff = 3, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Inglês", score = 18.0, maxScore = 20.0, coeff = 3, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Artes Visuais", score = 19.0, maxScore = 20.0, coeff = 2, term = "1º Trimestre"),
                SubjectGrade(subjectName = "Educação Física", score = 16.5, maxScore = 20.0, coeff = 2, term = "1º Trimestre"),

                // 2º Trimestre
                SubjectGrade(subjectName = "Matemática", score = 18.0, maxScore = 20.0, coeff = 5, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Língua Portuguesa", score = 16.5, maxScore = 20.0, coeff = 4, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Física e Química", score = 17.0, maxScore = 20.0, coeff = 4, term = "2º Trimestre"),
                SubjectGrade(subjectName = "História e Geografia", score = 15.0, maxScore = 20.0, coeff = 3, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Biologia e Geologia", score = 16.0, maxScore = 20.0, coeff = 3, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Inglês", score = 18.5, maxScore = 20.0, coeff = 3, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Artes Visuais", score = 18.0, maxScore = 20.0, coeff = 2, term = "2º Trimestre"),
                SubjectGrade(subjectName = "Educação Física", score = 17.0, maxScore = 20.0, coeff = 2, term = "2º Trimestre"),

                // 3º Trimestre
                SubjectGrade(subjectName = "Matemática", score = 19.0, maxScore = 20.0, coeff = 5, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Língua Portuguesa", score = 17.0, maxScore = 20.0, coeff = 4, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Física e Química", score = 18.5, maxScore = 20.0, coeff = 4, term = "3º Trimestre"),
                SubjectGrade(subjectName = "História e Geografia", score = 16.0, maxScore = 20.0, coeff = 3, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Biologia e Geologia", score = 17.5, maxScore = 20.0, coeff = 3, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Inglês", score = 19.5, maxScore = 20.0, coeff = 3, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Artes Visuais", score = 19.5, maxScore = 20.0, coeff = 2, term = "3º Trimestre"),
                SubjectGrade(subjectName = "Educação Física", score = 18.0, maxScore = 20.0, coeff = 2, term = "3º Trimestre")
            )
            subjectGradeDao.insertGrades(defaultGrades)
        }
    }

    suspend fun populateDefaultCanteenAndStoreIfEmpty() {
        // Canteen items Toca do Grilo
        val currentCanteen = allCanteenItems.first()
        if (currentCanteen.isEmpty() || currentCanteen.any { it.price < 100.0 }) {
            if (currentCanteen.isNotEmpty()) {
                canteenFoodItemDao.clearAllCanteenItems()
            }
            val defaultCanteen = listOf(
                CanteenFoodItem(
                    name = "Bolinho de Chouriço",
                    description = "Delicioso salgado frito, recheado com pedaços do tradicional chouriço português.",
                    price = 1500.00,
                    category = "Salgados",
                    emoji = "🧆"
                ),
                CanteenFoodItem(
                    name = "Bola de Berlim",
                    description = "Massa fofa polvilhada com açúcar e generosamente recheada com doce de ovos caseiro.",
                    price = 1200.00,
                    category = "Sobremesas",
                    emoji = "🍩"
                ),
                CanteenFoodItem(
                    name = "Hambúrguer",
                    description = "Hambúrguer com carne selecionada grelhada, queijo fatiado derretido e alface crocante no pão brioche.",
                    price = 4500.00,
                    category = "Refeições",
                    emoji = "🍔"
                ),
                CanteenFoodItem(
                    name = "Fahita",
                    description = "Fajita de tortilha mexicana enrolada com deliciosas tiras de frango grelhado, pimentos e vinagrete.",
                    price = 3800.00,
                    category = "Refeições",
                    emoji = "🌯"
                ),
                CanteenFoodItem(
                    name = "Cachorro",
                    description = "Pão de cachorro quente com salsicha alemã grelhada, batata palha fina crocante e molhos.",
                    price = 2500.00,
                    category = "Refeições",
                    emoji = "🌭"
                ),
                CanteenFoodItem(
                    name = "Sambapito",
                    description = "Chupa-chupa tradicional doce e colorido em forma de espiral com sabor super frutado.",
                    price = 500.00,
                    category = "Guloseimas",
                    emoji = "🍭"
                ),
                CanteenFoodItem(
                    name = "Pastilha",
                    description = "Pastilha elástica refrescante com sabor mentolado intenso ou de morango frutado.",
                    price = 200.00,
                    category = "Guloseimas",
                    emoji = "🍬"
                ),
                CanteenFoodItem(
                    name = "Bolachas",
                    description = "Bolachas caseiras americanas crocantes com pepitas ricas de chocolate preto e baunilha.",
                    price = 1000.00,
                    category = "Guloseimas",
                    emoji = "🍪"
                ),
                CanteenFoodItem(
                    name = "Iogurte",
                    description = "Iogurte grego natural cremoso com delicioso topping de fruta fresca (morango ou pêssego).",
                    price = 1200.00,
                    category = "Laticínios",
                    emoji = "🥛"
                ),
                CanteenFoodItem(
                    name = "Refrigerantes",
                    description = "Lata fresca de refrigerante (Coca-Cola, Guaraná Antarctica ou Sumol Orange).",
                    price = 1500.00,
                    category = "Bebidas",
                    emoji = "🥤"
                )
            )
            canteenFoodItemDao.insertCanteenItems(defaultCanteen)
        }

        // Store products
        val currentStore = allStoreItems.first()
        if (currentStore.isEmpty() || currentStore.any { it.price < 100.0 }) {
            if (currentStore.isNotEmpty()) {
                storeProductItemDao.clearAllStoreItems()
            }
            val defaultStore = listOf(
                StoreProductItem(
                    name = "Caderno Inteligente Freinet",
                    description = "Formato A5 com capa de linho e folhas pontilhadas premium para projetos e resumos.",
                    price = 2500.00,
                    category = "Material",
                    emoji = "📓"
                ),
                StoreProductItem(
                    name = "Estojo de Geometria",
                    description = "Kit completo com compasso profissional resistente, esquadro, transferidor e régua de 15cm.",
                    price = 3500.00,
                    category = "Material",
                    emoji = "📐"
                ),
                StoreProductItem(
                    name = "Estojo Escola Cívico",
                    description = "Estojo com acabamento azul escuro reforçado e brasão oficial do Complexo Escolar Freinet.",
                    price = 4500.00,
                    category = "Material",
                    emoji = "✏️"
                ),
                StoreProductItem(
                    name = "Canetas de Gel Freinet",
                    description = "Conjunto de 4 canetas de secagem rápida com as cores padrão de escrita escolar.",
                    price = 1800.00,
                    category = "Material",
                    emoji = "🖊️"
                ),
                StoreProductItem(
                    name = "Sudadera/Moletom Freinet",
                    description = "Casaco térmico e super macio com capuz bordado, cores oficiais da nossa escola (azul e laranja).",
                    price = 25000.00,
                    category = "Vestuário",
                    emoji = "🧥"
                ),
                StoreProductItem(
                    name = "Mochila Escolar Freinet",
                    description = "Design ergonómico com múltiplos fechos, costas acolchoadas e suporte duplo de portáteis.",
                    price = 30000.00,
                    category = "Vestuário",
                    emoji = "🎒"
                ),
                StoreProductItem(
                    name = "Garrafa Térmica Aço Inox",
                    description = "Mantém a bebida bem climatizada. Capacidade 750ml, livre de BPA, vedante hermético de silicone.",
                    price = 10000.00,
                    category = "Artigos",
                    emoji = "🧪"
                ),
                // Uniforms added as requested - category: "Uniformes" for automatic "Coming Soon" styling
                StoreProductItem(
                    name = "Bata Oficial Freinet",
                    description = "Bata branca tradicional com bordado oficial de alta qualidade do Complexo Freinet.",
                    price = 8500.00,
                    category = "Uniformes",
                    emoji = "🥼"
                ),
                StoreProductItem(
                    name = "Polo Oficial Freinet",
                    description = "Polo de algodão respirável, ideal para uso diário no ambiente letivo cooperativo.",
                    price = 6500.00,
                    category = "Uniformes",
                    emoji = "👕"
                ),
                StoreProductItem(
                    name = "Fato de Treino ED. Física",
                    description = "Conjunto de desporto oficial macio e resistente com casaco com fecho e calça desportiva.",
                    price = 15000.00,
                    category = "Uniformes",
                    emoji = "👟"
                )
            )
            storeProductItemDao.insertStoreItems(defaultStore)
        }
    }
}
