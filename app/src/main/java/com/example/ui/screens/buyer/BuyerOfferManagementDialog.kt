package com.example.ui.screens.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOfferManagementDialog(
    demand: BuyerDemand,
    allOffers: List<FarmerOffer>,
    onDismiss: () -> Unit,
    onAcceptOffer: (String) -> Unit
) {
    val demandOffers = remember(demand, allOffers) {
        allOffers.filter { it.demandId == demand.id }
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
                            Text(
                                text = "কৃষকদের প্রাপ্ত অফারসমূহ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = KrishiGreenPrimary
                            )
                            Text(
                                text = "${demand.productTitle} (${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn})",
                                fontSize = 12.sp,
                                color = KrishiTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KrishiSurface)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                // Demand Summary Banner
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiGreenContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(demand.category.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = demand.productTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KrishiGreenPrimary
                                    )
                                    Text(
                                        text = "মোট প্রয়োজন: ${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn} • কাঙ্ক্ষিত দর: ৳${toBnNumber(demand.minExpectedPrice.toInt())}–${toBnNumber(demand.maxExpectedPrice.toInt())}/${demand.unit.labelBn}",
                                        fontSize = 12.sp,
                                        color = KrishiTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "কৃষকদের পাঠানো প্রস্তাব (${toBnNumber(demandOffers.size)} টি)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                }

                if (demandOffers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("এই চাহিদার জন্য এখনও কোনো অফার জমা পড়েনি।", color = KrishiTextSecondary)
                        }
                    }
                } else {
                    items(demandOffers) { offer ->
                        FarmerOfferCompareCard(
                            offer = offer,
                            onAccept = { onAcceptOffer(offer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerOfferCompareCard(
    offer: FarmerOffer,
    onAccept: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offer_card_${offer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = KrishiGreenContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍🌾", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = offer.farmerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KrishiTextPrimary
                        )
                        Text(
                            text = "📍 ${offer.farmerLocation}",
                            fontSize = 12.sp,
                            color = KrishiTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (offer.status) {
                        OfferStatus.ACCEPTED -> KrishiGreenContainer
                        OfferStatus.REJECTED -> Color(0xFFFFEBEE)
                        else -> KrishiOrangeContainer
                    }
                ) {
                    Text(
                        text = offer.status.labelBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (offer.status) {
                            OfferStatus.ACCEPTED -> KrishiGreenPrimary
                            OfferStatus.REJECTED -> KrishiError
                            else -> KrishiOrange
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = KrishiOutline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("প্রস্তাবিত পরিমাণ", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = "${toBnNumber(offer.offeredQuantity)} ${offer.unit.labelBn}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiGreenPrimary
                    )
                }
                Column {
                    Text("প্রতি ইউনিটের দর", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = "৳${toBnNumber(offer.pricePerUnit.toInt())}/${offer.unit.labelBn}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiOrange
                    )
                }
                Column {
                    Text("মোট প্রস্তাবিত মূল্য", fontSize = 11.sp, color = KrishiTextTertiary)
                    Text(
                        text = formatTaka(offer.offeredQuantity * offer.pricePerUnit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "মান: ${offer.qualityGrade.labelBn} • সরবরাহের সম্ভাব্য তারিখ: ${offer.availableDate}",
                fontSize = 12.sp,
                color = KrishiTextSecondary
            )

            if (offer.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নোট: \"${offer.note}\"",
                    fontSize = 12.sp,
                    color = KrishiTextTertiary
                )
            }

            if (offer.status == OfferStatus.PENDING) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KrishiGreenPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accept_offer_btn_${offer.id}")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অফার গ্রহণ ও অর্ডার কনফার্ম করুন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
