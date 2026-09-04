package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class KrishiRepository {

    // Current Session / Active Role
    private val _currentRole = MutableStateFlow(UserRole.FARMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentFarmerId = MutableStateFlow("farmer_1")
    val currentFarmerId: StateFlow<String> = _currentFarmerId.asStateFlow()

    private val _currentBuyerId = MutableStateFlow("buyer_1")
    val currentBuyerId: StateFlow<String> = _currentBuyerId.asStateFlow()

    // Entities
    private val _farmers = MutableStateFlow<List<FarmerProfile>>(emptyList())
    val farmers: StateFlow<List<FarmerProfile>> = _farmers.asStateFlow()

    private val _buyers = MutableStateFlow<List<BuyerProfile>>(emptyList())
    val buyers: StateFlow<List<BuyerProfile>> = _buyers.asStateFlow()

    private val _products = MutableStateFlow<List<ProductListing>>(emptyList())
    val products: StateFlow<List<ProductListing>> = _products.asStateFlow()

    private val _demands = MutableStateFlow<List<BuyerDemand>>(emptyList())
    val demands: StateFlow<List<BuyerDemand>> = _demands.asStateFlow()

    private val _offers = MutableStateFlow<List<FarmerOffer>>(emptyList())
    val offers: StateFlow<List<FarmerOffer>> = _offers.asStateFlow()

    private val _orders = MutableStateFlow<List<MarketplaceOrder>>(emptyList())
    val orders: StateFlow<List<MarketplaceOrder>> = _orders.asStateFlow()

    private val _disputes = MutableStateFlow<List<Dispute>>(emptyList())
    val disputes: StateFlow<List<Dispute>> = _disputes.asStateFlow()

    private val _reviews = MutableStateFlow<List<UserReview>>(emptyList())
    val reviews: StateFlow<List<UserReview>> = _reviews.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        seedInitialData()
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setFarmer(farmerId: String) {
        _currentFarmerId.value = farmerId
    }

    fun setBuyer(buyerId: String) {
        _currentBuyerId.value = buyerId
    }

    private fun currentTimeFormatted(): String {
        return SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date())
    }

    // --- Farmer Registration & Moderation ---
    fun registerFarmer(
        name: String,
        phone: String,
        district: String,
        upazila: String,
        union: String,
        address: String,
        farmerType: String
    ): FarmerProfile {
        val newFarmer = FarmerProfile(
            id = "farmer_${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            district = district,
            upazila = upazila,
            union = union,
            address = address,
            farmerType = farmerType,
            verificationStatus = VerificationStatus.PENDING,
            totalCompletedOrders = 0,
            rating = 5.0,
            reviewsCount = 0
        )
        _farmers.value = listOf(newFarmer) + _farmers.value
        _currentFarmerId.value = newFarmer.id

        addNotification(
            role = UserRole.ADMIN,
            title = "নতুন কৃষক ভেরিফিকেশন আবেদন",
            message = "${newFarmer.name} (${newFarmer.district}) অ্যাকাউন্ট ভেরিফিকেশনের জন্য আবেদন করেছেন।"
        )
        return newFarmer
    }

    fun updateFarmerVerification(farmerId: String, status: VerificationStatus) {
        _farmers.value = _farmers.value.map {
            if (it.id == farmerId) it.copy(verificationStatus = status) else it
        }
        val farmer = _farmers.value.find { it.id == farmerId }
        farmer?.let {
            addNotification(
                role = UserRole.FARMER,
                targetUserId = farmer.id,
                title = if (status == VerificationStatus.VERIFIED) "অভিনন্দন! আপনার প্রোফাইল ভেরিফাইড ✅" else "প্রোফাইল স্ট্যাটাস পরিবর্তন",
                message = "অ্যাডমিন কর্তৃক আপনার প্রোফাইল স্ট্যাটাস ${status.labelBn} হিসেবে আপডেট করা হয়েছে।"
            )
        }
    }

    // --- Buyer Registration & Moderation ---
    fun registerBuyer(
        name: String,
        phone: String,
        businessName: String,
        businessType: String,
        district: String,
        area: String,
        address: String
    ): BuyerProfile {
        val newBuyer = BuyerProfile(
            id = "buyer_${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            businessName = businessName,
            businessType = businessType,
            district = district,
            area = area,
            address = address,
            verificationStatus = VerificationStatus.PENDING,
            completedOrders = 0,
            rating = 5.0,
            reviewsCount = 0
        )
        _buyers.value = listOf(newBuyer) + _buyers.value
        _currentBuyerId.value = newBuyer.id

        addNotification(
            role = UserRole.ADMIN,
            title = "নতুন ক্রেতা ভেরিফিকেশন আবেদন",
            message = "${newBuyer.businessName} (${newBuyer.name}) অ্যাকাউন্ট ভেরিফিকেশনের আবেদন করেছেন।"
        )
        return newBuyer
    }

    fun updateBuyerVerification(buyerId: String, status: VerificationStatus) {
        _buyers.value = _buyers.value.map {
            if (it.id == buyerId) it.copy(verificationStatus = status) else it
        }
    }

    // --- Product Actions ---
    fun addProduct(
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
        val currentFarmer = _farmers.value.find { it.id == _currentFarmerId.value }
            ?: _farmers.value.first()
        val newProduct = ProductListing(
            id = "prod_${System.currentTimeMillis()}",
            farmerId = currentFarmer.id,
            farmerName = currentFarmer.name,
            farmerDistrict = currentFarmer.district,
            farmerVerified = currentFarmer.verificationStatus == VerificationStatus.VERIFIED,
            title = title,
            category = category,
            quantity = quantity,
            remainingQuantity = quantity,
            unit = unit,
            expectedPrice = expectedPrice,
            minPrice = minPrice,
            location = location,
            availableDate = availableDate,
            harvestDate = harvestDate,
            qualityGrade = qualityGrade,
            description = description,
            status = ProductStatus.PENDING, // Pending Review by Admin
            createdAt = currentTimeFormatted()
        )
        _products.value = listOf(newProduct) + _products.value

        addNotification(
            role = UserRole.ADMIN,
            title = "নতুন পণ্য অনুমোদনের অপেক্ষায়",
            message = "${currentFarmer.name} '${title}' (${quantity} ${unit.labelBn}) লিস্টিং করেছেন। অনুমোদনের অনুরোধ রইলো।"
        )
    }

    fun updateProductStatus(productId: String, status: ProductStatus) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(status = status) else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    // --- Buyer Demand Actions ---
    fun addDemand(
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
        val currentBuyer = _buyers.value.find { it.id == _currentBuyerId.value }
            ?: _buyers.value.first()
        val newDemand = BuyerDemand(
            id = "dem_${System.currentTimeMillis()}",
            buyerId = currentBuyer.id,
            buyerName = currentBuyer.name,
            buyerBusinessName = currentBuyer.businessName,
            buyerDistrict = currentBuyer.district,
            buyerVerified = currentBuyer.verificationStatus == VerificationStatus.VERIFIED,
            productTitle = productTitle,
            category = category,
            requiredQuantity = requiredQuantity,
            fulfilledQuantity = 0.0,
            unit = unit,
            requiredLocation = requiredLocation,
            requiredDate = requiredDate,
            minExpectedPrice = minExpectedPrice,
            maxExpectedPrice = maxExpectedPrice,
            qualityGrade = qualityGrade,
            additionalNote = additionalNote,
            status = DemandStatus.ACTIVE,
            offersCount = 0,
            createdAt = currentTimeFormatted()
        )
        _demands.value = listOf(newDemand) + _demands.value

        addNotification(
            role = UserRole.FARMER,
            title = "ঢাকার নতুন পণ্য চাহিদা পোস্ট হয়েছে! 📢",
            message = "${currentBuyer.businessName}: ${requiredQuantity} ${unit.labelBn} ${productTitle} প্রয়োজন (${requiredLocation})।"
        )
    }

    // --- Farmer Offer Actions ---
    fun sendOffer(
        demandId: String,
        offeredQuantity: Double,
        unit: ProductUnit,
        pricePerUnit: Double,
        qualityGrade: QualityGrade,
        availableDate: String,
        note: String
    ) {
        val currentFarmer = _farmers.value.find { it.id == _currentFarmerId.value }
            ?: _farmers.value.first()
        val demand = _demands.value.find { it.id == demandId } ?: return

        val newOffer = FarmerOffer(
            id = "off_${System.currentTimeMillis()}",
            demandId = demandId,
            farmerId = currentFarmer.id,
            farmerName = currentFarmer.name,
            farmerPhone = currentFarmer.phone,
            farmerLocation = "${currentFarmer.upazila}, ${currentFarmer.district}",
            farmerVerified = currentFarmer.verificationStatus == VerificationStatus.VERIFIED,
            offeredQuantity = offeredQuantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            qualityGrade = qualityGrade,
            availableDate = availableDate,
            note = note,
            status = OfferStatus.PENDING,
            createdAt = currentTimeFormatted()
        )
        _offers.value = listOf(newOffer) + _offers.value

        // update demand offer count
        _demands.value = _demands.value.map {
            if (it.id == demandId) it.copy(offersCount = it.offersCount + 1, status = DemandStatus.OFFER_RECEIVED) else it
        }

        addNotification(
            role = UserRole.BUYER,
            targetUserId = demand.buyerId,
            title = "আপনার চাহিদায় নতুন অফার এসেছে!",
            message = "কৃষক ${currentFarmer.name} ${offeredQuantity} ${unit.labelBn} ${demand.productTitle} সরবরাহের অফার দিয়েছেন (৳${pricePerUnit}/${unit.labelBn})।"
        )
    }

    // --- Buyer Accepts Offer & Order Creation ---
    fun acceptOffer(offerId: String): MarketplaceOrder? {
        val offer = _offers.value.find { it.id == offerId } ?: return null
        val demand = _demands.value.find { it.id == offer.demandId } ?: return null
        val buyer = _buyers.value.find { it.id == demand.buyerId } ?: _buyers.value.first()
        val farmer = _farmers.value.find { it.id == offer.farmerId } ?: _farmers.value.first()

        val totalAmount = offer.offeredQuantity * offer.pricePerUnit
        val deposit = totalAmount * 0.20 // 20% required deposit

        val orderNumber = "KB-${1000 + _orders.value.size + 1}"
        val newOrder = MarketplaceOrder(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = orderNumber,
            demandId = demand.id,
            offerId = offer.id,
            buyerId = buyer.id,
            buyerName = buyer.name,
            buyerBusinessName = buyer.businessName,
            buyerPhone = buyer.phone,
            farmerId = farmer.id,
            farmerName = farmer.name,
            farmerPhone = farmer.phone,
            farmerLocation = "${farmer.upazila}, ${farmer.district}",
            productTitle = demand.productTitle,
            category = demand.category,
            quantity = offer.offeredQuantity,
            unit = offer.unit,
            pricePerUnit = offer.pricePerUnit,
            totalAmount = totalAmount,
            depositRequired = deposit,
            isDepositPaid = false,
            orderStatus = OrderStatus.PAYMENT_PENDING,
            deliveryLocation = demand.requiredLocation,
            expectedDeliveryDate = demand.requiredDate,
            deliveryInfo = DeliveryInfo(
                pickupLocation = "${farmer.union}, ${farmer.upazila}, ${farmer.district}",
                collectionCenter = "${farmer.district} এগ্রিকালচার হাব ও কালেকশন পয়েন্ট",
                deliveryLocation = demand.requiredLocation,
                transportStatus = TransportStatus.WAITING
            ),
            verification = QualityVerification(
                expectedWeight = offer.offeredQuantity,
                actualWeight = offer.offeredQuantity,
                unit = offer.unit,
                qualityGrade = offer.qualityGrade
            ),
            createdAt = currentTimeFormatted()
        )

        // Mark offer accepted
        _offers.value = _offers.value.map {
            if (it.id == offerId) it.copy(status = OfferStatus.ACCEPTED) else it
        }

        // Update demand status and fulfilled quantity
        _demands.value = _demands.value.map {
            if (it.id == demand.id) {
                val newFulfilled = it.fulfilledQuantity + offer.offeredQuantity
                val newStatus = if (newFulfilled >= it.requiredQuantity) DemandStatus.FULFILLED else DemandStatus.ORDERED
                it.copy(fulfilledQuantity = newFulfilled, status = newStatus)
            } else it
        }

        _orders.value = listOf(newOrder) + _orders.value

        addNotification(
            role = UserRole.FARMER,
            targetUserId = farmer.id,
            title = "অফার গৃহীত হয়েছে! অর্ডার #${newOrder.orderNumber}",
            message = "${buyer.businessName} আপনার অফার গ্রহণ করেছে। ডিপোজিট জমা হলে ফসল তোলার কাজ শুরু করুন।"
        )

        addNotification(
            role = UserRole.BUYER,
            targetUserId = buyer.id,
            title = "অর্ডার নিশ্চিত করতে ডিপোজিট পে করুন",
            message = "অর্ডার #${newOrder.orderNumber}-এর জন্য ২০% ডিপোজিট (৳${deposit.toInt()}) প্রদান করুন।"
        )

        return newOrder
    }

    // --- Order Direct Purchase (Buyer ordering directly from farmer's product listing) ---
    fun createDirectOrder(productId: String, quantity: Double): MarketplaceOrder? {
        val product = _products.value.find { it.id == productId } ?: return null
        val buyer = _buyers.value.find { it.id == _currentBuyerId.value } ?: _buyers.value.first()
        val farmer = _farmers.value.find { it.id == product.farmerId } ?: _farmers.value.first()

        val totalAmount = quantity * product.expectedPrice
        val deposit = totalAmount * 0.20
        val orderNumber = "KB-${1000 + _orders.value.size + 1}"

        val newOrder = MarketplaceOrder(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = orderNumber,
            buyerId = buyer.id,
            buyerName = buyer.name,
            buyerBusinessName = buyer.businessName,
            buyerPhone = buyer.phone,
            farmerId = farmer.id,
            farmerName = farmer.name,
            farmerPhone = farmer.phone,
            farmerLocation = product.location,
            productTitle = product.title,
            category = product.category,
            quantity = quantity,
            unit = product.unit,
            pricePerUnit = product.expectedPrice,
            totalAmount = totalAmount,
            depositRequired = deposit,
            isDepositPaid = false,
            orderStatus = OrderStatus.PAYMENT_PENDING,
            deliveryLocation = "${buyer.area}, ${buyer.district}",
            expectedDeliveryDate = product.availableDate,
            deliveryInfo = DeliveryInfo(
                pickupLocation = product.location,
                collectionCenter = "${product.farmerDistrict} এগ্রিকালচার হাব ও কালেকশন পয়েন্ট",
                deliveryLocation = "${buyer.area}, ${buyer.district}",
                transportStatus = TransportStatus.WAITING
            ),
            verification = QualityVerification(
                expectedWeight = quantity,
                actualWeight = quantity,
                unit = product.unit,
                qualityGrade = product.qualityGrade
            ),
            createdAt = currentTimeFormatted()
        )

        // Update product remaining quantity
        _products.value = _products.value.map {
            if (it.id == productId) {
                val rem = it.remainingQuantity - quantity
                val newStatus = if (rem <= 0) ProductStatus.SOLD else ProductStatus.PARTIALLY_SOLD
                it.copy(remainingQuantity = maxOf(0.0, rem), status = newStatus)
            } else it
        }

        _orders.value = listOf(newOrder) + _orders.value
        return newOrder
    }

    // --- Mock Deposit Payment Flow ---
    fun payDeposit(orderId: String) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                it.copy(
                    isDepositPaid = true,
                    orderStatus = OrderStatus.PAYMENT_CONFIRMED
                )
            } else it
        }

        val order = _orders.value.find { it.id == orderId }
        order?.let {
            addNotification(
                role = UserRole.FARMER,
                targetUserId = order.farmerId,
                title = "পেমেন্ট নিশ্চিত! ✅ ফসল তৈরি শুরু করুন",
                message = "অর্ডার #${order.orderNumber}-এর ডিপোজিট (৳${order.depositRequired.toInt()}) জমা হয়েছে। কৃষক ফসল সংগ্রহ সেন্টারে পাঠাতে পারেন।"
            )
            addNotification(
                role = UserRole.BUYER,
                targetUserId = order.buyerId,
                title = "পেমেন্ট সফল! ✅",
                message = "অর্ডার #${order.orderNumber}-এর ডিপোজিট পরিশোধ সম্পন্ন হয়েছে।"
            )
        }
    }

    // --- Order Status Lifecycle Management ---
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(orderStatus = newStatus) else it
        }

        val order = _orders.value.find { it.id == orderId } ?: return
        addNotification(
            role = null, // both
            title = "অর্ডার #${order.orderNumber} আপডেট",
            message = "অর্ডারের বর্তমান অবস্থা: ${newStatus.labelBn}"
        )
    }

    // --- Collection Stage Quality & Weight Verification ---
    fun updateQualityVerification(
        orderId: String,
        actualWeight: Double,
        qualityGrade: QualityGrade,
        notes: String
    ) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                val updatedVerification = it.verification.copy(
                    actualWeight = actualWeight,
                    qualityGrade = qualityGrade,
                    notes = notes,
                    isVerified = true,
                    verificationDate = currentTimeFormatted()
                )
                it.copy(
                    verification = updatedVerification,
                    orderStatus = if (it.orderStatus == OrderStatus.PREPARING) OrderStatus.COLLECTED else it.orderStatus
                )
            } else it
        }

        val order = _orders.value.find { it.id == orderId } ?: return
        addNotification(
            role = UserRole.BUYER,
            targetUserId = order.buyerId,
            title = "কালেকশন সেন্টারে ওজন ও কোয়ালিটি যাচাই সম্পন্ন ✅",
            message = "প্রত্যাশিত: ${order.quantity} ${order.unit.labelBn}, প্রাপ্ত: ${actualWeight} ${order.unit.labelBn}, মান: ${qualityGrade.labelBn}।"
        )
    }

    // --- Transport Status Progression ---
    fun updateTransportStatus(
        orderId: String,
        transportStatus: TransportStatus,
        driverName: String? = null,
        driverPhone: String? = null,
        vehicleNumber: String? = null
    ) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                val updatedDelivery = it.deliveryInfo.copy(
                    transportStatus = transportStatus,
                    driverName = driverName ?: it.deliveryInfo.driverName,
                    driverPhone = driverPhone ?: it.deliveryInfo.driverPhone,
                    vehicleNumber = vehicleNumber ?: it.deliveryInfo.vehicleNumber
                )
                val orderStatus = when (transportStatus) {
                    TransportStatus.PICKED_UP -> OrderStatus.COLLECTED
                    TransportStatus.IN_TRANSIT -> OrderStatus.IN_TRANSIT
                    TransportStatus.DELIVERED -> OrderStatus.DELIVERED
                    else -> it.orderStatus
                }
                it.copy(deliveryInfo = updatedDelivery, orderStatus = orderStatus)
            } else it
        }
    }

    // --- Dispute System ---
    fun reportProblem(
        orderId: String,
        problemType: ProblemType,
        description: String
    ) {
        val order = _orders.value.find { it.id == orderId } ?: return
        val currentRole = _currentRole.value
        val reporterName = if (currentRole == UserRole.FARMER) order.farmerName else order.buyerName
        val reporterPhone = if (currentRole == UserRole.FARMER) order.farmerPhone else order.buyerPhone

        val dispute = Dispute(
            id = "disp_${System.currentTimeMillis()}",
            orderId = order.id,
            orderNumber = order.orderNumber,
            reportedByRole = currentRole,
            reporterName = reporterName,
            reporterPhone = reporterPhone,
            problemType = problemType,
            description = description,
            status = DisputeStatus.OPEN,
            createdAt = currentTimeFormatted()
        )

        _disputes.value = listOf(dispute) + _disputes.value

        // Mark order as disputed
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(orderStatus = OrderStatus.DISPUTED, hasDispute = true) else it
        }

        addNotification(
            role = UserRole.ADMIN,
            title = "নতুন সমস্যা রিপোর্ট / Dispute ⚠️",
            message = "অর্ডার #${order.orderNumber}-এ ${problemType.labelBn} রিপোর্ট করা হয়েছে।"
        )
    }

    fun resolveDispute(disputeId: String, status: DisputeStatus, adminNotes: String) {
        _disputes.value = _disputes.value.map {
            if (it.id == disputeId) it.copy(status = status, adminNotes = adminNotes) else it
        }
    }

    // --- Rating System ---
    fun rateOrder(
        orderId: String,
        stars: Int,
        productQuality: Int,
        quantityAccuracy: Int,
        communication: Int,
        reliability: Int,
        comment: String
    ) {
        val order = _orders.value.find { it.id == orderId } ?: return
        val currentRole = _currentRole.value
        val fromName = if (currentRole == UserRole.FARMER) order.farmerName else order.buyerName
        val toUserId = if (currentRole == UserRole.FARMER) order.buyerId else order.farmerId

        val review = UserReview(
            id = "rev_${System.currentTimeMillis()}",
            orderId = orderId,
            fromUserName = fromName,
            fromUserRole = currentRole,
            toUserId = toUserId,
            stars = stars,
            productQualityRating = productQuality,
            quantityAccuracyRating = quantityAccuracy,
            communicationRating = communication,
            reliabilityRating = reliability,
            comment = comment,
            createdAt = currentTimeFormatted()
        )

        _reviews.value = listOf(review) + _reviews.value
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(isRated = true, orderStatus = OrderStatus.COMPLETED) else it
        }
    }

    // --- Chat System ---
    fun sendMessage(orderId: String, text: String) {
        val order = _orders.value.find { it.id == orderId } ?: return
        val role = _currentRole.value
        val senderName = if (role == UserRole.FARMER) order.farmerName else if (role == UserRole.BUYER) order.buyerName else "অ্যাডমিন"

        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            orderId = orderId,
            senderName = senderName,
            senderRole = role,
            message = text,
            timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    private fun addNotification(
        role: UserRole?,
        targetUserId: String? = null,
        title: String,
        message: String,
        orderId: String? = null,
        demandId: String? = null
    ) {
        val notif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            targetRole = role,
            targetUserId = targetUserId,
            title = title,
            message = message,
            timestamp = "এখনই",
            isRead = false,
            relatedOrderId = orderId,
            relatedDemandId = demandId
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    // --- Rich Seed Data (10+ Farmers, 10+ Buyers, 20+ Products, 10+ Demands, 20+ Offers, 10+ Orders) ---
    private fun seedInitialData() {
        // 10 Farmers
        val initialFarmers = listOf(
            FarmerProfile("farmer_1", "মো: আব্দুল রহিম", "০১৭০৯-১২২৩৩৩", "", "রাজশাহী", "গোদাগাড়ী", "মহিষবাথান", "গ্রাম: মোহনপুর", "বাণিজ্যিক খামারি", VerificationStatus.VERIFIED, totalCompletedOrders = 38, rating = 4.9, reviewsCount = 42),
            FarmerProfile("farmer_2", "করিম উল্লাহ মৃধা", "০১৮১২-৩৪৫৬৭৮", "", "বগুড়া", "শিবগঞ্জ", "মহাস্থান", "গ্রাম: চন্দনপুর", "মাঝারি কৃষক", VerificationStatus.VERIFIED, totalCompletedOrders = 29, rating = 4.8, reviewsCount = 31),
            FarmerProfile("farmer_3", "খলিলুর রহমান", "০১৯৮৭-৬৫৪৩২১", "", "যশোর", "ঝিকরগাছা", "গদখালী", "গ্রাম: পানিসারা", "সবজি ও ফুল চাষী", VerificationStatus.VERIFIED, totalCompletedOrders = 54, rating = 4.9, reviewsCount = 60),
            FarmerProfile("farmer_4", "মো: আবুল কাশেম", "০১৭৫১-৯৮৭৬৫০", "", "দিনাজপুর", "বীরগঞ্জ", "পলাশবাড়ী", "গ্রাম: ভোগনগর", "বাণিজ্যিক ধান চাষী", VerificationStatus.VERIFIED, totalCompletedOrders = 21, rating = 4.7, reviewsCount = 19),
            FarmerProfile("farmer_5", "আব্দুল লতিফ", "০১৬৭৮-১১২২৩৩", "", "ময়মনসিংহ", "ত্রিশাল", "ধানীখোলা", "গ্রাম: সতেরপাড়া", "মাছ ও মুরগি খামারি", VerificationStatus.VERIFIED, totalCompletedOrders = 45, rating = 4.9, reviewsCount = 50),
            FarmerProfile("farmer_6", "সোহেল রানা", "০১৭৩৩-৪৪৩৩৫৫", "", "রংপুর", "মিঠাপুকুর", "কাফ্রিখাল", "গ্রাম: খামার পাড়া", "আলু ও ভুট্টা চাষী", VerificationStatus.VERIFIED, totalCompletedOrders = 17, rating = 4.6, reviewsCount = 15),
            FarmerProfile("farmer_7", "রফিকুল ইসলাম", "০১৮৭৬-৫৪২১০৯", "", "পাবনা", "সাঁথিয়া", "কাশীনাথপুর", "গ্রাম: পাইকরডাঙ্গা", "পেঁয়াজ চাষী", VerificationStatus.VERIFIED, totalCompletedOrders = 62, rating = 4.9, reviewsCount = 74),
            FarmerProfile("farmer_8", "ফজলুল হক", "০১৯১১-২২৩৩৪৪", "", "নাটোর", "সিংড়া", "চৌগ্রাম", "গ্রাম: হাতিয়ান্দহ", "রসুন ও ধানের কৃষক", VerificationStatus.VERIFIED, totalCompletedOrders = 14, rating = 4.8, reviewsCount = 12),
            FarmerProfile("farmer_9", "আনোয়ার হোসেন", "০১৭৯৯-৮৮৭৭৬৬", "", "চুয়াডাঙ্গা", "দামুড়হুদা", "কার্পাসডাঙ্গা", "গ্রাম: দর্শনা রোড", "আম ও পেয়ারা বাগান", VerificationStatus.PENDING, totalCompletedOrders = 4, rating = 4.5, reviewsCount = 3),
            FarmerProfile("farmer_10", "লোকমান হাকিম", "০১৮৫৫-৬৬৭৭৮৮", "", "টাঙ্গাইল", "মধুপুর", "অরণখোলা", "গ্রাম: বেরীবাইদ", "আনারস ও পেঁপে খামারি", VerificationStatus.PENDING, totalCompletedOrders = 6, rating = 4.7, reviewsCount = 5)
        )
        _farmers.value = initialFarmers

        // 10 Buyers
        val initialBuyers = listOf(
            BuyerProfile("buyer_1", "হাজী সালাহউদ্দিন", "০১৯৯১-০০১১০২", "কাওরান বাজার পাইকারি আড়ত", "পাইকারি আড়তদার", "ঢাকা", "কাওরান বাজার", "আড়ত নং ১২, কাওরান বাজার", "TR-DH-892182", VerificationStatus.VERIFIED, 140, 4.9, 85, 99),
            BuyerProfile("buyer_2", "তানভীর আহমেদ", "০১৮১৮-৪৪৩৩২২", "স্বপ্ন সুপারশপ সাপ্লাই", "সুপারশপ সাপ্লাইয়ার", "ঢাকা", "তেজগাঁও", "প্লট ৪৭, তেজগাঁও শিল্প এলাকা", "TR-DH-778210", VerificationStatus.VERIFIED, 310, 5.0, 190, 100),
            BuyerProfile("buyer_3", "শাহ আলম চৌধুরী", "০১৭২২-৩৩৪৪৫৫", "আগোরা ফ্রেশ নেটওয়ার্ক", "সুপারমার্কেট চেইন", "ঢাকা", "গুলশান-১", "রোড ১১, হাউস ৪, গুলশান", "TR-DH-334201", VerificationStatus.VERIFIED, 95, 4.8, 64, 97),
            BuyerProfile("buyer_4", "মাহবুবুর রহমান", "০১৬১২-৯৯৮৮৭৭", "ঢাকা কিচেন ক্যাটারার্স", "ক্যাটারিং ও ফুড সার্ভিস", "ঢাকা", "ধানমন্ডি", "রোড ৮/এ, ধানমন্ডি", "TR-DH-554219", VerificationStatus.VERIFIED, 48, 4.7, 32, 96),
            BuyerProfile("buyer_5", "বরকত উল্লাহ", "০১৭৪০-৬৬৫৫৪৪", "মেসার্স বরকত এন্টারপ্রাইজ", "পাইকারি খাদ্যশস্য বিক্রেতা", "ঢাকা", "বাদামতলী", "বাদামতলী ঘাট, ঢাকা", "TR-DH-901238", VerificationStatus.VERIFIED, 120, 4.8, 77, 98),
            BuyerProfile("buyer_6", "নুরুল ইসলাম", "০১৮৭২-১১২২০০", "শ্যামবাজার ট্রেডার্স", "পেঁয়াজ-আলু পাইকারি", "ঢাকা", "শ্যামবাজার", "শ্যামবাজার সদরঘাট, ঢাকা", "TR-DH-445521", VerificationStatus.VERIFIED, 230, 4.9, 140, 99),
            BuyerProfile("buyer_7", "ফারুক হোসেন", "০১৯১২-৮৮৭৭৬৬", "মিরপুর ফ্রেশ কিচেন সাপ্লাই", "রেস্তোরাঁ সাপ্লাই চেইন", "ঢাকা", "মিরপুর-১০", "সেকশন ১০, মিরপুর", "TR-DH-665512", VerificationStatus.VERIFIED, 35, 4.6, 21, 95),
            BuyerProfile("buyer_8", "কামরুল হাসান", "০১৭৮৮-৯৯০০১১", "উত্তরা পাইকারি ঘর", "খুচরা বাজার পরিবেশক", "ঢাকা", "উত্তরা", "সেক্টর ৭, উত্তরা", "TR-DH-223399", VerificationStatus.VERIFIED, 72, 4.8, 45, 98),
            BuyerProfile("buyer_9", "মুস্তাফিজুর রহমান", "০১৬১১-৩৩৪৪৪৫", "ক্যাপিটাল ফ্রেশ মার্ট", "গ্রোসারি মার্ট", "ঢাকা", "মোহাম্মদপুর", "রিং রোড, মোহাম্মদপুর", "TR-DH-887711", VerificationStatus.PENDING, 12, 4.5, 8, 92),
            BuyerProfile("buyer_10", "জাহিদুল ইসলাম", "০১৮০০-৭৭৬৬৫৫", "বনানী অর্গানিক কর্নার", "অর্গানিক ফুড স্টোর", "ঢাকা", "বনানী", "রোড ১১, ব্লক ডি, বনানী", "TR-DH-112233", VerificationStatus.PENDING, 8, 4.7, 6, 94)
        )
        _buyers.value = initialBuyers

        // 20 Products
        val initialProducts = listOf(
            ProductListing("prod_1", "farmer_1", "মো: আব্দুল রহিম", "রাজশাহী", true, "রাজশাহীর মিষ্টি পাকা টমেটো", ProductCategory.VEGETABLES, 3500.0, 3500.0, ProductUnit.KG, 38.0, 35.0, "গোদাগাড়ী, রাজশাহী", "আগামীকাল", "গতকাল তোলা", QualityGrade.GRADE_A, "বিষমুক্ত লাল পাকা ফ্রেশ টমেটো, সরাসরি ক্ষেত থেকে তোলা। ঢাকার আড়তে সরাসরি সরবরাহের উপযোগী।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_2", "farmer_2", "করিম উল্লাহ মৃধা", "বগুড়া", true, "বগুড়ার ডায়মন্ড গোল আলু", ProductCategory.POTATO, 8000.0, 8000.0, ProductUnit.KG, 28.0, 26.0, "শিবগঞ্জ, বগুড়া", "চলতি সপ্তাহে", "২ দিন আগে", QualityGrade.GRADE_A, "কোল্ডস্টোরেজ মুক্ত নতুন তাজা ডায়মন্ড আলু, দাগহীন ও পরিষ্কার।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_3", "farmer_7", "রফিকুল ইসলাম", "পাবনা", true, "পাবনার দেশি লাল পেঁয়াজ (তাহেরপুরী)", ProductCategory.ONION, 5000.0, 5000.0, ProductUnit.KG, 85.0, 80.0, "সাঁথিয়া, পাবনা", "আজই পাওয়া যাবে", "শুকনো ও বাছাইকৃত", QualityGrade.GRADE_A, "দীর্ঘদিন সংরক্ষণযোগ্য পাকা দেশি পেঁয়াজ।", ProductStatus.ACTIVE, "০৩ সেপ্টেম্বর"),
            ProductListing("prod_4", "farmer_3", "খলিলুর রহমান", "যশোর", true, "যশোরের ফ্রেশ কচি বাঁধাকপি", ProductCategory.VEGETABLES, 2500.0, 2500.0, ProductUnit.PIECE, 22.0, 20.0, "ঝিকরগাছা, যশোর", "২৪ ঘণ্টার মধ্যে", "ভোরবেলা কাটা", QualityGrade.GRADE_A, "প্রতিটি পিস গড়ে ১.৫ কেজি। একদম সতেজ সবুজ পাতাযুক্ত।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_5", "farmer_4", "মো: আবুল কাশেম", "দিনাজপুর", true, "দিনাজপুরের সুগন্ধি কাটারিভোগ ধান", ProductCategory.PADDY, 120.0, 120.0, ProductUnit.MON, 1650.0, 1600.0, "বীরগঞ্জ, দিনাজপুর", "আগামী ৩ দিন", "রৌদ্রে শুকানো", QualityGrade.GRADE_A, "ভেজালমুক্ত খাঁটি সুগন্ধি কাটারিভোগ নতুন ধান।", ProductStatus.ACTIVE, "০২ সেপ্টেম্বর"),
            ProductListing("prod_6", "farmer_5", "আব্দুল লতিফ", "ময়মনসিংহ", true, "ত্রিশালের তাজা রুই মাছ (জীবন্ত)", ProductCategory.FISH, 1500.0, 1500.0, ProductUnit.KG, 280.0, 265.0, "ত্রিশাল, ময়মনসিংহ", "ভোরে ডেলিভারি", "আজ সকালে ধরা", QualityGrade.GRADE_A, "পুকুরের তাজা রুই মাছ। গড়ে ১.৮ থেকে ২.৫ কেজি ওজনের।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_7", "farmer_3", "খলিলুর রহমান", "যশোর", true, "গোল বেগুন (তাল বেগুন)", ProductCategory.VEGETABLES, 1800.0, 1800.0, ProductUnit.KG, 45.0, 42.0, "গদখালী, যশোর", "আজ বিকেলে", "সকালে বাছাইকৃত", QualityGrade.GRADE_A, "পোকা মুক্ত নিখুঁত গোল চকচকে বেগুন।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_8", "farmer_10", "লোকমান হাকিম", "টাঙ্গাইল", true, "মধুপুরের হানিকুইন মিষ্টি আনারস", ProductCategory.FRUITS, 3000.0, 3000.0, ProductUnit.PIECE, 35.0, 32.0, "মধুপুর, টাঙ্গাইল", "আগামীকাল", "গাছপাকা", QualityGrade.GRADE_A, "রাসায়নিক মুক্ত মিষ্টি সুস্বাদু রসালো হানিকুইন আনারস।", ProductStatus.ACTIVE, "০৩ সেপ্টেম্বর"),
            ProductListing("prod_9", "farmer_9", "আনোয়ার হোসেন", "চুয়াডাঙ্গা", false, "চুয়াডাঙ্গার থাই-৭ মিষ্টি পেয়ারা", ProductCategory.FRUITS, 4000.0, 4000.0, ProductUnit.KG, 48.0, 44.0, "দামুড়হুদা, চুয়াডাঙ্গা", "আগামী পরশু", "ব্যাগিং করা ফল", QualityGrade.GRADE_A, "ফোম ও নেট দিয়ে প্যাকেট করা আকর্ষণীয় সাইজের পেয়ারা।", ProductStatus.PENDING, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_10", "farmer_6", "সোহেল রানা", "রংপুর", true, "রংপুরের হাইব্রিড কাঁচামরিচ", ProductCategory.VEGETABLES, 1200.0, 1200.0, ProductUnit.KG, 90.0, 85.0, "মিঠাপুকুর, রংপুর", "আজই রেডি", "গতকাল তোলা", QualityGrade.GRADE_A, "ঝাল ও গাঢ় সবুজ ফ্রেশ কাঁচামরিচ।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_11", "farmer_8", "ফজলুল হক", "নাটোর", true, "নাটোরের দেশি শুকনো রসুন", ProductCategory.OTHER, 2000.0, 2000.0, ProductUnit.KG, 160.0, 150.0, "সিংড়া, নাটোর", "যেকোনো দিন", "ভালোভাবে শুকানো", QualityGrade.GRADE_A, "মাঝারি ও বড় কোয়ার দেশি সুবাসিত রসুন।", ProductStatus.ACTIVE, "০১ সেপ্টেম্বর"),
            ProductListing("prod_12", "farmer_4", "মো: আবুল কাশেম", "দিনাজপুর", true, "মিনিকেট চাল (অটো রাইস মিল কোয়ালিটি)", ProductCategory.RICE, 5000.0, 5000.0, ProductUnit.KG, 68.0, 65.0, "বীরগঞ্জ, দিনাজপুর", "পরশু", "নতুন মিলিং", QualityGrade.GRADE_A, "সাদা, চকচকে ও পলিশড ঝরঝরে মিনিকেট চাল।", ProductStatus.ACTIVE, "০৩ সেপ্টেম্বর"),
            ProductListing("prod_13", "farmer_2", "করিম উল্লাহ মৃধা", "বগুড়া", true, "সাদা ফুলকপি (মাঝারি ও বড় সাইজ)", ProductCategory.VEGETABLES, 3000.0, 3000.0, ProductUnit.PIECE, 25.0, 22.0, "শিবগঞ্জ, বগুড়া", "আগামীকাল ভোরে", "টাটকা ক্ষেত থেকে", QualityGrade.GRADE_A, "ঘন ও দুধ সাদা ধবধবে ফুলকপি।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_14", "farmer_1", "মো: আব্দুল রহিম", "রাজশাহী", true, "হিমসাগর আম (অগ্রিম বুকিং)", ProductCategory.FRUITS, 2000.0, 2000.0, ProductUnit.KG, 95.0, 90.0, "বাঘা, রাজশাহী", "সিজনে সরবরাহ", "গাছপাকা", QualityGrade.GRADE_A, "রাজশাহীর আসল কেমিক্যালমুক্ত সুমিষ্ট হিমসাগর।", ProductStatus.ACTIVE, "০৩ সেপ্টেম্বর"),
            ProductListing("prod_15", "farmer_7", "রফিকুল ইসলাম", "পাবনা", true, "হাইব্রিড শসা (সালাদ স্পেশাল)", ProductCategory.VEGETABLES, 1500.0, 1500.0, ProductUnit.KG, 32.0, 30.0, "সাঁথিয়া, পাবনা", "আজ রাত ৮টা", "সতেজ ও রসালো", QualityGrade.GRADE_A, "সোজা, কচি ও তেতোমুক্ত ফ্রেশ শসা।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_16", "farmer_8", "ফজলুল হক", "নাটোর", true, "দেশি লাল শাক (আঁটি)", ProductCategory.VEGETABLES, 1000.0, 1000.0, ProductUnit.PIECE, 12.0, 10.0, "সিংড়া, নাটোর", "ভোরবেলা", "মাটি থেকে তোলা", QualityGrade.ORGANIC, "সম্পূর্ণ জৈব সারে উৎপাদিত পুষ্টিকর লাল শাক।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_17", "farmer_5", "আব্দুল লতিফ", "ময়মনসিংহ", true, "তেলাপিয়া মাছ (বড় সাইজ)", ProductCategory.FISH, 2200.0, 2200.0, ProductUnit.KG, 175.0, 165.0, "ত্রিশাল, ময়মনসিংহ", "ভোর ৬টা", "অক্সিজেন ভ্যানে", QualityGrade.GRADE_A, "৩০০-৫০০ গ্রাম ওজনের তেলাপিয়া মাছ।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_18", "farmer_6", "সোহেল রানা", "রংপুর", true, "কাটিং গম (শুকনো ও ঝাড়া)", ProductCategory.WHEAT, 4000.0, 4000.0, ProductUnit.KG, 42.0, 40.0, "মিঠাপুকুর, রংপুর", "চলতি সপ্তাহে", "দানা পুষ্ট", QualityGrade.GRADE_B, "ময়দা ও আটা তৈরির উপযোগী উন্নত জাতের গম।", ProductStatus.ACTIVE, "০২ সেপ্টেম্বর"),
            ProductListing("prod_19", "farmer_1", "মো: আব্দুল রহিম", "রাজশাহী", true, "সবুজ কাঁচা পেঁপে (রান্নার জন্য)", ProductCategory.VEGETABLES, 2000.0, 2000.0, ProductUnit.KG, 24.0, 20.0, "গোদাগাড়ী, রাজশাহী", "আগামীকাল", "তাজা ডাল কাটা", QualityGrade.GRADE_A, "কষযুক্ত সতেজ সবুজ কাঁচা পেঁপে।", ProductStatus.PENDING, "০৪ সেপ্টেম্বর"),
            ProductListing("prod_20", "farmer_3", "খলিলুর রহমান", "যশোর", true, "অটো চারা লাল শিম (নতুন সিজন)", ProductCategory.VEGETABLES, 1100.0, 1100.0, ProductUnit.KG, 75.0, 70.0, "গদখালী, যশোর", "আগামী ৩ দিন", "গাছের প্রথম ফলন", QualityGrade.GRADE_A, "দানা ভরা ও মিষ্টি স্বাদের সুস্বাদু শিম।", ProductStatus.ACTIVE, "০৪ সেপ্টেম্বর")
        )
        _products.value = initialProducts

        // 10 Buyer Demands (Most Important Feature!)
        val initialDemands = listOf(
            BuyerDemand("dem_1", "buyer_1", "হাজী সালাহউদ্দিন", "কাওরান বাজার পাইকারি আড়ত", "ঢাকা", true, "টমেটো (Tomato)", ProductCategory.VEGETABLES, 2000.0, 0.0, ProductUnit.KG, "কাওরান বাজার আড়ত, ঢাকা", "২০ সেপ্টেম্বর", 40.0, 45.0, QualityGrade.GRADE_A, "লাল পাকা ফ্রেশ টমেটো প্রয়োজন, ক্র্যাটিং ভালো হতে হবে।", DemandStatus.ACTIVE, offersCount = 3, createdAt = "০৪ সেপ্টেম্বর"),
            BuyerDemand("dem_2", "buyer_2", "তানভীর আহমেদ", "স্বপ্ন সুপারশপ সাপ্লাই", "ঢাকা", true, "ডায়মন্ড গোল আলু (Potato)", ProductCategory.POTATO, 5000.0, 0.0, ProductUnit.KG, "তেজগাঁও সেন্ট্রাল ওয়্যারহাউস, ঢাকা", "২২ সেপ্টেম্বর", 27.0, 30.0, QualityGrade.GRADE_A, "মেশিন ওয়াশযোগ্য সাইজ এ ডায়মন্ড আলু, কোনো সবুজ ছোপ থাকা চলবে না।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০৪ সেপ্টেম্বর"),
            BuyerDemand("dem_3", "buyer_3", "শাহ আলম চৌধুরী", "আগোরা ফ্রেশ নেটওয়ার্ক", "ঢাকা", true, "দেশি লাল পেঁয়াজ (Onion)", ProductCategory.ONION, 3000.0, 0.0, ProductUnit.KG, "গুলশান ডিসট্রিবিউশন হাব, ঢাকা", "১৮ সেপ্টেম্বর", 82.0, 88.0, QualityGrade.GRADE_A, "শুকনো ও ভালো বাছাই করা পাবনা অথবা ফরিদপুরের পেঁয়াজ।", DemandStatus.ACTIVE, offersCount = 3, createdAt = "০৩ সেপ্টেম্বর"),
            BuyerDemand("dem_4", "buyer_2", "তানভীর আহমেদ", "স্বপ্ন সুপারশপ সাপ্লাই", "ঢাকা", true, "বাঁধাকপি (Cabbage)", ProductCategory.VEGETABLES, 1500.0, 0.0, ProductUnit.PIECE, "তেজগাঁও ওয়্যারহাউস, ঢাকা", "১৯ সেপ্টেম্বর", 22.0, 26.0, QualityGrade.GRADE_A, "প্রতিটি পিস ১.২ থেকে ১.৮ কেজি হতে হবে। সতেজ কাঁচাপাতা যুক্ত।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০৪ সেপ্টেম্বর"),
            BuyerDemand("dem_5", "buyer_4", "মাহবুবুর রহমান", "ঢাকা কিচেন ক্যাটারার্স", "ঢাকা", true, "নাজিরশাইল / মিনিকেট চাল", ProductCategory.RICE, 2500.0, 0.0, ProductUnit.KG, "ধানমন্ডি ৮/এ কিচেন, ঢাকা", "২১ সেপ্টেম্বর", 66.0, 72.0, QualityGrade.GRADE_A, "ক্যাটারিং বিরিয়ানি ও ভাতের উপযোগী চিকন ও সুগন্ধি চাল।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০২ সেপ্টেম্বর"),
            BuyerDemand("dem_6", "buyer_5", "বরকত উল্লাহ", "মেসার্স বরকত এন্টারপ্রাইজ", "ঢাকা", true, "বোরো ধান (Paddy)", ProductCategory.PADDY, 150.0, 0.0, ProductUnit.MON, "বাদামতলী ঘাট আড়ত, ঢাকা", "২৫ সেপ্টেম্বর", 1250.0, 1350.0, QualityGrade.GRADE_A, "আর্দ্রতা ১৪% এর নিচে হতে হবে। সম্পূর্ণ পরিষ্কার দানা।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০১ সেপ্টেম্বর"),
            BuyerDemand("dem_7", "buyer_1", "হাজী সালাহউদ্দিন", "কাওরান বাজার পাইকারি আড়ত", "ঢাকা", true, "পুকুরের তাজা রুই মাছ (Fish)", ProductCategory.FISH, 1200.0, 0.0, ProductUnit.KG, "কাওরান বাজার মাছের আড়ত, ঢাকা", "১৭ সেপ্টেম্বর", 270.0, 290.0, QualityGrade.GRADE_A, "জীবন্ত বা বরফ ঠান্ডা তাজা মাছ। গড়ে ২ কেজি ওজন।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০৩ সেপ্টেম্বর"),
            BuyerDemand("dem_8", "buyer_7", "ফারুক হোসেন", "মিরপুর ফ্রেশ কিচেন সাপ্লাই", "ঢাকা", true, "কাঁচামরিচ (Green Chili)", ProductCategory.VEGETABLES, 800.0, 0.0, ProductUnit.KG, "মিরপুর-১০ পাইকারি পয়েন্ট, ঢাকা", "১৮ সেপ্টেম্বর", 85.0, 95.0, QualityGrade.GRADE_A, "গাঢ় সবুজ ও কড়া ঝাল মরিচ দরকার।", DemandStatus.ACTIVE, offersCount = 2, createdAt = "০৪ সেপ্টেম্বর"),
            BuyerDemand("dem_9", "buyer_8", "কামরুল হাসান", "উত্তরা পাইকারি ঘর", "ঢাকা", true, "মিষ্টি হানিকুইন আনারস (Pineapple)", ProductCategory.FRUITS, 2000.0, 0.0, ProductUnit.PIECE, "উত্তরা সেক্টর ৭ বাজার, ঢাকা", "২০ সেপ্টেম্বর", 32.0, 36.0, QualityGrade.GRADE_A, "রসালো ও মিষ্টি টাঙ্গাইল মধুপুরের আনারস।", DemandStatus.ACTIVE, offersCount = 1, createdAt = "০৩ সেপ্টেম্বর"),
            BuyerDemand("dem_10", "buyer_6", "নুরুল ইসলাম", "শ্যামবাজার ট্রেডার্স", "ঢাকা", true, "শুকনো আটা গম (Wheat)", ProductCategory.WHEAT, 4000.0, 0.0, ProductUnit.KG, "শ্যামবাজার ঘাট, ঢাকা", "২৪ সেপ্টেম্বর", 40.0, 44.0, QualityGrade.GRADE_B, "ভালো মানের গম। পোকা বা ভেজাল থাকা চলবে না।", DemandStatus.ACTIVE, offersCount = 1, createdAt = "০২ সেপ্টেম্বর")
        )
        _demands.value = initialDemands

        // 20 Offers from Farmers on Buyer Demands
        val initialOffers = listOf(
            // For Demand 1 (Tomato 2000kg)
            FarmerOffer("off_1", "dem_1", "farmer_1", "মো: আব্দুল রহিম", "০১৭০৯-১২২৩৩৩", "গোদাগাড়ী, রাজশাহী", true, 500.0, ProductUnit.KG, 42.0, QualityGrade.GRADE_A, "১৮ সেপ্টেম্বর", "আমি ৫০০ কেজি দিতে পারব। গ্রেড এ পাকা টমেটো। প্লাস্টিক ক্রেটে ডেলিভারি।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),
            FarmerOffer("off_2", "dem_1", "farmer_2", "করিম উল্লাহ মৃধা", "০১৮১২-৩৪৫৬৭৮", "শিবগঞ্জ, বগুড়া", true, 800.0, ProductUnit.KG, 41.0, QualityGrade.GRADE_A, "১৯ সেপ্টেম্বর", "আমি ৮০০ কেজি দিতে পারব। একদম তাজা বাগান থেকে তোলো।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),
            FarmerOffer("off_3", "dem_1", "farmer_3", "খলিলুর রহমান", "০১৯৮৭-৬৫৪৩২১", "ঝিকরগাছা, যশোর", true, 700.0, ProductUnit.KG, 43.0, QualityGrade.GRADE_A, "২০ সেপ্টেম্বর", "আমি ৭০০ কেজি দিতে পারব। সরাসরি ট্রাকে ঢাকা পৌঁছাবে।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),

            // For Demand 2 (Potato 5000kg)
            FarmerOffer("off_4", "dem_2", "farmer_2", "করিম উল্লাহ মৃধা", "০১৮১২-৩৪৫৬৭৮", "শিবগঞ্জ, বগুড়া", true, 3000.0, ProductUnit.KG, 28.0, QualityGrade.GRADE_A, "২১ সেপ্টেম্বর", "বগুড়ার সেরা ডায়মন্ড আলু ৩০০০ কেজি এক ট্রাকে দেওয়া সম্ভব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),
            FarmerOffer("off_5", "dem_2", "farmer_6", "সোহেল রানা", "০১৭৩৩-৪৪৩৩৫৫", "মিঠাপুকুর, রংপুর", true, 2000.0, ProductUnit.KG, 27.5, QualityGrade.GRADE_A, "২২ সেপ্টেম্বর", "রংপুরের নতুন পরিষ্কার আলু ২০০০ কেজি সরবরাহ করতে পারব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),

            // For Demand 3 (Onion 3000kg)
            FarmerOffer("off_6", "dem_3", "farmer_7", "রফিকুল ইসলাম", "০১৮৭৬-৫৪২১০৯", "সাঁথিয়া, পাবনা", true, 1500.0, ProductUnit.KG, 84.0, QualityGrade.GRADE_A, "১৭ সেপ্টেম্বর", "পাবনার তাহেরপুরী দেশি পেঁয়াজ ১৫০০ কেজি দিতে পারব। শুকনো ও মানসম্পন্ন।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),
            FarmerOffer("off_7", "dem_3", "farmer_8", "ফজলুল হক", "০১৯১১-২২৩৩৪৪", "সিংড়া, নাটোর", true, 1000.0, ProductUnit.KG, 85.0, QualityGrade.GRADE_A, "১৮ সেপ্টেম্বর", "সিংড়ার বাছাইকৃত দেশি পেঁয়াজ ১০০০ কেজি রেডি আছে।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),
            FarmerOffer("off_8", "dem_3", "farmer_1", "মো: আব্দুল রহিম", "০১৭০৯-১২২৩৩৩", "গোদাগাড়ী, রাজশাহী", true, 500.0, ProductUnit.KG, 83.5, QualityGrade.GRADE_A, "১৮ সেপ্টেম্বর", "রাজশাহীর ভালো মানের দেশি পেঁয়াজ ৫০০ কেজি দিতে পারব।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),

            // For Demand 4 (Cabbage 1500 pcs)
            FarmerOffer("off_9", "dem_4", "farmer_3", "খলিলুর রহমান", "০১৯৮৭-৬৫৪৩২১", "ঝিকরগাছা, যশোর", true, 1000.0, ProductUnit.PIECE, 23.0, QualityGrade.GRADE_A, "১৯ সেপ্টেম্বর", "বড় সাইজের বাঁধাকপি ১০০০ পিস পাঠাতে পারব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),
            FarmerOffer("off_10", "dem_4", "farmer_2", "করিম উল্লাহ মৃধা", "০১৮১২-৩৪৫৬৭৮", "শিবগঞ্জ, বগুড়া", true, 500.0, ProductUnit.PIECE, 24.0, QualityGrade.GRADE_A, "১৯ সেপ্টেম্বর", "তাজা বাঁধাকপি ৫০০ পিস দিতে পারব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),

            // For Demand 5 (Rice 2500 kg)
            FarmerOffer("off_11", "dem_5", "farmer_4", "মো: আবুল কাশেম", "০১৭৫১-৯৮৭৬৫০", "বীরগঞ্জ, দিনাজপুর", true, 2500.0, ProductUnit.KG, 68.0, QualityGrade.GRADE_A, "২০ সেপ্টেম্বর", "দিনাজপুরের প্রথম গ্রেডের মিনিকেট চাল পুরো ২৫০০ কেজি এক লটে পাঠাব।", OfferStatus.PENDING, "০২ সেপ্টেম্বর"),
            FarmerOffer("off_12", "dem_5", "farmer_1", "মো: আব্দুল রহিম", "০১৭০৯-১২২৩৩৩", "গোদাগাড়ী, রাজশাহী", true, 1500.0, ProductUnit.KG, 69.0, QualityGrade.GRADE_A, "২১ সেপ্টেম্বর", "১৫০০ কেজি নাজিরশাইল চাল দিতে পারব।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),

            // For Demand 6 (Paddy 150 Mon)
            FarmerOffer("off_13", "dem_6", "farmer_4", "মো: আবুল কাশেম", "০১৭৫১-৯৮৭৬৫০", "বীরগঞ্জ, দিনাজপুর", true, 100.0, ProductUnit.MON, 1300.0, QualityGrade.GRADE_A, "২৪ সেপ্টেম্বর", "১০০ মণ শুকনো পুষ্ট বোরো ধান দেওয়া যাবে।", OfferStatus.PENDING, "০২ সেপ্টেম্বর"),
            FarmerOffer("off_14", "dem_6", "farmer_8", "ফজলুল হক", "০১৯১১-২২৩৩৪৪", "সিংড়া, নাটোর", true, 50.0, ProductUnit.MON, 1280.0, QualityGrade.GRADE_A, "২৫ সেপ্টেম্বর", "চলনবিলের নতুন ৫০ মণ ধান দেব।", OfferStatus.PENDING, "০২ সেপ্টেম্বর"),

            // For Demand 7 (Fish 1200 kg)
            FarmerOffer("off_15", "dem_7", "farmer_5", "আব্দুল লতিফ", "০১৬৭৮-১১২২৩৩", "ত্রিশাল, ময়মনসিংহ", true, 1200.0, ProductUnit.KG, 275.0, QualityGrade.GRADE_A, "১৬ সেপ্টেম্বর", "অক্সিজেন ড্রামে করে একদম জীবন্ত রুই মাছ পৌঁছানো হবে।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),
            FarmerOffer("off_16", "dem_7", "farmer_3", "খলিলুর রহমান", "০১৯৮৭-৬৫৪৩২১", "ঝিকরগাছা, যশোর", true, 600.0, ProductUnit.KG, 280.0, QualityGrade.GRADE_A, "১৭ সেপ্টেম্বর", "পুকুরের তাজা রুই মাছ ৬০০ কেজি পাঠাতে পারি।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),

            // For Demand 8 (Green Chili 800 kg)
            FarmerOffer("off_17", "dem_8", "farmer_6", "সোহেল রানা", "০১৭৩৩-৪৪৩৩৫৫", "মিঠাপুকুর, রংপুর", true, 800.0, ProductUnit.KG, 88.0, QualityGrade.GRADE_A, "১৭ সেপ্টেম্বর", "রংপুরের গাঢ় সবুজ তাজা কাঁচামরিচ পুরো ৮০০ কেজি সরবরাহ করব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),
            FarmerOffer("off_18", "dem_8", "farmer_7", "রফিকুল ইসলাম", "০১৮৭৬-৫৪২১০৯", "সাঁথিয়া, পাবনা", true, 400.0, ProductUnit.KG, 90.0, QualityGrade.GRADE_A, "১৮ সেপ্টেম্বর", "পাবনার হাইব্রিড মরিচ ৪০০ কেজি দিতে পারব।", OfferStatus.PENDING, "০৪ সেপ্টেম্বর"),

            // For Demand 9 (Pineapple 2000 pcs)
            FarmerOffer("off_19", "dem_9", "farmer_10", "লোকমান হাকিম", "০১৮৫৫-৬৬৭৭৮৮", "মধুপুর, টাঙ্গাইল", true, 2000.0, ProductUnit.PIECE, 34.0, QualityGrade.GRADE_A, "১৯ সেপ্টেম্বর", "মধুপুরের হানিকুইন মিষ্টি আনারস ২০০০ পিস পিকআপে পৌঁছাব।", OfferStatus.PENDING, "০৩ সেপ্টেম্বর"),

            // For Demand 10 (Wheat 4000 kg)
            FarmerOffer("off_20", "dem_10", "farmer_6", "সোহেল রানা", "০১৭৩৩-৪৪৩৩৫৫", "মিঠাপুকুর, রংপুর", true, 4000.0, ProductUnit.KG, 41.5, QualityGrade.GRADE_B, "২৩ সেপ্টেম্বর", "৪০০০ কেজি শুকনো পরিষ্কার গম সরবরাহ করতে প্রস্তুত।", OfferStatus.PENDING, "০২ সেপ্টেম্বর")
        )
        _offers.value = initialOffers

        // 10 Orders in different lifecycle states (Completed, In Transit, Payment Confirmed, Preparing, Payment Pending)
        val initialOrders = listOf(
            // Order 1: Completed with rating
            MarketplaceOrder(
                id = "ord_1001",
                orderNumber = "KB-1001",
                demandId = "dem_1",
                buyerId = "buyer_1",
                buyerName = "হাজী সালাহউদ্দিন",
                buyerBusinessName = "কাওরান বাজার পাইকারি আড়ত",
                buyerPhone = "০১৯৯১-০০১১০২",
                farmerId = "farmer_1",
                farmerName = "মো: আব্দুল রহিম",
                farmerPhone = "০১৭০৯-১২২৩৩৩",
                farmerLocation = "গোদাগাড়ী, রাজশাহী",
                productTitle = "মিষ্টি পাকা টমেটো",
                category = ProductCategory.VEGETABLES,
                quantity = 1000.0,
                unit = ProductUnit.KG,
                pricePerUnit = 40.0,
                totalAmount = 40000.0,
                depositRequired = 8000.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.COMPLETED,
                deliveryLocation = "কাওরান বাজার আড়ত, ঢাকা",
                expectedDeliveryDate = "০১ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("গোদাগাড়ী, রাজশাহী", "রাজশাহী সেন্ট্রাল এগ্রি হাব", "কাওরান বাজার, ঢাকা", TransportStatus.DELIVERED, "মোঃ কাশেম ড্রাইভার", "০১৭১১-২২৩৩৪৪", "ঢাকা মেট্রো-ট ১১-৪৫২৩", "পৌঁছেছে"),
                verification = QualityVerification(1000.0, 995.0, ProductUnit.KG, QualityGrade.GRADE_A, "সেলিম রেজা (ইন্সপেক্টর)", "০১ সেপ্টেম্বর সকাল ৯টা", "পণ্য ফ্রেশ ও পাকা ছিল", true),
                isRated = true,
                createdAt = "২৯ আগস্ট"
            ),
            // Order 2: In Transit 🚚
            MarketplaceOrder(
                id = "ord_1002",
                orderNumber = "KB-1002",
                demandId = "dem_2",
                buyerId = "buyer_2",
                buyerName = "তানভীর আহমেদ",
                buyerBusinessName = "স্বপ্ন সুপারশপ সাপ্লাই",
                buyerPhone = "০১৮১৮-৪৪৩৩২২",
                farmerId = "farmer_2",
                farmerName = "করিম উল্লাহ মৃধা",
                farmerPhone = "০১৮১২-৩৪৫৬৭৮",
                farmerLocation = "শিবগঞ্জ, বগুড়া",
                productTitle = "ডায়মন্ড গোল আলু",
                category = ProductCategory.POTATO,
                quantity = 2500.0,
                unit = ProductUnit.KG,
                pricePerUnit = 28.0,
                totalAmount = 70000.0,
                depositRequired = 14000.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.IN_TRANSIT,
                deliveryLocation = "তেজগাঁও ওয়্যারহাউস, ঢাকা",
                expectedDeliveryDate = "আজ সন্ধ্যা ৭টা",
                deliveryInfo = DeliveryInfo("শিবগঞ্জ, বগুড়া", "বগুড়া এগ্রো হাব", "তেজগাঁও ওয়্যারহাউস, ঢাকা", TransportStatus.IN_TRANSIT, "মোঃ জাহাঙ্গীর আলম", "০১৭২২-৯৯৮৮৭৭", "ঢাকা মেট্রো-ট ১৫-৮৯০২", "আজ সন্ধ্যা ৭টা"),
                verification = QualityVerification(2500.0, 2490.0, ProductUnit.KG, QualityGrade.GRADE_A, "মুরাদ হাসান (সিংড়া হাব)", "আজ দুপুর ২:১৫", "আলু পরিষ্কার ও শুকনো", true),
                createdAt = "০৩ সেপ্টেম্বর"
            ),
            // Order 3: Collected / Weight Verified
            MarketplaceOrder(
                id = "ord_1003",
                orderNumber = "KB-1003",
                demandId = "dem_3",
                buyerId = "buyer_3",
                buyerName = "শাহ আলম চৌধুরী",
                buyerBusinessName = "আগোরা ফ্রেশ নেটওয়ার্ক",
                buyerPhone = "০১৭২২-৩৩৪৪৫৫",
                farmerId = "farmer_7",
                farmerName = "রফিকুল ইসলাম",
                farmerPhone = "০১৮৭৬-৫৪২১০৯",
                farmerLocation = "সাঁথিয়া, পাবনা",
                productTitle = "পাবনার দেশি লাল পেঁয়াজ",
                category = ProductCategory.ONION,
                quantity = 1500.0,
                unit = ProductUnit.KG,
                pricePerUnit = 85.0,
                totalAmount = 127500.0,
                depositRequired = 25500.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.COLLECTED,
                deliveryLocation = "গুলশান ডিসট্রিবিউশন হাব, ঢাকা",
                expectedDeliveryDate = "আগামীকাল ভোর ৬টা",
                deliveryInfo = DeliveryInfo("সাঁথিয়া, পাবনা", "পাবনা জেলা এগ্রি হাব", "গুলশান, ঢাকা", TransportStatus.ASSIGNED, "আজিজুল হক", "০১৯১১-৬৬৭৭৮৮", "ঢাকা মেট্রো-ট ১৪-১২০৯", "কাল ভোর ৬টা"),
                verification = QualityVerification(1500.0, 1492.0, ProductUnit.KG, QualityGrade.GRADE_A, "সোহরাব হোসেন (পাবনা হাব)", "আজ দুপুর ৩টা", "ভালোভাবে শুকানো তাহেরপুরী পেঁয়াজ", true),
                createdAt = "০৩ সেপ্টেম্বর"
            ),
            // Order 4: Payment Confirmed / Preparing
            MarketplaceOrder(
                id = "ord_1004",
                orderNumber = "KB-1004",
                demandId = "dem_4",
                buyerId = "buyer_2",
                buyerName = "তানভীর আহমেদ",
                buyerBusinessName = "স্বপ্ন সুপারশপ সাপ্লাই",
                buyerPhone = "০১৮১৮-৪৪৩৩২২",
                farmerId = "farmer_3",
                farmerName = "খলিলুর রহমান",
                farmerPhone = "০১৯৮৭-৬৫৪৩২১",
                farmerLocation = "ঝিকরগাছা, যশোর",
                productTitle = "তাজা বাঁধাকপি",
                category = ProductCategory.VEGETABLES,
                quantity = 1000.0,
                unit = ProductUnit.PIECE,
                pricePerUnit = 23.0,
                totalAmount = 23000.0,
                depositRequired = 4600.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.PREPARING,
                deliveryLocation = "তেজগাঁও ওয়্যারহাউস, ঢাকা",
                expectedDeliveryDate = "১৯ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("ঝিকরগাছা, যশোর", "যশোর কালেকশন সেন্টার", "তেজগাঁও, ঢাকা", TransportStatus.WAITING),
                verification = QualityVerification(1000.0, 1000.0, ProductUnit.PIECE, QualityGrade.GRADE_A, isVerified = false),
                createdAt = "০৪ সেপ্টেম্বর"
            ),
            // Order 5: Payment Pending (Awaiting Deposit Pay)
            MarketplaceOrder(
                id = "ord_1005",
                orderNumber = "KB-1005",
                demandId = "dem_5",
                buyerId = "buyer_4",
                buyerName = "মাহবুবুর রহমান",
                buyerBusinessName = "ঢাকা কিচেন ক্যাটারার্স",
                buyerPhone = "০১৬১২-৯৯৮৮৭৭",
                farmerId = "farmer_4",
                farmerName = "মো: আবুল কাশেম",
                farmerPhone = "০১৭৫১-৯৮৭৬৫০",
                farmerLocation = "বীরগঞ্জ, দিনাজপুর",
                productTitle = "মিনিকেট চাল",
                category = ProductCategory.RICE,
                quantity = 1000.0,
                unit = ProductUnit.KG,
                pricePerUnit = 68.0,
                totalAmount = 68000.0,
                depositRequired = 13600.0,
                isDepositPaid = false,
                orderStatus = OrderStatus.PAYMENT_PENDING,
                deliveryLocation = "ধানমন্ডি ৮/এ কিচেন, ঢাকা",
                expectedDeliveryDate = "২১ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("বীরগঞ্জ, দিনাজপুর", "দিনাজপুর সেন্ট্রাল হাব", "ধানমন্ডি, ঢাকা", TransportStatus.WAITING),
                verification = QualityVerification(1000.0, 1000.0, ProductUnit.KG, QualityGrade.GRADE_A, isVerified = false),
                createdAt = "০৪ সেপ্টেম্বর"
            ),
            // Order 6: Disputed Order ⚠️
            MarketplaceOrder(
                id = "ord_1006",
                orderNumber = "KB-1006",
                demandId = "dem_8",
                buyerId = "buyer_7",
                buyerName = "ফারুক হোসেন",
                buyerBusinessName = "মিরপুর ফ্রেশ কিচেন সাপ্লাই",
                buyerPhone = "০১৯১২-৮৮৭৭৬৬",
                farmerId = "farmer_6",
                farmerName = "সোহেল রানা",
                farmerPhone = "০১৭৩৩-৪৪৩৩৫৫",
                farmerLocation = "মিঠাপুকুর, রংপুর",
                productTitle = "কাঁচামরিচ",
                category = ProductCategory.VEGETABLES,
                quantity = 400.0,
                unit = ProductUnit.KG,
                pricePerUnit = 90.0,
                totalAmount = 36000.0,
                depositRequired = 7200.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.DISPUTED,
                deliveryLocation = "মিরপুর-১০, ঢাকা",
                expectedDeliveryDate = "০২ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("মিঠাপুকুর, রংপুর", "রংপুর হাব", "মিরপুর-১০, ঢাকা", TransportStatus.DELIVERED, "বাবুল মিয়া", "০১৮০০-১২৩৪৫৬", "ঢাকা মেট্রো-ন ১২-৮৮৭৬", "বিতর্কিত"),
                verification = QualityVerification(400.0, 360.0, ProductUnit.KG, QualityGrade.GRADE_B, "ইন্সপেক্টর শফিকুল", "০২ সেপ্টেম্বর", "৪০ কেজি ঘাটতি পাওয়া গেছে", true),
                hasDispute = true,
                createdAt = "৩১ আগস্ট"
            ),
            // Order 7: Completed Order
            MarketplaceOrder(
                id = "ord_1007",
                orderNumber = "KB-1007",
                demandId = "dem_7",
                buyerId = "buyer_1",
                buyerName = "হাজী সালাহউদ্দিন",
                buyerBusinessName = "কাওরান বাজার পাইকারি আড়ত",
                buyerPhone = "০১৯৯১-০০১১০২",
                farmerId = "farmer_5",
                farmerName = "আব্দুল লতিফ",
                farmerPhone = "০১৬৭৮-১১২২৩৩",
                farmerLocation = "ত্রিশাল, ময়মনসিংহ",
                productTitle = "পুকুরের তাজা রুই মাছ",
                category = ProductCategory.FISH,
                quantity = 800.0,
                unit = ProductUnit.KG,
                pricePerUnit = 275.0,
                totalAmount = 220000.0,
                depositRequired = 44000.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.COMPLETED,
                deliveryLocation = "কাওরান বাজার মাছের আড়ত, ঢাকা",
                expectedDeliveryDate = "৩০ আগস্ট",
                deliveryInfo = DeliveryInfo("ত্রিশাল, ময়মনসিংহ", "ত্রিশাল মৎস্য হাব", "কাওরান বাজার, ঢাকা", TransportStatus.DELIVERED, "নাসির উদ্দিন", "০১৭৫৫-৪৪৩৩২২", "ঢাকা মেট্রো-ম ১৯-৭৭৬৫", "সম্পন্ন"),
                verification = QualityVerification(800.0, 804.0, ProductUnit.KG, QualityGrade.GRADE_A, "কবির হোসেন", "৩০ আগস্ট ভোর", "জীবন্ত ও সতেজ মাছ", true),
                isRated = true,
                createdAt = "২৮ আগস্ট"
            ),
            // Order 8: Delivered
            MarketplaceOrder(
                id = "ord_1008",
                orderNumber = "KB-1008",
                demandId = "dem_9",
                buyerId = "buyer_8",
                buyerName = "কামরুল হাসান",
                buyerBusinessName = "উত্তরা পাইকারি ঘর",
                buyerPhone = "০১৭৮৮-৯৯০০১১",
                farmerId = "farmer_10",
                farmerName = "লোকমান হাকিম",
                farmerPhone = "০১৮৫৫-৬৬৭৭৮৮",
                farmerLocation = "মধুপুর, টাঙ্গাইল",
                productTitle = "হানিকুইন আনারস",
                category = ProductCategory.FRUITS,
                quantity = 1500.0,
                unit = ProductUnit.PIECE,
                pricePerUnit = 34.0,
                totalAmount = 51000.0,
                depositRequired = 10200.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.DELIVERED,
                deliveryLocation = "উত্তরা সেক্টর ৭ বাজার, ঢাকা",
                expectedDeliveryDate = "আজ দুপুর",
                deliveryInfo = DeliveryInfo("মধুপুর, টাঙ্গাইল", "মধুপুর এগ্রি হাব", "উত্তরা সেক্টর ৭, ঢাকা", TransportStatus.DELIVERED, "আসাদুল ইসলাম", "০১৯৯৯-৮৮৭৭৬৬", "ঢাকা মেট্রো-ট ১২-৩৪১২", "পৌঁছেছে"),
                verification = QualityVerification(1500.0, 1500.0, ProductUnit.PIECE, QualityGrade.GRADE_A, "জাকির হোসেন", "আজ দুপুর ১২টা", "ভালো প্যাকেজিং", true),
                createdAt = "০৩ সেপ্টেম্বর"
            ),
            // Order 9: Confirmed
            MarketplaceOrder(
                id = "ord_1009",
                orderNumber = "KB-1009",
                demandId = "dem_6",
                buyerId = "buyer_5",
                buyerName = "বরকত উল্লাহ",
                buyerBusinessName = "মেসার্স বরকত এন্টারপ্রাইজ",
                buyerPhone = "০১৭৪০-৬৬৫৫৪৪",
                farmerId = "farmer_4",
                farmerName = "মো: আবুল কাশেম",
                farmerPhone = "০১৭৫১-৯৮৭৬৫০",
                farmerLocation = "বীরগঞ্জ, দিনাজপুর",
                productTitle = "বোরো ধান",
                category = ProductCategory.PADDY,
                quantity = 80.0,
                unit = ProductUnit.MON,
                pricePerUnit = 1300.0,
                totalAmount = 104000.0,
                depositRequired = 20800.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.CONFIRMED,
                deliveryLocation = "বাদামতলী ঘাট আড়ত, ঢাকা",
                expectedDeliveryDate = "২২ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("বীরগঞ্জ, দিনাজপুর", "দিনাজপুর খাদ্যশস্য হাব", "বাদামতলী ঘাট, ঢাকা", TransportStatus.WAITING),
                verification = QualityVerification(80.0, 80.0, ProductUnit.MON, QualityGrade.GRADE_A, isVerified = false),
                createdAt = "০৪ সেপ্টেম্বর"
            ),
            // Order 10: Payment Confirmed (Preparing)
            MarketplaceOrder(
                id = "ord_1010",
                orderNumber = "KB-1010",
                demandId = "dem_1",
                buyerId = "buyer_1",
                buyerName = "হাজী সালাহউদ্দিন",
                buyerBusinessName = "কাওরান বাজার পাইকারি আড়ত",
                buyerPhone = "০১৯৯১-০০১১০২",
                farmerId = "farmer_2",
                farmerName = "করিম উল্লাহ মৃধা",
                farmerPhone = "০১৮১২-৩৪৫৬৭৮",
                farmerLocation = "শিবগঞ্জ, বগুড়া",
                productTitle = "মিষ্টি পাকা টমেটো",
                category = ProductCategory.VEGETABLES,
                quantity = 800.0,
                unit = ProductUnit.KG,
                pricePerUnit = 41.0,
                totalAmount = 32800.0,
                depositRequired = 6560.0,
                isDepositPaid = true,
                orderStatus = OrderStatus.PAYMENT_CONFIRMED,
                deliveryLocation = "কাওরান বাজার আড়ত, ঢাকা",
                expectedDeliveryDate = "১৯ সেপ্টেম্বর",
                deliveryInfo = DeliveryInfo("শিবগঞ্জ, বগুড়া", "বগুড়া এগ্রো হাব", "কাওরান বাজার, ঢাকা", TransportStatus.WAITING),
                verification = QualityVerification(800.0, 800.0, ProductUnit.KG, QualityGrade.GRADE_A, isVerified = false),
                createdAt = "০৪ সেপ্টেম্বর"
            )
        )
        _orders.value = initialOrders

        // Initial Disputes
        _disputes.value = listOf(
            Dispute(
                id = "disp_1",
                orderId = "ord_1006",
                orderNumber = "KB-1006",
                reportedByRole = UserRole.BUYER,
                reporterName = "ফারুক হোসেন (মিরপুর ফ্রেশ কিচেন)",
                reporterPhone = "০১৯১২-৮৮৭৭৬৬",
                problemType = ProblemType.QUANTITY_MISMATCH,
                description = "৪০০ কেজি কাঁচামরিচ দেওয়ার কথা ছিল, কিন্তু সংগ্রহ কেন্দ্রে ডিজিটাল স্কেলে ৩৬০ কেজি পাওয়া গেছে। ৪০ কেজি ঘাটতি রয়েছে।",
                status = DisputeStatus.UNDER_REVIEW,
                adminNotes = "কালেকশন এজেন্টকে পুনঃযাচাই করতে বলা হয়েছে। কৃষকের সাথে যোগাযোগ চলছে।",
                createdAt = "০২ সেপ্টেম্বর"
            )
        )

        // Initial Reviews
        _reviews.value = listOf(
            UserReview(
                id = "rev_1",
                orderId = "ord_1001",
                fromUserName = "হাজী সালাহউদ্দিন (কাওরান বাজার)",
                fromUserRole = UserRole.BUYER,
                toUserId = "farmer_1",
                stars = 5,
                productQualityRating = 5,
                quantityAccuracyRating = 5,
                communicationRating = 5,
                reliabilityRating = 5,
                comment = "আব্দুল রহিম ভাইয়ের টমেটো অসম্ভব চমৎকার ছিল। একদম টাটকা ও সময়মতো কাওরান বাজারে পৌঁছেছে।",
                createdAt = "০১ সেপ্টেম্বর"
            ),
            UserReview(
                id = "rev_2",
                orderId = "ord_1007",
                fromUserName = "আব্দুল লতিফ (খামারি)",
                fromUserRole = UserRole.FARMER,
                toUserId = "buyer_1",
                stars = 5,
                productQualityRating = 5,
                quantityAccuracyRating = 5,
                communicationRating = 5,
                reliabilityRating = 5,
                comment = "হাজী সাহেব সঠিক সময়ে পেমেন্ট ক্লিয়ার করেছেন। লেনদেন করে খুব ভালো লাগলো।",
                createdAt = "৩১ আগস্ট"
            )
        )

        // Initial Notifications
        _notifications.value = listOf(
            NotificationItem("notif_1", null, "স্বাগতম কৃষিবাজারে! 🌾", "বাংলাদেশের প্রথম সরাসরি কৃষক-ক্রেতা ডিজিটাল প্ল্যাটফর্ম।", "আজ", false),
            NotificationItem("notif_2", UserRole.BUYER, "অর্ডার #KB-1002 ট্রাকে রওনা হয়েছে 🚚", "বগুড়া থেকে তেজগাঁওয়ের উদ্দেশ্যে ডায়মন্ড আলুর ট্রাক চলছে।", "২ ঘণ্টা আগে", false, relatedOrderId = "ord_1002"),
            NotificationItem("notif_3", UserRole.FARMER, "আপনার অফার গৃহীত হয়েছে! 🎉", "কাওরান বাজার আড়ত আপনার ৮০০ কেজি টমেটোর অফার গ্রহণ করেছে।", "আজ সকাল", false, relatedOrderId = "ord_1010"),
            NotificationItem("notif_4", UserRole.ADMIN, "নতুন সমস্যা রিপোর্ট হয়েছে ⚠️", "অর্ডার #KB-1006 এ ওজনে গরমিল রিপোর্ট এসেছে। তদন্ত করুন।", "গতকাল", false)
        )

        // Initial Chat Messages for Order KB-1002
        _chatMessages.value = listOf(
            ChatMessage("msg_1", "ord_1002", "করিম উল্লাহ মৃধা", UserRole.FARMER, "আসসালামু আলাইকুম তানভীর ভাই। আলু ট্রাকে লোড হয়ে রওনা দিয়েছে।", "দুপুর ২:৩০"),
            ChatMessage("msg_2", "ord_1002", "তানভীর আহমেদ", UserRole.BUYER, "ওয়ালাইকুম আসসালাম করিম ভাই। ড্রাইভারের নম্বর পেয়েছি, সন্ধ্যা ৭টার মধ্যে ওয়্যারহাউসে রিসিভ করব ইনশাআল্লাহ।", "দুপুর ২:৪৫")
        )
    }
}
