package com.example.ui.screens.buyer

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
import com.example.ui.viewmodel.BuyerTab
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun BuyerMainScreen(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentBuyer by viewModel.currentBuyer.collectAsState()
    val products by viewModel.products.collectAsState()
    val demands by viewModel.demands.collectAsState()
    val offers by viewModel.offers.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val myDemands = remember(demands, currentBuyer) {
        demands.filter { it.buyerId == currentBuyer?.id }
    }
    val myOrders = remember(orders, currentBuyer) {
        orders.filter { it.buyerId == currentBuyer?.id }
    }

    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        bottomBar = {
            SleekBottomNavigationBar(
                items = BuyerTab.values().toList(),
                selectedItem = uiState.buyerTab,
                onItemSelected = { viewModel.setBuyerTab(it) },
                labelProvider = { it.labelBn },
                iconProvider = { tab ->
                    when (tab) {
                        BuyerTab.HOME -> Icons.Default.Home
                        BuyerTab.SEARCH -> Icons.Default.Search
                        BuyerTab.DEMANDS -> Icons.Default.Assignment
                        BuyerTab.ORDERS -> Icons.Default.ShoppingCart
                        BuyerTab.PROFILE -> Icons.Default.Business
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
            when (uiState.buyerTab) {
                BuyerTab.HOME -> BuyerHomeScreen(
                    buyer = currentBuyer,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = {
                        viewModel.setSelectedCategory(it)
                        viewModel.setBuyerTab(BuyerTab.SEARCH)
                    },
                    onSearchClick = { viewModel.setBuyerTab(BuyerTab.SEARCH) },
                    onPostDemandClick = { viewModel.openAddDemandDialog() },
                    myDemands = myDemands,
                    myOrders = myOrders,
                    onViewOffers = { viewModel.openDemandOfferManagement(it) },
                    onOrderClick = { viewModel.openOrderDetail(it) }
                )
                BuyerTab.SEARCH -> BuyerProductSearchScreen(
                    products = products,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                    selectedDistrict = uiState.selectedDistrict,
                    onDistrictSelect = { viewModel.setSelectedDistrict(it) },
                    verifiedOnly = uiState.verifiedOnlyFilter,
                    onToggleVerifiedOnly = { viewModel.toggleVerifiedOnlyFilter() },
                    onOrderProduct = { product ->
                        viewModel.orderDirectProduct(product.id, product.quantity.coerceAtMost(100.0))
                    }
                )
                BuyerTab.DEMANDS -> BuyerDemandListScreen(
                    demands = myDemands,
                    offers = offers,
                    onPostDemandClick = { viewModel.openAddDemandDialog() },
                    onViewOffers = { viewModel.openDemandOfferManagement(it) }
                )
                BuyerTab.ORDERS -> BuyerOrdersScreen(
                    orders = myOrders,
                    onOrderClick = { viewModel.openOrderDetail(it) },
                    onPayDeposit = { viewModel.payDeposit(it.id) }
                )
                BuyerTab.PROFILE -> BuyerProfileView(
                    buyer = currentBuyer,
                    completedOrdersCount = myOrders.count { it.orderStatus == OrderStatus.COMPLETED }
                )
            }
        }
    }
}

@Composable
fun BuyerHomeScreen(
    buyer: BuyerProfile?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: ProductCategory?,
    onSelectCategory: (ProductCategory) -> Unit,
    onSearchClick: () -> Unit,
    onPostDemandClick: () -> Unit,
    myDemands: List<BuyerDemand>,
    myOrders: List<MarketplaceOrder>,
    onViewOffers: (BuyerDemand) -> Unit,
    onOrderClick: (MarketplaceOrder) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Buyer Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KrishiGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏢", fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = buyer?.businessName ?: "কাওরান বাজার পাইকারি আড়ত",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "স্বত্বাধিকারী: ${buyer?.name ?: "হাজী সালাহউদ্দিন"} • ${buyer?.district ?: "ঢাকা"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifiedBadge(
                            isVerified = buyer?.verificationStatus == VerificationStatus.VERIFIED,
                            text = if (buyer?.verificationStatus == VerificationStatus.VERIFIED) "ভেরিফাইড বায়ার ✅" else "যাচাই প্রক্রিয়াধীন"
                        )
                        Text(
                            text = "পেমেন্ট নির্ভরযোগ্যতা: ${toBnNumber(buyer?.paymentReliability ?: 98)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search Bar: “আপনার কী পণ্য দরকার?”
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("আপনার কী পণ্য দরকার? (যেমন: টমেটো, আলু, পেঁয়াজ)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KrishiGreenPrimary) },
                trailingIcon = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "খুঁজুন", tint = KrishiGreenPrimary)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KrishiGreenPrimary,
                    unfocusedBorderColor = KrishiOutline,
                    focusedContainerColor = KrishiSurface,
                    unfocusedContainerColor = KrishiSurface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("buyer_search_input")
            )
        }

        // Two Big Action Buttons: “পণ্য খুঁজুন” এবং “আমার প্রয়োজন পোস্ট করুন”
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: পণ্য খুঁজুন
                Card(
                    onClick = onSearchClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KrishiGreenContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_search_products")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = KrishiGreenPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "পণ্য খুঁজুন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiGreenPrimary
                        )
                        Text(
                            text = "কৃষকের সরাসরি ফসল",
                            fontSize = 11.sp,
                            color = KrishiTextSecondary
                        )
                    }
                }

                // Button 2: আমার প্রয়োজন পোস্ট করুন (Most Important Feature)
                Card(
                    onClick = onPostDemandClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KrishiOrangeContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_post_demand")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = KrishiOrange,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "আমার প্রয়োজন পোস্ট করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiOrangeOnContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "কৃষকদের অফার আহ্বান",
                            fontSize = 11.sp,
                            color = KrishiTextSecondary
                        )
                    }
                }
            }
        }

        // Category Pills
        item {
            Text(
                text = "পণ্যের ক্যাটাগরি",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ProductCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cat.labelBn, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KrishiGreenContainer,
                            selectedLabelColor = KrishiGreenPrimary
                        )
                    )
                }
            }
        }

        // My Posted Demands Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আমার প্রকাশিত চাহিদা (${toBnNumber(myDemands.size)} টি)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiTextPrimary
                )
                TextButton(onClick = onPostDemandClick) {
                    Text("+ নতুন চাহিদা", color = KrishiOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(myDemands.take(3)) { demand ->
            BuyerDemandManagementCard(
                demand = demand,
                onViewOffers = { onViewOffers(demand) }
            )
        }

        // Active Orders
        if (myOrders.isNotEmpty()) {
            item {
                Text(
                    text = "আমার সাম্প্রতিক অর্ডার 📦",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiTextPrimary
                )
            }
            items(myOrders.take(3)) { order ->
                BuyerOrderCard(
                    order = order,
                    onClick = { onOrderClick(order) },
                    onPayDeposit = {}
                )
            }
        }
    }
}

