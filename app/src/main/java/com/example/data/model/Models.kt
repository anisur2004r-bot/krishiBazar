package com.example.data.model

enum class UserRole(val labelBn: String) {
    FARMER("কৃষক / বিক্রেতা"),
    BUYER("ক্রেতা / ব্যাপারী"),
    ADMIN("সুপার অ্যাডমিন")
}

enum class VerificationStatus(val labelBn: String) {
    PENDING("অপেক্ষমান (Pending)"),
    VERIFIED("যাচাইকৃত (Verified ✅)"),
    REJECTED("প্রত্যাখ্যাত (Rejected)"),
    SUSPENDED("স্থগিত (Suspended)")
}

enum class ProductCategory(val labelBn: String, val icon: String) {
    VEGETABLES("সবজি", "🥬"),
    FRUITS("ফল", "🥭"),
    PADDY("ধান", "🌾"),
    RICE("চাল", "🍚"),
    WHEAT("গম", "🌾"),
    POTATO("আলু", "🥔"),
    ONION("পেঁয়াজ", "🧅"),
    FISH("মাছ", "🐟"),
    OTHER("অন্যান্য", "📦")
}

enum class QualityGrade(val labelBn: String) {
    GRADE_A("গ্রেড A (প্রিমিয়াম)"),
    GRADE_B("গ্রেড B (স্ট্যান্ডার্ড)"),
    ORGANIC("প্রাকৃতিক / অর্গানিক")
}

enum class ProductUnit(val labelBn: String) {
    KG("কেজি (kg)"),
    MON("মণ (৪০ কেজি)"),
    TON("টন (১০০০ কেজি)"),
    PIECE("পিস / সংখ্যা")
}

enum class ProductStatus(val labelBn: String) {
    PENDING("পর্যালোচনায় (Pending)"),
    ACTIVE("সক্রিয় (Active)"),
    PARTIALLY_SOLD("আংশিক বিক্রিত"),
    SOLD("বিক্রিত (Sold)"),
    EXPIRED("মেয়াদোত্তীর্ণ"),
    REJECTED("বাতিল (Rejected)")
}

enum class DemandStatus(val labelBn: String) {
    ACTIVE("সক্রিয় চাহিদা"),
    OFFER_RECEIVED("অফার প্রাপ্ত"),
    ORDERED("অর্ডারকৃত"),
    FULFILLED("সম্পন্ন"),
    CLOSED("বন্ধ")
}

enum class OfferStatus(val labelBn: String) {
    PENDING("বিবেচনায় (Pending)"),
    ACCEPTED("গৃহীত (Accepted ✅)"),
    REJECTED("প্রত্যাখ্যাত (Rejected)")
}

enum class OrderStatus(val labelBn: String) {
    PENDING("অপেক্ষমান (Pending)"),
    CONFIRMED("নিশ্চিতকৃত (Confirmed)"),
    PAYMENT_PENDING("ডিপোজিট বাকি (Payment Pending)"),
    PAYMENT_CONFIRMED("পেমেন্ট সফল (Payment Confirmed ✅)"),
    PREPARING("ফসল তোলা হচ্ছে (Preparing)"),
    COLLECTED("সংগ্রহ কেন্দ্রে জমা (Collected)"),
    IN_TRANSIT("পরিবহনে পথে (In Transit 🚚)"),
    DELIVERED("পৌঁছেছে (Delivered)"),
    COMPLETED("লেনদেন সম্পন্ন (Completed 🎉)"),
    CANCELLED("বাতিলকৃত (Cancelled)"),
    DISPUTED("সমস্যা রিপোর্টকৃত (Disputed ⚠️)")
}

enum class TransportStatus(val labelBn: String) {
    WAITING("গাড়ি অপেক্ষমান"),
    ASSIGNED("চালক নিযুক্ত"),
    PICKED_UP("লোড সম্পন্ন"),
    IN_TRANSIT("পথিমধ্যে রয়েছে"),
    DELIVERED("গন্তব্যে পৌঁছেছে")
}

