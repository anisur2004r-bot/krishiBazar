package com.example.ui.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FarmerTab
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun FarmerMainScreen(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentFarmer by viewModel.currentFarmer.collectAsState()
    val products by viewModel.products.collectAsState()
    val demands by viewModel.demands.collectAsState()
    val offers by viewModel.offers.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val myProducts = remember(products, currentFarmer) {
        products.filter { it.farmerId == currentFarmer?.id }
    }
    val myOffers = remember(offers, currentFarmer) {
        offers.filter { it.farmerId == currentFarmer?.id }
    }
    val myOrders = remember(orders, currentFarmer) {
        orders.filter { it.farmerId == currentFarmer?.id }
    }

    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        bottomBar = {
            SleekBottomNavigationBar(
                items = FarmerTab.values().toList(),
                selectedItem = uiState.farmerTab,
                onItemSelected = { viewModel.setFarmerTab(it) },
                labelProvider = { it.labelBn },
                iconProvider = { tab ->
                    when (tab) {
                        FarmerTab.HOME -> Icons.Default.Home
                        FarmerTab.PRODUCTS -> Icons.Default.Inventory2
                        FarmerTab.DEMANDS -> Icons.Default.Campaign
                        FarmerTab.ORDERS -> Icons.Default.LocalShipping
                        FarmerTab.PROFILE -> Icons.Default.Person
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.farmerTab) {
                FarmerTab.HOME -> FarmerHomeScreen(
                    farmer = currentFarmer,
                    myProductsCount = myProducts.size,
                    demandsCount = demands.size,
                    myOffersCount = myOffers.size,
                    myOrders = myOrders,
                    activeDemands = demands.take(4),
                    onSellClick = { viewModel.openAddProductDialog() },
                    onDemandsClick = { viewModel.setFarmerTab(FarmerTab.DEMANDS) },
                    onProductsClick = { viewModel.setFarmerTab(FarmerTab.PRODUCTS) },
                    onOffersClick = { viewModel.setFarmerTab(FarmerTab.DEMANDS) },
                    onOrdersClick = { viewModel.setFarmerTab(FarmerTab.ORDERS) },
                    onDemandOfferClick = { viewModel.openOfferDialog(it) },
                    onOrderClick = { viewModel.openOrderDetail(it) }
                )
                FarmerTab.PRODUCTS -> FarmerProductsScreen(
                    products = myProducts,
                    onAddProductClick = { viewModel.openAddProductDialog() },
                    onDeleteProduct = { viewModel.deleteProduct(it) }
                )
                FarmerTab.DEMANDS -> FarmerDemandsFeedScreen(
                    demands = demands,
                    onSendOfferClick = { viewModel.openOfferDialog(it) }
                )
                FarmerTab.ORDERS -> FarmerOrdersScreen(
                    orders = myOrders,
                    onOrderClick = { viewModel.openOrderDetail(it) }
                )
                FarmerTab.PROFILE -> FarmerProfileView(
                    farmer = currentFarmer,
                    completedOrdersCount = myOrders.count { it.orderStatus == OrderStatus.COMPLETED },
                    onEditProfile = { viewModel.showSnackbar("প্রোফাইল সম্পাদন অপশন শীঘ্রই আসছে") }
                )
            }
        }
    }
}

@Composable
fun FarmerHomeScreen(
    farmer: FarmerProfile?,
    myProductsCount: Int,
    demandsCount: Int,
    myOffersCount: Int,
    myOrders: List<MarketplaceOrder>,
    activeDemands: List<BuyerDemand>,
    onSellClick: () -> Unit,
    onDemandsClick: () -> Unit,
    onProductsClick: () -> Unit,
    onOffersClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onDemandOfferClick: (BuyerDemand) -> Unit,
    onOrderClick: (MarketplaceOrder) -> Unit
) {
    val totalEarnings = remember(myOrders) {
        myOrders.filter { it.orderStatus == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Farmer Header Profile Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👨‍🌾", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = farmer?.name ?: "মো: আব্দুল রহিম",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "📍 ${farmer?.upazila ?: "গোদাগাড়ী"}, ${farmer?.district ?: "রাজশাহী"} • ${farmer?.farmerType ?: "বাণিজ্যিক খামারি"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifiedBadge(
                            isVerified = farmer?.verificationStatus == VerificationStatus.VERIFIED,
                            text = if (farmer?.verificationStatus == VerificationStatus.VERIFIED) "ভেরিফাইড কৃষক ✅" else "যাচাই প্রক্রিয়াধীন (Pending)"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${farmer?.rating ?: 4.9} (${toBnNumber(farmer?.reviewsCount ?: 18)} রিভিউ)",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // BIG CTA BUTTON: “আমি পণ্য বিক্রি করতে চাই”
        item {
            LargeActionButton(
                text = "আমি পণ্য বিক্রি করতে চাই",
                icon = Icons.Default.AddCircle,
                onClick = onSellClick,
                containerColor = KrishiGreenSecondary,
                modifier = Modifier.testTag("farmer_sell_product_button")
            )
        }

        // Stats Grid (2x2 sleek cards)
        item {
            Text(
                text = "আমার খামার ড্যাশবোর্ড",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "আমার পণ্য",
                    value = "${toBnNumber(myProductsCount)} টি",
                    icon = Icons.Default.Inventory2,
                    accentColor = KrishiGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onProductsClick
                )
                StatCard(
                    title = "অফারসমূহ",
                    value = "${toBnNumber(myOffersCount)} টি",
                    icon = Icons.Default.LocalOffer,
                    accentColor = KrishiOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onOffersClick
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "চলমান অর্ডার",
                    value = "${toBnNumber(myOrders.count { it.orderStatus != OrderStatus.COMPLETED && it.orderStatus != OrderStatus.CANCELLED })} টি",
                    icon = Icons.Default.LocalShipping,
                    accentColor = KrishiInfo,
                    modifier = Modifier.weight(1f),
                    onClick = onOrdersClick
                )
                StatCard(
                    title = "মোট আয়",
                    value = formatTaka(totalEarnings),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = KrishiTextPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onOrdersClick
                )
            }
        }

        // Section: ঢাকার ক্রেতাদের চাহিদা (Demand Feed Shortcut)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ঢাকার পাইকারি ক্রেতাদের চাহিদা 📢",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiTextPrimary
                )
                TextButton(onClick = onDemandsClick) {
                    Text("সব দেখুন", color = KrishiGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(activeDemands) { demand ->
            SleekDemandCard(
                demand = demand,
                onOfferClick = { onDemandOfferClick(demand) },
                isUrgent = demand.qualityGrade == QualityGrade.GRADE_A
            )
        }

        // Section: চলমান অর্ডার
        if (myOrders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "চলমান ও সাম্প্রতিক অর্ডার 📦",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                    TextButton(onClick = onOrdersClick) {
                        Text("সব অর্ডার", color = KrishiGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            items(myOrders.take(3)) { order ->
                FarmerOrderCard(
                    order = order,
                    onClick = { onOrderClick(order) }
                )
            }
        }
    }
}

@Composable
fun BuyerDemandCard(
    demand: BuyerDemand,
    onSendOfferClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("demand_card_${demand.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = demand.category.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = demand.productTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextPrimary
                        )
                        Text(
                            text = "${demand.buyerBusinessName} • ${demand.buyerDistrict}",
                            fontSize = 12.sp,
                            color = KrishiTextSecondary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KrishiOrangeContainer
                ) {
                    Text(
                        text = demand.qualityGrade.labelBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiOrangeOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = KrishiOutline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Demand Requirements Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("প্রয়োজনীয় পরিমাণ", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = "${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiGreenPrimary
                    )
                }
                Column {
                    Text("কাঙ্ক্ষিত মূল্য", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = "৳${toBnNumber(demand.minExpectedPrice.toInt())}–${toBnNumber(demand.maxExpectedPrice.toInt())}/${demand.unit.labelBn}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiOrange
                    )
                }
                Column {
                    Text("সরবরাহের তারিখ", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = demand.requiredDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = KrishiTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = KrishiGreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ডেলিভারি লোকেশন: ${demand.requiredLocation}",
                    fontSize = 12.sp,
                    color = KrishiTextSecondary
                )
            }

            if (demand.additionalNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "নোট: \"${demand.additionalNote}\"",
                    fontSize = 12.sp,
                    color = KrishiTextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            // Big button: “আমি দিতে পারব”
            Button(
                onClick = onSendOfferClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KrishiGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .testTag("send_offer_button_${demand.id}")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "আমি দিতে পারব (অফার পাঠান)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FarmerProductsScreen(
    products: List<ProductListing>,
    onAddProductClick: () -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ProductStatus?>(null) }
    val filtered = remember(products, selectedFilter) {
        if (selectedFilter == null) products else products.filter { it.status == selectedFilter }
    }

    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProductClick,
                containerColor = KrishiOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("নতুন পণ্য যোগ করুন", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_product")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "আমার লিস্টিং করা পণ্য (${toBnNumber(products.size)} টি)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("সকল") }
                    )
                }
                items(ProductStatus.values()) { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = status },
                        label = { Text(status.labelBn) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("কোনো পণ্য লিস্টিং নেই", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("নিচের বাটনে চাপ দিয়ে আপনার নতুন ফসল লিস্টিং করুন।", fontSize = 13.sp, color = KrishiTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered) { prod ->
                        FarmerProductCard(
                            product = prod,
                            onDelete = { onDeleteProduct(prod.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerProductCard(
    product: ProductListing,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KrishiOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = product.category.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = product.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextPrimary
                        )
                        Text(
                            text = "ক্যাটেগরি: ${product.category.labelBn} • ${product.location}",
                            fontSize = 12.sp,
                            color = KrishiTextSecondary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (product.status) {
                        ProductStatus.ACTIVE -> KrishiGreenContainer
                        ProductStatus.PENDING -> KrishiOrangeContainer
                        ProductStatus.SOLD -> Color(0xFFE0E0E0)
                        else -> Color(0xFFFFEBEE)
                    }
                ) {
                    Text(
                        text = product.status.labelBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (product.status) {
                            ProductStatus.ACTIVE -> KrishiGreenPrimary
                            ProductStatus.PENDING -> KrishiOrange
                            ProductStatus.SOLD -> Color(0xFF616161)
                            else -> KrishiError
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "মোট: ${toBnNumber(product.quantity)} ${product.unit.labelBn} (অবশিষ্ট: ${toBnNumber(product.remainingQuantity)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = KrishiGreenPrimary
                )
                Text(
                    text = "প্রত্যাশিত: ৳${toBnNumber(product.expectedPrice.toInt())}/${product.unit.labelBn}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiOrange
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "মান: ${product.qualityGrade.labelBn} • ফসল তোলার তারিখ: ${product.harvestDate}",
                fontSize = 12.sp,
                color = KrishiTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KrishiError),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("মুছুন", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FarmerDemandsFeedScreen(
    demands: List<BuyerDemand>,
    onSendOfferClick: (BuyerDemand) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ঢাকার ক্রেতাদের চাহিদা ফিড (${toBnNumber(demands.size)} টি)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KrishiTextPrimary
        )
        Text(
            text = "সরাসরি পাইকারি ক্রেতাদের কাছে আপনার ফসলের অফার পাঠান",
            fontSize = 12.sp,
            color = KrishiTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(demands) { demand ->
                BuyerDemandCard(
                    demand = demand,
                    onSendOfferClick = { onSendOfferClick(demand) }
                )
            }
        }
    }
}

@Composable
fun FarmerOrdersScreen(
    orders: List<MarketplaceOrder>,
    onOrderClick: (MarketplaceOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "আমার অর্ডার ও বিক্রয় (${toBnNumber(orders.size)} টি)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KrishiTextPrimary
        )
        Text(
            text = "ডিপোজিট ও অর্ডার অগ্রগতি ট্র্যাক করুন",
            fontSize = 12.sp,
            color = KrishiTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("বর্তমানে কোনো চলমান অর্ডার নেই", fontSize = 15.sp, color = KrishiTextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(orders) { order ->
                    FarmerOrderCard(
                        order = order,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerOrderCard(
    order: MarketplaceOrder,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KrishiGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "• ${order.createdAt}", fontSize = 11.sp, color = KrishiTextTertiary)
                }
                OrderStatusBadge(status = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${order.productTitle} — ${toBnNumber(order.quantity)} ${order.unit.labelBn}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ক্রেতা: ${order.buyerBusinessName} (${order.buyerName})",
                fontSize = 13.sp,
                color = KrishiTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("মোট মূল্য", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = formatTaka(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiGreenPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.isDepositPaid) KrishiGreenContainer else KrishiOrangeContainer
                ) {
                    Text(
                        text = if (order.isDepositPaid) "ডিপোজিট পেইড ✅" else "ডিপোজিট বাকি ⏳",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isDepositPaid) KrishiGreenPrimary else KrishiOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerProfileView(
    farmer: FarmerProfile?,
    completedOrdersCount: Int,
    onEditProfile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = KrishiGreenContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍🌾", fontSize = 38.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = farmer?.name ?: "মো: আব্দুল রহিম",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                    Text(
                        text = "মোবাইল: ${farmer?.phone ?: "০১৭০৯-১২২৩৩৩"}",
                        fontSize = 13.sp,
                        color = KrishiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    VerifiedBadge(
                        isVerified = farmer?.verificationStatus == VerificationStatus.VERIFIED,
                        text = if (farmer?.verificationStatus == VerificationStatus.VERIFIED) "ভেরিফাইড কৃষক ✅" else "যাচাই প্রক্রিয়াধীন (Pending)"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("খামারের তথ্য ও ঠিকানা", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider(color = KrishiOutline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("জেলা:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(farmer?.district ?: "রাজশাহী", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("উপজেলা:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(farmer?.upazila ?: "গোদাগাড়ী", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ইউনিয়ন/গ্রাম:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(farmer?.union ?: "মহিষবাথান", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("কৃষকের ধরন:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(farmer?.farmerType ?: "বাণিজ্যিক খামারি", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("সম্পন্ন অর্ডার:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text("${toBnNumber(farmer?.totalCompletedOrders ?: 38)} টি", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("রেটিং ও সুনাম:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text("⭐ ${farmer?.rating ?: 4.9} (${toBnNumber(farmer?.reviewsCount ?: 18)} রিভিউ)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            LargeActionButton(
                text = "প্রোফাইল তথ্য সম্পাদন করুন",
                icon = Icons.Default.Edit,
                onClick = onEditProfile,
                containerColor = KrishiGreenPrimary
            )
        }
    }
}
