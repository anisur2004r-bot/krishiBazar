package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.KrishiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FarmerTab(val labelBn: String) {
    HOME("হোম"),
    PRODUCTS("আমার পণ্য"),
    DEMANDS("ক্রেতার চাহিদা"),
    ORDERS("চলমান অর্ডার"),
    PROFILE("প্রোফাইল")
}

enum class BuyerTab(val labelBn: String) {
    HOME("হোম"),
    SEARCH("পণ্য খুঁজুন"),
    DEMANDS("আমার চাহিদা"),
    ORDERS("অর্ডারসমূহ"),
    PROFILE("প্রোফাইল")
}

enum class AdminTab(val labelBn: String) {
    OVERVIEW("ওভারভিউ"),
    FARMERS("কৃষক যাচাই"),
    BUYERS("ক্রেতা যাচাই"),
    PRODUCTS("পণ্য অনুমোদন"),
    DEMANDS("চাহিদা তদারকি"),
    ORDERS("অর্ডার ট্র্যাকিং"),
    DISPUTES("অভিযোগ নিষ্পত্তি")
}

data class UiState(
    val currentRole: UserRole = UserRole.FARMER,
    val farmerTab: FarmerTab = FarmerTab.HOME,
    val buyerTab: BuyerTab = BuyerTab.HOME,
    val adminTab: AdminTab = AdminTab.OVERVIEW,
    // Search / Filter states
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val selectedDistrict: String = "সকল জেলা",
    val verifiedOnlyFilter: Boolean = false,
    val demandSearchQuery: String = "",
    // Active Dialogs & Sheets
    val showAddProductDialog: Boolean = false,
    val showAddDemandDialog: Boolean = false,
    val activeDemandForOffer: BuyerDemand? = null,
    val activeDemandForOfferManagement: BuyerDemand? = null,
    val activeOrderForDetail: MarketplaceOrder? = null,
    val activeOrderForChat: MarketplaceOrder? = null,
    val activeOrderForDispute: MarketplaceOrder? = null,
    val activeOrderForRating: MarketplaceOrder? = null,
    val activeOrderForVerification: MarketplaceOrder? = null,
    val showNotificationsSheet: Boolean = false,
    val showRoleSwitcherDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class KrishiViewModel(
    private val repository: KrishiRepository = KrishiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Data streams from repository
    val farmers: StateFlow<List<FarmerProfile>> = repository.farmers
    val buyers: StateFlow<List<BuyerProfile>> = repository.buyers
    val products: StateFlow<List<ProductListing>> = repository.products
    val demands: StateFlow<List<BuyerDemand>> = repository.demands
    val offers: StateFlow<List<FarmerOffer>> = repository.offers
    val orders: StateFlow<List<MarketplaceOrder>> = repository.orders
    val disputes: StateFlow<List<Dispute>> = repository.disputes
    val reviews: StateFlow<List<UserReview>> = repository.reviews
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages

    val currentFarmer: StateFlow<FarmerProfile?> = combine(farmers, repository.currentFarmerId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentBuyer: StateFlow<BuyerProfile?> = combine(buyers, repository.currentBuyerId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
        _uiState.update { it.copy(currentRole = role, showRoleSwitcherDialog = false) }
    }

    fun setFarmerTab(tab: FarmerTab) {
        _uiState.update { it.copy(farmerTab = tab) }
    }

    fun setBuyerTab(tab: BuyerTab) {
        _uiState.update { it.copy(buyerTab = tab) }
    }

    fun setAdminTab(tab: AdminTab) {
        _uiState.update { it.copy(adminTab = tab) }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setSelectedCategory(cat: ProductCategory?) {
        _uiState.update { it.copy(selectedCategory = if (it.selectedCategory == cat) null else cat) }
    }

    fun setSelectedDistrict(dist: String) {
        _uiState.update { it.copy(selectedDistrict = dist) }
    }

    fun toggleVerifiedOnlyFilter() {
        _uiState.update { it.copy(verifiedOnlyFilter = !it.verifiedOnlyFilter) }
    }

    fun setDemandSearchQuery(q: String) {
        _uiState.update { it.copy(demandSearchQuery = q) }
    }

    // Dialog state controllers
    fun openAddProductDialog() { _uiState.update { it.copy(showAddProductDialog = true) } }
    fun closeAddProductDialog() { _uiState.update { it.copy(showAddProductDialog = false) } }

    fun openAddDemandDialog() { _uiState.update { it.copy(showAddDemandDialog = true) } }
    fun closeAddDemandDialog() { _uiState.update { it.copy(showAddDemandDialog = false) } }

    fun openOfferDialog(demand: BuyerDemand) { _uiState.update { it.copy(activeDemandForOffer = demand) } }
    fun closeOfferDialog() { _uiState.update { it.copy(activeDemandForOffer = null) } }

    fun openDemandOfferManagement(demand: BuyerDemand) { _uiState.update { it.copy(activeDemandForOfferManagement = demand) } }
    fun closeDemandOfferManagement() { _uiState.update { it.copy(activeDemandForOfferManagement = null) } }

    fun openOrderDetail(order: MarketplaceOrder) { _uiState.update { it.copy(activeOrderForDetail = order) } }
    fun closeOrderDetail() { _uiState.update { it.copy(activeOrderForDetail = null) } }

    fun openChat(order: MarketplaceOrder) { _uiState.update { it.copy(activeOrderForChat = order) } }
    fun closeChat() { _uiState.update { it.copy(activeOrderForChat = null) } }

    fun openDisputeDialog(order: MarketplaceOrder) { _uiState.update { it.copy(activeOrderForDispute = order) } }
    fun closeDisputeDialog() { _uiState.update { it.copy(activeOrderForDispute = null) } }

    fun openRatingDialog(order: MarketplaceOrder) { _uiState.update { it.copy(activeOrderForRating = order) } }
    fun closeRatingDialog() { _uiState.update { it.copy(activeOrderForRating = null) } }

    fun openVerificationDialog(order: MarketplaceOrder) { _uiState.update { it.copy(activeOrderForVerification = order) } }
    fun closeVerificationDialog() { _uiState.update { it.copy(activeOrderForVerification = null) } }

    fun openNotifications() { _uiState.update { it.copy(showNotificationsSheet = true) } }
    fun closeNotifications() { _uiState.update { it.copy(showNotificationsSheet = false) } }

    fun openRoleSwitcher() { _uiState.update { it.copy(showRoleSwitcherDialog = true) } }
    fun closeRoleSwitcher() { _uiState.update { it.copy(showRoleSwitcherDialog = false) } }

    fun showSnackbar(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }
    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // --- Action dispatchers ---
    fun submitProduct(
        title: String,
        category: ProductCategory,
        quantity: Double,
        unit: ProductUnit,
        expectedPrice: Double,
        minPrice: Double,
        location: String,
        availableDate: String,
        harvestDate: String,
        qualityGrade: QualityGrade,
        description: String
    ) {
        repository.addProduct(
            title = title,
            category = category,
            quantity = quantity,
            unit = unit,
            expectedPrice = expectedPrice,
            minPrice = minPrice,
            location = location,
            availableDate = availableDate,
            harvestDate = harvestDate,
            qualityGrade = qualityGrade,
            description = description
        )
        closeAddProductDialog()
        showSnackbar("পণ্য লিস্টিং সফল! অ্যাডমিন অনুমোদনের পর সক্রিয় হবে।")
    }

    fun submitDemand(
        productTitle: String,
        category: ProductCategory,
        requiredQuantity: Double,
        unit: ProductUnit,
        requiredLocation: String,
        requiredDate: String,
        minExpectedPrice: Double,
        maxExpectedPrice: Double,
        qualityGrade: QualityGrade,
        additionalNote: String
    ) {
        repository.addDemand(
            productTitle = productTitle,
            category = category,
            requiredQuantity = requiredQuantity,
            unit = unit,
            requiredLocation = requiredLocation,
            requiredDate = requiredDate,
            minExpectedPrice = minExpectedPrice,
            maxExpectedPrice = maxExpectedPrice,
            qualityGrade = qualityGrade,
            additionalNote = additionalNote
        )
        closeAddDemandDialog()
        showSnackbar("চাহিদা পোস্ট সফল হয়েছে! কৃষকেরা অফার পাঠাতে পারবেন।")
    }

    fun submitOffer(
        demandId: String,
        offeredQuantity: Double,
        unit: ProductUnit,
        pricePerUnit: Double,
        qualityGrade: QualityGrade,
        availableDate: String,
        note: String
    ) {
        repository.sendOffer(
            demandId = demandId,
            offeredQuantity = offeredQuantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            qualityGrade = qualityGrade,
            availableDate = availableDate,
            note = note
        )
        closeOfferDialog()
        showSnackbar("অফার পাঠানো সফল হয়েছে! ক্রেতা গ্রহণ করলে জানানো হবে।")
    }

    fun acceptOffer(offerId: String) {
        val newOrder = repository.acceptOffer(offerId)
        if (newOrder != null) {
            closeDemandOfferManagement()
            openOrderDetail(newOrder)
            showSnackbar("অফার গৃহীত হয়েছে! অর্ডার তৈরি হয়েছে #${newOrder.orderNumber}")
        }
    }

    fun orderDirectProduct(productId: String, quantity: Double) {
        val newOrder = repository.createDirectOrder(productId, quantity)
        if (newOrder != null) {
            openOrderDetail(newOrder)
            showSnackbar("অর্ডার তৈরি হয়েছে #${newOrder.orderNumber}। ডিপোজিট পে করুন।")
        }
    }

    fun payDeposit(orderId: String) {
        repository.payDeposit(orderId)
        // Refresh active order detail
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("পেমেন্ট সফল! ডিপোজিট নিশ্চিত করা হয়েছে ✅")
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        repository.updateOrderStatus(orderId, newStatus)
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("অর্ডার স্ট্যাটাস আপডেট: ${newStatus.labelBn}")
    }

    fun advanceTransport(orderId: String, nextStatus: TransportStatus) {
        repository.updateTransportStatus(orderId, nextStatus)
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("পরিবহন অবস্থা আপডেট: ${nextStatus.labelBn}")
    }

    fun submitWeightVerification(
        orderId: String,
        actualWeight: Double,
        qualityGrade: QualityGrade,
        notes: String
    ) {
        repository.updateQualityVerification(orderId, actualWeight, qualityGrade, notes)
        closeVerificationDialog()
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("ওজন ও কোয়ালিটি যাচাই সম্পন্ন ✅")
    }

    fun submitDispute(orderId: String, problemType: ProblemType, description: String) {
        repository.reportProblem(orderId, problemType, description)
        closeDisputeDialog()
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("সমস্যা রিপোর্ট করা হয়েছে। অ্যাডমিন তদন্ত করবে।")
    }

    fun submitRating(
        orderId: String,
        stars: Int,
        productQuality: Int,
        quantityAccuracy: Int,
        communication: Int,
        reliability: Int,
        comment: String
    ) {
        repository.rateOrder(
            orderId = orderId,
            stars = stars,
            productQuality = productQuality,
            quantityAccuracy = quantityAccuracy,
            communication = communication,
            reliability = reliability,
            comment = comment
        )
        closeRatingDialog()
        val updated = orders.value.find { it.id == orderId }
        if (updated != null) {
            _uiState.update { it.copy(activeOrderForDetail = updated) }
        }
        showSnackbar("রেটিং ও রিভিউ জমা দেওয়া হয়েছে! ধন্যবাদ। ⭐")
    }

    fun sendChatMessage(orderId: String, text: String) {
        if (text.isNotBlank()) {
            repository.sendMessage(orderId, text)
        }
    }

    // Admin moderation handlers
    fun verifyFarmer(farmerId: String, status: VerificationStatus) {
        repository.updateFarmerVerification(farmerId, status)
        showSnackbar("কৃষকের স্ট্যাটাস আপডেট: ${status.labelBn}")
    }

    fun verifyBuyer(buyerId: String, status: VerificationStatus) {
        repository.updateBuyerVerification(buyerId, status)
        showSnackbar("ক্রেতার স্ট্যাটাস আপডেট: ${status.labelBn}")
    }

    fun moderateProduct(productId: String, status: ProductStatus) {
        repository.updateProductStatus(productId, status)
        showSnackbar("পণ্য স্ট্যাটাস আপডেট: ${status.labelBn}")
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
        showSnackbar("পণ্য মুছে ফেলা হয়েছে")
    }

    fun resolveDispute(disputeId: String, status: DisputeStatus, notes: String) {
        repository.resolveDispute(disputeId, status, notes)
        showSnackbar("ডিসপ্যুট আপডেট: ${status.labelBn}")
    }

    fun selectFarmerProfile(farmerId: String) {
        repository.setFarmer(farmerId)
    }

    fun selectBuyerProfile(buyerId: String) {
        repository.setBuyer(buyerId)
    }
}