enum class ProblemType(val labelBn: String) {
    QUANTITY_MISMATCH("ওজনে গরমিল (Quantity Mismatch)"),
    WEIGHT_SHORTAGE("ওজনে কম পাওয়া গেছে (Weight Shortage)"),
    QUALITY_PROBLEM("পণ্যের মান খারাপ / পচা (Quality Problem)"),
    DELIVERY_PROBLEM("দেরিতে সরবরাহ / ট্রাক সমস্যা (Delivery Delay)"),
    PAYMENT_PROBLEM("পেমেন্ট সংক্রান্ত সমস্যা (Payment Issue)"),
    WRONG_PRODUCT("ভুল পণ্য পাঠানো হয়েছে"),
    BUYER_REFUSED("ক্রেতা পণ্য গ্রহণ করতে অস্বীকৃতি জানিয়েছে"),
    FARMER_NOT_PROVIDED("কৃষক পণ্য সরবরাহ করেনি"),
    OTHER("অন্যান্য সমস্যা")
}

enum class DisputeStatus(val labelBn: String) {
    OPEN("উন্মুক্ত (Open)"),
    UNDER_REVIEW("তদন্তাধীন (Under Review)"),
    RESOLVED("মীমাংসিত (Resolved ✅)"),
    RESOLVED_REFUND("রিফান্ড ও মীমাংসিত (Refunded)"),
    REJECTED("বাতিল (Rejected)")
}

// User & Profile
data class FarmerProfile(
    val id: String,
    val name: String,
    val phone: String,
    val photoUrl: String = "",
    val district: String,
    val upazila: String,
    val union: String,
    val address: String,
    val farmerType: String, // ক্ষুদ্র কৃষক / বাণিজ্যিক খামারি
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val nidOrDoc: String = "NID-7829102938",
    val totalCompletedOrders: Int = 12,
    val rating: Double = 4.8,
    val reviewsCount: Int = 18
) {
    val nidNumber: String get() = nidOrDoc
}

data class BuyerProfile(
    val id: String,
    val name: String,
    val phone: String,
    val businessName: String,
    val businessType: String, // পাইকারি আড়তদার / সুপারশপ / রেস্তোরাঁ / ক্যাটারিং
    val district: String,
    val area: String,
    val address: String,
    val tradeInfo: String = "TR-DH-892182",
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val completedOrders: Int = 24,
    val rating: Double = 4.9,
    val reviewsCount: Int = 30,
    val paymentReliability: Int = 98 // 98% on-time payment
)

// Product Listing by Farmer
data class ProductListing(
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val farmerDistrict: String,
    val farmerVerified: Boolean = true,
    val title: String,
    val category: ProductCategory,
    val quantity: Double,
    val remainingQuantity: Double,
    val unit: ProductUnit,
    val expectedPrice: Double, // BDT per unit
    val minPrice: Double,      // BDT per unit
    val location: String,
    val availableDate: String,
    val harvestDate: String,
    val qualityGrade: QualityGrade,
    val description: String,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val createdAt: String
)

// Buyer Demand Post
data class BuyerDemand(
    val id: String,
    val buyerId: String,
    val buyerName: String,
    val buyerBusinessName: String,
    val buyerDistrict: String,
    val buyerVerified: Boolean = true,
    val productTitle: String,
    val category: ProductCategory,
    val requiredQuantity: Double,
    val fulfilledQuantity: Double = 0.0,
    val unit: ProductUnit,
    val requiredLocation: String, // e.g. ঢাকা কাওরান বাজার
    val requiredDate: String,
    val minExpectedPrice: Double,
    val maxExpectedPrice: Double,
    val qualityGrade: QualityGrade,
    val additionalNote: String,
    val status: DemandStatus = DemandStatus.ACTIVE,
    val offersCount: Int = 0,
    val createdAt: String
)

// Farmer Offer for Buyer Demand
data class FarmerOffer(
    val id: String,
    val demandId: String,
    val farmerId: String,
    val farmerName: String,
    val farmerPhone: String,
    val farmerLocation: String,
    val farmerVerified: Boolean = true,
    val offeredQuantity: Double,
    val unit: ProductUnit,
    val pricePerUnit: Double,
    val qualityGrade: QualityGrade,
    val availableDate: String,
    val note: String,
    val status: OfferStatus = OfferStatus.PENDING,
    val createdAt: String
)