@Composable
fun BuyerDemandManagementCard(
    demand: BuyerDemand,
    onViewOffers: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KrishiOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = demand.category.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = demand.productTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (demand.offersCount > 0) KrishiGreenContainer else KrishiSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (demand.offersCount > 0) KrishiGreenLight else KrishiOutline)
                ) {
                    Text(
                        text = if (demand.offersCount > 0) "${toBnNumber(demand.offersCount)}টি অফার এসেছে 🔔" else "অফারের অপেক্ষায়",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (demand.offersCount > 0) KrishiGreenPrimary else KrishiTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "প্রয়োজন: ${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn} • কাঙ্ক্ষিত দর: ৳${toBnNumber(demand.minExpectedPrice.toInt())}–${toBnNumber(demand.maxExpectedPrice.toInt())}/${demand.unit.labelBn}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = KrishiTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ডেলিভারি লোকেশন: ${demand.requiredLocation} • তারিখ: ${demand.requiredDate}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = KrishiTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewOffers,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KrishiGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_offers_button_${demand.id}")
            ) {
                Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "প্রাপ্ত অফার দেখুন ও নির্বাচন করুন (${toBnNumber(demand.offersCount)} টি)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BuyerProductSearchScreen(
    products: List<ProductListing>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ProductCategory?,
    onCategorySelect: (ProductCategory?) -> Unit,
    selectedDistrict: String,
    onDistrictSelect: (String) -> Unit,
    verifiedOnly: Boolean,
    onToggleVerifiedOnly: () -> Unit,
    onOrderProduct: (ProductListing) -> Unit
) {
    val districts = listOf("সকল জেলা", "রাজশাহী", "বগুড়া", "পাবনা", "যশোর", "দিনাজপুর", "ময়মনসিংহ", "রংপুর", "নাটোর", "চুয়াডাঙ্গা", "টাঙ্গাইল")
    var showDistrictMenu by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedDistrict, verifiedOnly) {
        products.filter { prod ->
            val matchQuery = searchQuery.isBlank() || prod.title.contains(searchQuery, ignoreCase = true) || prod.farmerDistrict.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || prod.category == selectedCategory
            val matchDistrict = selectedDistrict == "সকল জেলা" || prod.farmerDistrict.contains(selectedDistrict, ignoreCase = true)
            val matchVerified = !verifiedOnly || prod.farmerVerified
            matchQuery && matchCategory && matchDistrict && matchVerified
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("পণ্য, ফসল বা জেলার নাম দিয়ে খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KrishiGreenPrimary) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // District filter & Verified filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                OutlinedButton(
                    onClick = { showDistrictMenu = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = KrishiGreenPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedDistrict, fontSize = 12.sp, color = KrishiTextPrimary)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showDistrictMenu,
                    onDismissRequest = { showDistrictMenu = false }
                ) {
                    districts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                onDistrictSelect(dist)
                                showDistrictMenu = false
                            }
                        )
                    }
                }
            }

            FilterChip(
                selected = verifiedOnly,
                onClick = onToggleVerifiedOnly,
                label = { Text("ভেরিফাইড কৃষক ✅", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Categories row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("সব ক্যাটাগরি") }
                )
            }
            items(ProductCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelect(cat) },
                    label = { Text("${cat.icon} ${cat.labelBn}") }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "উপলব্ধ ফসল (${toBnNumber(filteredProducts.size)} টি)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = KrishiTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredProducts) { prod ->
                BuyerProductCard(
                    product = prod,
                    onOrderClick = { onOrderProduct(prod) }
                )
            }
        }
    }
}

