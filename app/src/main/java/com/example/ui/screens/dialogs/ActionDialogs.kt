package com.example.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

// 1. ADD PRODUCT DIALOG (FARMER)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onSubmit: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.VEGETABLES) }
    var quantityText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(ProductUnit.KG) }
    var expectedPriceText by remember { mutableStateOf("") }
    var minPriceText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("গোদাগাড়ী, রাজশাহী") }
    var availableDate by remember { mutableStateOf("১৫ সেপ্টেম্বর ২০২৪") }
    var harvestDate by remember { mutableStateOf("১০ সেপ্টেম্বর ২০২৪") }
    var selectedGrade by remember { mutableStateOf(QualityGrade.GRADE_A) }
    var description by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("নতুন পণ্য লিস্টিং (বিক্রি)", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KrishiSurface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পণ্যের নাম (যেমন: দেশি লাল টমেটো)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_product_title")
                )

                Text("ক্যাটাগরি নির্বাচন করুন:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ProductCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.icon} ${cat.labelBn}") }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("পরিমাণ") },
                        modifier = Modifier.weight(1.5f).testTag("input_product_quantity")
                    )
                    // Unit selector
                    var unitExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { unitExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(selectedUnit.labelBn)
                        }
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            ProductUnit.values().forEach { u ->
                                DropdownMenuItem(text = { Text(u.labelBn) }, onClick = { selectedUnit = u; unitExpanded = false })
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = expectedPriceText,
                        onValueChange = { expectedPriceText = it },
                        label = { Text("কাঙ্ক্ষিত দর (৳/ইউনিট)") },
                        modifier = Modifier.weight(1f).testTag("input_product_price")
                    )
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { minPriceText = it },
                        label = { Text("সর্বনিম্ন দর (৳)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("খামারের অবস্থান / ডেলিভারি পয়েন্ট") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("ফসল তোলার তারিখ") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = availableDate,
                        onValueChange = { availableDate = it },
                        label = { Text("ডেলিভারির তারিখ") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("গুণগত মান (Grade):", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(QualityGrade.values()) { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.labelBn) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("পণ্যের বিস্তারিত বিবরণ") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))
                LargeActionButton(
                    text = "লিস্টিং নিশ্চিত করুন",
                    icon = Icons.Default.Check,
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 100.0
                        val price = expectedPriceText.toDoubleOrNull() ?: 50.0
                        val minP = minPriceText.toDoubleOrNull() ?: (price * 0.9)
                        val name = if (title.isBlank()) "নতুন ফসল" else title
                        onSubmit(name, selectedCategory, qty, selectedUnit, price, minP, location, availableDate, harvestDate, selectedGrade, description)
                    },
                    modifier = Modifier.testTag("button_confirm_add_product")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// 2. ADD BUYER DEMAND DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBuyerDemandDialog(
    onDismiss: () -> Unit,
    onSubmit: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.VEGETABLES) }
    var quantityText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(ProductUnit.KG) }
    var minPriceText by remember { mutableStateOf("") }
    var maxPriceText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("কাওরান বাজার, ঢাকা") }
    var requiredDate by remember { mutableStateOf("২৫ সেপ্টেম্বর ২০২৪") }
    var selectedGrade by remember { mutableStateOf(QualityGrade.GRADE_A) }
    var note by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("নতুন চাহিদা পোস্ট করুন (ক্রেতা)", fontWeight = FontWeight.Bold, color = KrishiOrange) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KrishiSurface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কী পণ্য প্রয়োজন? (যেমন: ফ্রেশ টমেটো)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_demand_title")
                )

                Text("ক্যাটাগরি নির্বাচন করুন:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ProductCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.icon} ${cat.labelBn}") }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("প্রয়োজনীয় পরিমাণ") },
                        modifier = Modifier.weight(1.5f).testTag("input_demand_quantity")
                    )
                    var unitExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { unitExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(selectedUnit.labelBn)
                        }
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            ProductUnit.values().forEach { u ->
                                DropdownMenuItem(text = { Text(u.labelBn) }, onClick = { selectedUnit = u; unitExpanded = false })
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { minPriceText = it },
                        label = { Text("সর্বনিম্ন বাজেট (৳)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPriceText,
                        onValueChange = { maxPriceText = it },
                        label = { Text("সর্বোচ্চ বাজেট (৳)") },
                        modifier = Modifier.weight(1f).testTag("input_demand_max_price")
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("ডেলিভারি গ্রহণের স্থান (যেমন: কাওরান বাজার, ঢাকা)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = requiredDate,
                    onValueChange = { requiredDate = it },
                    label = { Text("কবে প্রয়োজন? (তারিখ)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("কাঙ্ক্ষিত গ্রেড:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(QualityGrade.values()) { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.labelBn) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিশেষ কোনো নির্দেশনা বা নোট") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))
                LargeActionButton(
                    text = "চাহিদাপত্র পোস্ট করুন",
                    icon = Icons.Default.Campaign,
                    containerColor = KrishiOrange,
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 500.0
                        val minP = minPriceText.toDoubleOrNull() ?: 40.0
                        val maxP = maxPriceText.toDoubleOrNull() ?: 45.0
                        val name = if (title.isBlank()) "কৃষি পণ্য" else title
                        onSubmit(name, selectedCategory, qty, selectedUnit, location, requiredDate, minP, maxP, selectedGrade, note)
                    },
                    modifier = Modifier.testTag("button_confirm_add_demand")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// 3. FARMER SEND OFFER DIALOG
@Composable
fun FarmerSendOfferDialog(
    demand: BuyerDemand,
    onDismiss: () -> Unit,
    onSubmit: (
        offeredQuantity: Double,
        unit: ProductUnit,
        pricePerUnit: Double,
        qualityGrade: QualityGrade,
        availableDate: String,
        note: String
    ) -> Unit
) {
    var offeredQuantityText by remember { mutableStateOf(demand.requiredQuantity.toInt().toString()) }
    var pricePerUnitText by remember { mutableStateOf(demand.minExpectedPrice.toInt().toString()) }
    var selectedGrade by remember { mutableStateOf(demand.qualityGrade) }
    var availableDate by remember { mutableStateOf(demand.requiredDate) }
    var note by remember { mutableStateOf("সম্পূর্ণ খাঁটি ও টাটকা ফসল সরবরাহ করব।") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("অফার পাঠান: ${demand.productTitle}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("ক্রেতার চাহিদা: ${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn} (বাজেট ৳${toBnNumber(demand.minExpectedPrice.toInt())}–${toBnNumber(demand.maxExpectedPrice.toInt())})", fontSize = 12.sp, color = KrishiTextSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = offeredQuantityText,
                    onValueChange = { offeredQuantityText = it },
                    label = { Text("আপনি কতটুকু দিতে পারবেন? (${demand.unit.labelBn})") },
                    modifier = Modifier.fillMaxWidth().testTag("input_offer_qty")
                )

                OutlinedTextField(
                    value = pricePerUnitText,
                    onValueChange = { pricePerUnitText = it },
                    label = { Text("আপনার প্রস্তাবিত দর (৳ প্রতি ${demand.unit.labelBn})") },
                    modifier = Modifier.fillMaxWidth().testTag("input_offer_price")
                )

                OutlinedTextField(
                    value = availableDate,
                    onValueChange = { availableDate = it },
                    label = { Text("সরবরাহের সম্ভাব্য তারিখ") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট / বার্তা") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = offeredQuantityText.toDoubleOrNull() ?: demand.requiredQuantity
                    val price = pricePerUnitText.toDoubleOrNull() ?: demand.minExpectedPrice
                    onSubmit(qty, demand.unit, price, selectedGrade, availableDate, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                modifier = Modifier.testTag("button_submit_offer")
            ) {
                Text("অফার জমা দিন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

// 4. DISPUTE / PROBLEM REPORTING DIALOG
@Composable
fun DisputeReportDialog(
    order: MarketplaceOrder,
    onDismiss: () -> Unit,
    onSubmit: (ProblemType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(ProblemType.WEIGHT_SHORTAGE) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("অর্ডার #${order.orderNumber} সমস্যা রিপোর্ট", fontWeight = FontWeight.Bold, color = KrishiError)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("সমস্যার ধরন নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                ProblemType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedType = type }
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(type.labelBn, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিস্তারিত বিবরণ লিখুন") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val desc = if (description.isBlank()) "সমস্যার বিবরণ প্রদান করা হয়েছে।" else description
                    onSubmit(selectedType, desc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiError)
            ) {
                Text("অভিযোগ জমা দিন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

// 5. RATING & REVIEW DIALOG
@Composable
fun RatingReviewDialog(
    order: MarketplaceOrder,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, quality: Int, accuracy: Int, communication: Int, reliability: Int, comment: String) -> Unit
) {
    var overallStars by remember { mutableStateOf(5) }
    var qualityStars by remember { mutableStateOf(5) }
    var accuracyStars by remember { mutableStateOf(5) }
    var communicationStars by remember { mutableStateOf(5) }
    var reliabilityStars by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("রেটিং ও মতামত প্রদান", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("সার্বিক রেটিং:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                StarRatingBar(rating = overallStars, onRatingChanged = { overallStars = it })

                Divider(color = KrishiOutline.copy(alpha = 0.3f))

                Text("পণ্যের গুণমান:", fontSize = 12.sp)
                StarRatingBar(rating = qualityStars, onRatingChanged = { qualityStars = it }, starSize = 20)

                Text("ওজন ও পরিমাণের সঠিকতা:", fontSize = 12.sp)
                StarRatingBar(rating = accuracyStars, onRatingChanged = { accuracyStars = it }, starSize = 20)

                Text("যোগাযোগ ও ব্যবহার:", fontSize = 12.sp)
                StarRatingBar(rating = communicationStars, onRatingChanged = { communicationStars = it }, starSize = 20)

                Text("পেমেন্ট ও প্রতিশ্রুতি রক্ষা:", fontSize = 12.sp)
                StarRatingBar(rating = reliabilityStars, onRatingChanged = { reliabilityStars = it }, starSize = 20)

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("আপনার মন্তব্য লিখুন") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = if (comment.isBlank()) "অত্যন্ত সন্তোষজনক অভিজ্ঞতা।" else comment
                    onSubmit(overallStars, qualityStars, accuracyStars, communicationStars, reliabilityStars, c)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary)
            ) {
                Text("রিভিউ জমা দিন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("পরে") }
        }
    )
}

// 6. CHAT DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderChatDialog(
    order: MarketplaceOrder,
    messages: List<ChatMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val orderMessages = remember(order, messages) {
        messages.filter { it.orderId == order.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("চ্যাট: #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${order.farmerName} ↔ ${order.buyerBusinessName}", fontSize = 11.sp, color = KrishiTextSecondary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KrishiSurface)
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp,
                    color = KrishiSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("বার্তা লিখুন...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    onSendMessage(messageText)
                                    messageText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "পাঠান", tint = KrishiGreenPrimary)
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orderMessages) { msg ->
                    val isBuyer = msg.senderRole == UserRole.BUYER
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isBuyer) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isBuyer) KrishiOrangeContainer else KrishiGreenContainer,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBuyer) KrishiOrangeOnContainer else KrishiGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(text = msg.text, fontSize = 13.sp, color = KrishiTextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = msg.time, fontSize = 9.sp, color = KrishiTextTertiary, modifier = Modifier.align(Alignment.End))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. NOTIFICATIONS DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDialog(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("বিজ্ঞপ্তিসমূহ", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KrishiSurface)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (notif.isRead) KrishiSurfaceVariant else KrishiGreenContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(notif.time, fontSize = 11.sp, color = KrishiTextTertiary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notif.message, fontSize = 12.sp, color = KrishiTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// 8. ROLE SWITCHER DIALOG
@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole,
    farmers: List<FarmerProfile>,
    buyers: List<BuyerProfile>,
    currentFarmer: FarmerProfile?,
    currentBuyer: BuyerProfile?,
    onDismiss: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectFarmer: (String) -> Unit,
    onSelectBuyer: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("মোড ও প্রোফাইল নির্বাচন করুন", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("১. আপনার ভূমিকা (Role):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSelectRole(UserRole.FARMER) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == UserRole.FARMER) KrishiGreenPrimary else KrishiSurfaceVariant,
                            contentColor = if (currentRole == UserRole.FARMER) Color.White else KrishiTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("কৃষক", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSelectRole(UserRole.BUYER) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == UserRole.BUYER) KrishiOrange else KrishiSurfaceVariant,
                            contentColor = if (currentRole == UserRole.BUYER) Color.White else KrishiTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ক্রেতা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSelectRole(UserRole.ADMIN) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == UserRole.ADMIN) Color(0xFF1976D2) else KrishiSurfaceVariant,
                            contentColor = if (currentRole == UserRole.ADMIN) Color.White else KrishiTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("অ্যাডমিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentRole == UserRole.FARMER) {
                    Divider(color = KrishiOutline.copy(alpha = 0.3f))
                    Text("২. সক্রিয় কৃষক নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    farmers.take(5).forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFarmer(f.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = currentFarmer?.id == f.id, onClick = { onSelectFarmer(f.id) })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(f.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("📍 ${f.district} • ${f.farmerType}", fontSize = 11.sp, color = KrishiTextSecondary)
                            }
                        }
                    }
                } else if (currentRole == UserRole.BUYER) {
                    Divider(color = KrishiOutline.copy(alpha = 0.3f))
                    Text("২. সক্রিয় ক্রেতা নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    buyers.take(5).forEach { b ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBuyer(b.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = currentBuyer?.id == b.id, onClick = { onSelectBuyer(b.id) })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(b.businessName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("মালিক: ${b.name} • 📍 ${b.district}", fontSize = 11.sp, color = KrishiTextSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary)
            ) {
                Text("সম্পন্ন")
            }
        }
    )
}

// 9. WEIGHT & QUALITY VERIFICATION INPUT DIALOG
@Composable
fun WeightVerificationDialog(
    order: MarketplaceOrder,
    onDismiss: () -> Unit,
    onSubmit: (actualWeight: Double, grade: QualityGrade, notes: String) -> Unit
) {
    var actualWeightText by remember { mutableStateOf(order.quantity.toInt().toString()) }
    var selectedGrade by remember { mutableStateOf(order.qualityGrade) }
    var notes by remember { mutableStateOf("ডিজিটাল স্কেলে সঠিক ওজন ও ফ্রেশ কোয়ালিটি যাচাই সম্পন্ন।") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ওজন ও কোয়ালিটি ইনপুট (কালেকশন পয়েন্ট)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("চুক্তিকৃত পরিমাণ: ${toBnNumber(order.quantity)} ${order.unit.labelBn}", fontSize = 13.sp, color = KrishiTextSecondary)

                OutlinedTextField(
                    value = actualWeightText,
                    onValueChange = { actualWeightText = it },
                    label = { Text("মাপা প্রকৃত ওজন (${order.unit.labelBn})") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("যাচাইকৃত গ্রেড:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(QualityGrade.values()) { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.labelBn) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ইনস্পেকশন নোট") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = actualWeightText.toDoubleOrNull() ?: order.quantity
                    onSubmit(w, selectedGrade, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary)
            ) {
                Text("যাচাই সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