// Delivery & Transport Tracking
data class DeliveryInfo(
    val pickupLocation: String,
    val collectionCenter: String, // উপজেলা এগ্রি হাব
    val deliveryLocation: String, // ঢাকা আড়ত
    val transportStatus: TransportStatus = TransportStatus.WAITING,
    val driverName: String = "মোঃ কাশেম ড্রাইভার",
    val driverPhone: String = "০১৭১১-২২৩৩৪৪",
    val vehicleNumber: String = "ঢাকা মেট্রো-ট ১১-৪৫২৩",
    val estimatedArrival: String = "আজ সন্ধ্যা ৬টা"
)

// Collection Stage Quality & Weight Verification
data class QualityVerification(
    val expectedWeight: Double,
    val actualWeight: Double,
    val unit: ProductUnit,
    val qualityGrade: QualityGrade,
    val verifiedBy: String = "মুরাদ হাসান (কোয়ালিটি ইন্সপেক্টর, সিংড়া হাব)",
    val verificationDate: String = "আজ দুপুর ২:১৫",
    val notes: String = "পণ্য ফ্রেশ এবং গ্রেড এ মানসম্পন্ন",
    val isVerified: Boolean = true
) {
    val actualGrade: QualityGrade get() = qualityGrade
    val verifiedAt: String get() = verificationDate
}

// Order System
data class MarketplaceOrder(
    val id: String,
    val orderNumber: String,
    val demandId: String? = null,
    val offerId: String? = null,
    val buyerId: String,
    val buyerName: String,
    val buyerBusinessName: String,
    val buyerPhone: String,
    val farmerId: String,
    val farmerName: String,
    val farmerPhone: String,
    val farmerLocation: String,
    val productTitle: String,
    val category: ProductCategory,
    val quantity: Double,
    val unit: ProductUnit,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val depositRequired: Double, // 20%
    val isDepositPaid: Boolean = false,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val deliveryLocation: String,
    val expectedDeliveryDate: String,
    val deliveryInfo: DeliveryInfo,
    val verification: QualityVerification,
    val hasDispute: Boolean = false,
    val isRated: Boolean = false,
    val createdAt: String
) {
    val unitPrice: Double get() = pricePerUnit
    val depositAmount: Double get() = depositRequired
    val remainingAmount: Double get() = totalAmount - depositRequired
    val transportStatus: TransportStatus get() = deliveryInfo.transportStatus
    val transportInfo: DeliveryInfo? get() = deliveryInfo
    val qualityVerification: QualityVerification? get() = verification
    val qualityGrade: QualityGrade get() = verification.qualityGrade
}

// Dispute System
data class Dispute(
    val id: String,
    val orderId: String,
    val orderNumber: String,
    val reportedByRole: UserRole,
    val reporterName: String,
    val reporterPhone: String,
    val problemType: ProblemType,
    val description: String,
    val status: DisputeStatus = DisputeStatus.OPEN,
    val adminNotes: String = "",
    val createdAt: String
) {
    val raisedByName: String get() = reporterName
    val resolutionNotes: String get() = adminNotes
}

// Rating System
data class UserReview(
    val id: String,
    val orderId: String,
    val fromUserName: String,
    val fromUserRole: UserRole,
    val toUserId: String,
    val stars: Int,
    val productQualityRating: Int = 5,
    val quantityAccuracyRating: Int = 5,
    val communicationRating: Int = 5,
    val reliabilityRating: Int = 5,
    val comment: String,
    val createdAt: String
)

// Notification
data class NotificationItem(
    val id: String,
    val targetRole: UserRole?,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val relatedOrderId: String? = null,
    val relatedDemandId: String? = null,
    val targetUserId: String? = null
) {
    val time: String get() = timestamp
}

// Chat System
data class ChatMessage(
    val id: String,
    val orderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val message: String,
    val timestamp: String
) {
    val text: String get() = message
    val time: String get() = timestamp
}
