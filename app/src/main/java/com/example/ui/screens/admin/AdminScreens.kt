package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun AdminMainScreen(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val farmers by viewModel.farmers.collectAsState()
    val buyers by viewModel.buyers.collectAsState()
    val products by viewModel.products.collectAsState()
    val demands by viewModel.demands.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val disputes by viewModel.disputes.collectAsState()

    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        topBar = {
            Surface(
                color = Color(0xFFE3F2FD),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("কৃষিবাজার অ্যাডমিন কন্ট্রোল প্যানেল", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D47A1))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1976D2)
                        ) {
                            Text("সুপার অ্যাডমিন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Admin Tabs Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AdminTab.values()) { tab ->
                            val isSelected = uiState.adminTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setAdminTab(tab) },
                                label = { Text(tab.labelBn, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1976D2),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState.adminTab) {
                AdminTab.OVERVIEW -> AdminOverviewScreen(
                    farmers = farmers,
                    buyers = buyers,
                    products = products,
                    demands = demands,
                    orders = orders,
                    disputes = disputes
                )
                AdminTab.FARMERS -> AdminFarmerVerificationScreen(
                    farmers = farmers,
                    onVerify = { id, status -> viewModel.verifyFarmer(id, status) }
                )
                AdminTab.BUYERS -> AdminBuyerVerificationScreen(
                    buyers = buyers,
                    onVerify = { id, status -> viewModel.verifyBuyer(id, status) }
                )
                AdminTab.PRODUCTS -> AdminProductModerationScreen(
                    products = products,
                    onModerate = { id, status -> viewModel.moderateProduct(id, status) }
                )
                AdminTab.DEMANDS -> AdminDemandsScreen(demands = demands)
                AdminTab.ORDERS -> AdminOrdersTrackingScreen(
                    orders = orders,
                    onOrderClick = { viewModel.openOrderDetail(it) }
                )
                AdminTab.DISPUTES -> AdminDisputesScreen(
                    disputes = disputes,
                    onResolve = { id, status, notes -> viewModel.resolveDispute(id, status, notes) }
                )
            }
        }
    }
}