@Composable
fun BuyerProductCard(
    product: ProductListing,
    onOrderClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("buyer_product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KrishiGreenContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(product.category.icon, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = product.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.farmerName,
                                fontSize = 12.sp,
                                color = KrishiTextSecondary
                            )
                            if (product.farmerVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = KrishiGreenPrimary, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KrishiGreenContainer
                ) {
                    Text(
                        text = "৳${toBnNumber(product.expectedPrice.toInt())}/${product.unit.labelBn}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiGreenPrimary,
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
                    text = "অবশিষ্ট: ${toBnNumber(product.remainingQuantity)} ${product.unit.labelBn}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = KrishiTextPrimary
                )
                Text(
                    text = "📍 ${product.location}",
                    fontSize = 12.sp,
                    color = KrishiTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "গ্রেড: ${product.qualityGrade.labelBn} • ডেলিভারি সময়: ${product.availableDate}",
                fontSize = 12.sp,
                color = KrishiTextTertiary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOrderClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KrishiOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_product_button_${product.id}")
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("সরাসরি অর্ডার / বুকিং করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BuyerDemandListScreen(
    demands: List<BuyerDemand>,
    offers: List<FarmerOffer>,
    onPostDemandClick: () -> Unit,
    onViewOffers: (BuyerDemand) -> Unit
) {
    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPostDemandClick,
                containerColor = KrishiOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("নতুন চাহিদা পোস্ট করুন", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_post_demand")
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
                text = "আমার চাহিদাসমূহ (${toBnNumber(demands.size)} টি)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Text(
                text = "চাহিদাপত্র পোস্ট করে সরাসরি কৃষকদের সেরা অফার যাচাই করুন",
                fontSize = 12.sp,
                color = KrishiTextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(demands) { demand ->
                    BuyerDemandManagementCard(
                        demand = demand,
                        onViewOffers = { onViewOffers(demand) }
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerOrdersScreen(
    orders: List<MarketplaceOrder>,
    onOrderClick: (MarketplaceOrder) -> Unit,
    onPayDeposit: (MarketplaceOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "আমার ক্রয় ও অর্ডারসমূহ (${toBnNumber(orders.size)} টি)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KrishiTextPrimary
        )
        Text(
            text = "ডিপোজিট প্রদান, ওজন যাচাই ও ট্রাক ট্র্যাকিং",
            fontSize = 12.sp,
            color = KrishiTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("বর্তমানে কোনো অর্ডার নেই", fontSize = 15.sp, color = KrishiTextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(orders) { order ->
                    BuyerOrderCard(
                        order = order,
                        onClick = { onOrderClick(order) },
                        onPayDeposit = { onPayDeposit(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerOrderCard(
    order: MarketplaceOrder,
    onClick: () -> Unit,
    onPayDeposit: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("buyer_order_${order.orderNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = KrishiGreenPrimary
                )
                OrderStatusBadge(status = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${order.productTitle} — ${toBnNumber(order.quantity)} ${order.unit.labelBn}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrishiTextPrimary
            )
            Text(
                text = "কৃষক: ${order.farmerName} (${order.farmerLocation})",
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
                if (!order.isDepositPaid) {
                    Button(
                        onClick = onPayDeposit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KrishiOrange),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ডিপোজিট পে করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = KrishiGreenContainer
                    ) {
                        Text(
                            text = "ডিপোজিট পেইড ✅",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerProfileView(
    buyer: BuyerProfile?,
    completedOrdersCount: Int
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
                        color = KrishiOrangeContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏢", fontSize = 38.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = buyer?.businessName ?: "কাওরান বাজার পাইকারি আড়ত",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                    Text(
                        text = "মালিক: ${buyer?.name ?: "হাজী সালাহউদ্দিন"}",
                        fontSize = 14.sp,
                        color = KrishiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    VerifiedBadge(
                        isVerified = buyer?.verificationStatus == VerificationStatus.VERIFIED,
                        text = if (buyer?.verificationStatus == VerificationStatus.VERIFIED) "ভেরিফাইড বায়ার ✅" else "যাচাই প্রক্রিয়াধীন"
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
                    Text("ব্যবসায়িক তথ্য", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider(color = KrishiOutline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ব্যবসার ধরন:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(buyer?.businessType ?: "পাইকারি আড়তদার", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ট্রেড লাইসেন্স নং:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(buyer?.tradeInfo ?: "TR-DH-892182", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ঠিকানা:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text(buyer?.address ?: "কাওরান বাজার, ঢাকা", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("সম্পন্ন অর্ডার:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text("${toBnNumber(buyer?.completedOrders ?: 140)} টি", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("পেমেন্ট অন-টাইম স্কোর:", color = KrishiTextSecondary, fontSize = 13.sp)
                        Text("${toBnNumber(buyer?.paymentReliability ?: 99)}%", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
