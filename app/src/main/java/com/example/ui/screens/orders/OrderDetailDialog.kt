package com.example.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.KrishiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailDialog(
    order: MarketplaceOrder,
    onDismiss: () -> Unit,
    onPayDeposit: () -> Unit,
    onAdvanceTransport: (TransportStatus) -> Unit,
    onOpenVerification: () -> Unit,
    onOpenDispute: () -> Unit,
    onOpenRating: () -> Unit,
    onOpenChat: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit
) {
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
                                text = "অর্ডার #${order.orderNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = KrishiGreenPrimary
                            )
                            Text(
                                text = "তারিখ: ${order.createdAt}",
                                fontSize = 11.sp,
                                color = KrishiTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenChat) {
                            Icon(Icons.Default.Chat, contentDescription = "চ্যাট", tint = KrishiGreenPrimary)
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
                // Status Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiGreenContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("অর্ডার বর্তমান অবস্থা:", fontSize = 13.sp, color = KrishiTextSecondary)
                                OrderStatusBadge(status = order.orderStatus)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${order.productTitle} — ${toBnNumber(order.quantity)} ${order.unit.labelBn}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = KrishiGreenPrimary
                            )
                            Text(
                                text = "একক দর: ৳${toBnNumber(order.unitPrice.toInt())}/${order.unit.labelBn} • মান গ্রেড: ${order.qualityGrade.labelBn}",
                                fontSize = 13.sp,
                                color = KrishiTextSecondary
                            )
                        }
                    }
                }

                // Farmer & Buyer Parties Information Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("অর্ডারের পক্ষসমূহ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Divider(color = KrishiOutline.copy(alpha = 0.3f))

                            // Farmer info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = KrishiGreenContainer, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("👨‍🌾", fontSize = 18.sp) }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.farmerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("কৃষক • 📍 ${order.farmerLocation}", fontSize = 12.sp, color = KrishiTextSecondary)
                                }
                                Text(order.farmerPhone, fontSize = 12.sp, color = KrishiGreenPrimary, fontWeight = FontWeight.Medium)
                            }

                            Divider(color = KrishiOutline.copy(alpha = 0.2f))

                            // Buyer info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = KrishiOrangeContainer, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("🏢", fontSize = 18.sp) }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.buyerBusinessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("ক্রেতা (${order.buyerName}) • 📍 ${order.deliveryLocation}", fontSize = 12.sp, color = KrishiTextSecondary)
                                }
                                Text(order.buyerPhone, fontSize = 12.sp, color = KrishiOrange, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Payment & Deposit Escrow Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("পেমেন্ট ও ডিপোজিট এসক্রো 💰", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (order.isDepositPaid) KrishiGreenContainer else KrishiOrangeContainer
                                ) {
                                    Text(
                                        text = if (order.isDepositPaid) "ডিপোজিট পেইড ✅" else "ডিপোজিট আবশ্যক ⏳",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.isDepositPaid) KrishiGreenPrimary else KrishiOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Divider(color = KrishiOutline.copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("মোট চুক্তি মূল্য:", fontSize = 13.sp, color = KrishiTextSecondary)
                                Text(formatTaka(order.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("প্রয়োজনীয় ২০% ডিপোজিট:", fontSize = 13.sp, color = KrishiTextSecondary)
                                Text(formatTaka(order.depositAmount), fontWeight = FontWeight.Bold, color = KrishiOrange, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("বাকি ৮০% (ডেলিভারির পর):", fontSize = 13.sp, color = KrishiTextSecondary)
                                Text(formatTaka(order.remainingAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            if (!order.isDepositPaid) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = onPayDeposit,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KrishiOrange),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("button_pay_deposit")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("এখনই ডিপোজিট পে করুন (${formatTaka(order.depositAmount)})", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🔒 ডিপোজিট মানি নিরাপদ এসক্রো অ্যাকাউন্টে সংরক্ষিত আছে। পণ্য প্রাপ্তি নিশ্চিত হলে কৃষক পাবেন।",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Transport & Logistic Tracking Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("পরিবহন ও ট্রাক ট্র্যাকিং 🚚", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE3F2FD)) {
                                    Text(
                                        text = order.transportStatus.labelBn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Divider(color = KrishiOutline.copy(alpha = 0.3f))

                            order.transportInfo?.let { transport ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ড্রাইভারের নাম:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text("${transport.driverName} (${transport.driverPhone})", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("গাড়ির নম্বর:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text(transport.vehicleNumber, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("সংগ্রহ পয়েন্ট:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text(transport.pickupLocation, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("গন্তব্য:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text(transport.deliveryLocation, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("আনুমানিক সময়:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text(transport.estimatedArrival, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }

                            // Advance Transport Buttons for Prototype Testing
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("পরিবহন স্ট্যাটাস পরিবর্তন করুন (টেস্টিং):", fontSize = 12.sp, color = KrishiTextTertiary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onAdvanceTransport(TransportStatus.PICKED_UP) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("পিকআপ", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { onAdvanceTransport(TransportStatus.IN_TRANSIT) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("ট্রানজিট", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAdvanceTransport(TransportStatus.DELIVERED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("ডেলিভার্ড", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Quality & Weight Verification (Collection Stage)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KrishiSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ওজন ও গুণমান যাচাই (কালেকশন পয়েন্ট) ⚖️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (order.qualityVerification != null) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = if (order.qualityVerification != null) "যাচাই সম্পন্ন ✅" else "যাচাই বাকি ⏳",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.qualityVerification != null) KrishiGreenPrimary else KrishiOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Divider(color = KrishiOutline.copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("চুক্তিকৃত পরিমাণ:", fontSize = 13.sp, color = KrishiTextSecondary)
                                Text("${toBnNumber(order.quantity)} ${order.unit.labelBn}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }

                            val v = order.qualityVerification
                            if (v != null) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("প্রকৃত মাপা ওজন:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text("${toBnNumber(v.actualWeight)} ${order.unit.labelBn}", fontWeight = FontWeight.Bold, color = KrishiGreenPrimary, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("যাচাইকৃত গ্রেড:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text(v.actualGrade.labelBn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("যাচাইকারী এজেন্ট:", fontSize = 13.sp, color = KrishiTextSecondary)
                                    Text("${v.verifiedBy} (${v.verifiedAt})", fontSize = 12.sp)
                                }
                                Text("মন্তব্য: \"${v.notes}\"", fontSize = 12.sp, color = KrishiTextTertiary)
                            } else {
                                Text("কালেকশন পয়েন্টে ডিজিটাল স্কেলে ওজন ও গুণমান নিশ্চিত করা হবে।", fontSize = 12.sp, color = KrishiTextSecondary)
                                OutlinedButton(
                                    onClick = onOpenVerification,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ওজন ও কোয়ালিটি ইনপুট দিন")
                                }
                            }
                        }
                    }
                }

                // Final Order Delivery & Completion
                if (order.transportStatus == TransportStatus.DELIVERED && order.orderStatus != OrderStatus.COMPLETED) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = KrishiGreenContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "পণ্য পৌঁছে গেছে! অর্ডার সম্পন্ন করুন",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KrishiGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "পণ্য চেক করে অর্ডার সম্পন্ন করুন। কৃষক তার সম্পূর্ণ পেমেন্ট পেয়ে যাবেন।",
                                    fontSize = 12.sp,
                                    color = KrishiTextSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KrishiGreenPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("পণ্য বুঝে পেয়েছি ও অর্ডার সম্পন্ন করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Action Buttons Row: Dispute (সমস্যা রিপোর্ট) & Rating (রেটিং দিন) & Chat
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenDispute,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KrishiError),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সমস্যা রিপোর্ট", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onOpenRating,
                            colors = ButtonDefaults.buttonColors(containerColor = KrishiOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("রেটিং দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