@Composable
fun AdminOverviewScreen(
    farmers: List<FarmerProfile>,
    buyers: List<BuyerProfile>,
    products: List<ProductListing>,
    demands: List<BuyerDemand>,
    orders: List<MarketplaceOrder>,
    disputes: List<Dispute>
) {
    val totalGmv = remember(orders) { orders.sumOf { it.totalAmount } }
    val platformFee = remember(totalGmv) { totalGmv * 0.02 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("প্ল্যাটফর্ম সারসংক্ষেপ (Platform GMV & Metrics)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "মোট লেনদেন (GMV)",
                    value = formatTaka(totalGmv),
                    subtitle = "১০টি সক্রিয় চুক্তি",
                    icon = Icons.Default.CurrencyExchange,
                    containerColor = KrishiGreenContainer,
                    accentColor = KrishiGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "প্ল্যাটফর্ম আয় (২%)",
                    value = formatTaka(platformFee),
                    subtitle = "অটোমেটেড এসক্রো ফি",
                    icon = Icons.Default.MonetizationOn,
                    containerColor = KrishiOrangeContainer,
                    accentColor = KrishiOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "নিবন্ধিত কৃষক",
                    value = "${toBnNumber(farmers.size)} জন",
                    subtitle = "${toBnNumber(farmers.count { it.verificationStatus == VerificationStatus.VERIFIED })} জন যাচাইকৃত",
                    icon = Icons.Default.Agriculture,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "পাইকারি ক্রেতা",
                    value = "${toBnNumber(buyers.size)} প্রতিষ্ঠান",
                    subtitle = "${toBnNumber(buyers.count { it.verificationStatus == VerificationStatus.VERIFIED })} প্রতিষ্ঠান যাচাইকৃত",
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "পণ্য লিস্টিং",
                    value = "${toBnNumber(products.size)} টি",
                    subtitle = "${toBnNumber(products.count { it.status == ProductStatus.ACTIVE })} টি সক্রিয়",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ক্রেতার চাহিদা",
                    value = "${toBnNumber(demands.size)} টি",
                    subtitle = "ঢাকা ও অন্যান্য আড়ত",
                    icon = Icons.Default.Campaign,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (disputes.any { it.status == DisputeStatus.OPEN }) Color(0xFFFFEBEE) else KrishiSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("অভিযোগ ও ডিসপ্যুট সংক্রান্ত", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("মোট ${toBnNumber(disputes.size)}টি অভিযোগ (${toBnNumber(disputes.count { it.status == DisputeStatus.OPEN })}টি খোলা)", fontSize = 12.sp, color = KrishiTextSecondary)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = if (disputes.any { it.status == DisputeStatus.OPEN }) KrishiError else KrishiGreenPrimary) {
                        Text(
                            if (disputes.any { it.status == DisputeStatus.OPEN }) "তদন্ত বাকি" else "নিষ্পত্তি হয়েছে",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFarmerVerificationScreen(
    farmers: List<FarmerProfile>,
    onVerify: (String, VerificationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("কৃষক যাচাই ও অনুমোদন (${toBnNumber(farmers.size)} জন)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(farmers) { farmer ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(farmer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("ফোন: ${farmer.phone} • NID: ${farmer.nidNumber}", fontSize = 12.sp, color = KrishiTextSecondary)
                            Text("📍 ${farmer.upazila}, ${farmer.district} • ${farmer.farmerType}", fontSize = 11.sp, color = KrishiTextTertiary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (farmer.verificationStatus == VerificationStatus.VERIFIED) KrishiGreenContainer else KrishiOrangeContainer
                        ) {
                            Text(
                                farmer.verificationStatus.labelBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (farmer.verificationStatus == VerificationStatus.VERIFIED) KrishiGreenPrimary else KrishiOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (farmer.verificationStatus != VerificationStatus.VERIFIED) {
                            Button(
                                onClick = { onVerify(farmer.id, VerificationStatus.VERIFIED) },
                                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("অনুমোদন করুন ✅", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (farmer.verificationStatus != VerificationStatus.REJECTED) {
                            OutlinedButton(
                                onClick = { onVerify(farmer.id, VerificationStatus.REJECTED) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KrishiError),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("বাতিল", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBuyerVerificationScreen(
    buyers: List<BuyerProfile>,
    onVerify: (String, VerificationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("পাইকারি ক্রেতা যাচাই ও অনুমোদন (${toBnNumber(buyers.size)} প্রতিষ্ঠান)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(buyers) { buyer ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(buyer.businessName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("স্বত্বাধিকারী: ${buyer.name} • ট্রেড লাইসেন্স: ${buyer.tradeInfo}", fontSize = 12.sp, color = KrishiTextSecondary)
                            Text("📍 ${buyer.address} • অন-টাইম পে: ${toBnNumber(buyer.paymentReliability)}%", fontSize = 11.sp, color = KrishiTextTertiary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (buyer.verificationStatus == VerificationStatus.VERIFIED) KrishiGreenContainer else KrishiOrangeContainer
                        ) {
                            Text(
                                buyer.verificationStatus.labelBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (buyer.verificationStatus == VerificationStatus.VERIFIED) KrishiGreenPrimary else KrishiOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (buyer.verificationStatus != VerificationStatus.VERIFIED) {
                            Button(
                                onClick = { onVerify(buyer.id, VerificationStatus.VERIFIED) },
                                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("অনুমোদন ✅", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductModerationScreen(
    products: List<ProductListing>,
    onModerate: (String, ProductStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("পণ্য লিস্টিং মডারেশন (${toBnNumber(products.size)} টি)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${prod.category.icon} ${prod.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("কৃষক: ${prod.farmerName} • 📍 ${prod.location}", fontSize = 12.sp, color = KrishiTextSecondary)
                            Text("${toBnNumber(prod.quantity)} ${prod.unit.labelBn} @ ৳${toBnNumber(prod.expectedPrice.toInt())} • মান: ${prod.qualityGrade.labelBn}", fontSize = 12.sp, color = KrishiGreenPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (prod.status == ProductStatus.ACTIVE) KrishiGreenContainer else KrishiOrangeContainer
                        ) {
                            Text(
                                prod.status.labelBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prod.status == ProductStatus.ACTIVE) KrishiGreenPrimary else KrishiOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (prod.status != ProductStatus.ACTIVE) {
                            Button(
                                onClick = { onModerate(prod.id, ProductStatus.ACTIVE) },
                                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("অনুমোদন করুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDemandsScreen(demands: List<BuyerDemand>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("ক্রেতাদের চাহিদা তদারকি (${toBnNumber(demands.size)} টি)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(demands) { demand ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${demand.category.icon} ${demand.productTitle}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("ক্রেতা: ${demand.buyerBusinessName} • ডেলিভারি: ${demand.requiredLocation}", fontSize = 12.sp, color = KrishiTextSecondary)
                    Text("চাহিদা: ${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn} • বাজেট: ৳${toBnNumber(demand.minExpectedPrice.toInt())}–${toBnNumber(demand.maxExpectedPrice.toInt())}", fontSize = 12.sp, color = KrishiOrange)
                    Text("অফার এসেছে: ${toBnNumber(demand.offersCount)} টি • স্ট্যাটাস: ${demand.status.labelBn}", fontSize = 12.sp, color = KrishiGreenPrimary)
                }
            }
        }
    }
}

@Composable
fun AdminOrdersTrackingScreen(
    orders: List<MarketplaceOrder>,
    onOrderClick: (MarketplaceOrder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("সকল অর্ডার ট্র্যাকিং (${toBnNumber(orders.size)} টি)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(orders) { order ->
            Card(
                onClick = { onOrderClick(order) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${order.orderNumber}", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary)
                        OrderStatusBadge(status = order.orderStatus)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${order.productTitle} — ${toBnNumber(order.quantity)} ${order.unit.labelBn}", fontWeight = FontWeight.Bold)
                    Text("${order.farmerName} (কৃষক) ➔ ${order.buyerBusinessName} (ক্রেতা)", fontSize = 12.sp, color = KrishiTextSecondary)
                    Text("মোট মূল্য: ${formatTaka(order.totalAmount)} • পরিবহন: ${order.transportStatus.labelBn}", fontSize = 12.sp, color = KrishiOrange)
                }
            }
        }
    }
}

@Composable
fun AdminDisputesScreen(
    disputes: List<Dispute>,
    onResolve: (disputeId: String, status: DisputeStatus, notes: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("অভিযোগ ও ডিসপ্যুট নিষ্পত্তি (${toBnNumber(disputes.size)} টি)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(disputes) { dispute ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (dispute.status == DisputeStatus.OPEN) Color(0xFFFFEBEE) else KrishiSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("অভিযোগকারী: ${dispute.raisedByName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (dispute.status == DisputeStatus.OPEN) KrishiError else KrishiGreenPrimary
                        ) {
                            Text(dispute.status.labelBn, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text("সমস্যার ধরন: ${dispute.problemType.labelBn}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("বিবরণ: \"${dispute.description}\"", fontSize = 12.sp, color = KrishiTextSecondary)

                    if (dispute.status == DisputeStatus.OPEN) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onResolve(dispute.id, DisputeStatus.RESOLVED_REFUND, "তদন্তে সত্যতা পাওয়ায় আংশিক রিফান্ড প্রদান করা হলো।") },
                                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("রিফান্ড ও নিষ্পত্তি", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onResolve(dispute.id, DisputeStatus.REJECTED, "যথাযথ প্রমাণ না পাওয়ায় বাতিল।") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KrishiError),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("বাতিল করুন", fontSize = 11.sp)
                            }
                        }
                    } else if (dispute.resolutionNotes.isNotBlank()) {
                        Text("নিষ্পত্তি সিদ্ধান্ত: ${dispute.resolutionNotes}", fontSize = 11.sp, color = KrishiGreenPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
