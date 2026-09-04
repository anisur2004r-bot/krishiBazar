package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

// Bengali Number Conversion
fun toBnNumber(number: Number): String {
    val enDigits = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bnDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val str = if (number is Double) {
        if (number % 1.0 == 0.0) number.toLong().toString() else "%.1f".format(number)
    } else number.toString()

    val sb = StringBuilder()
    for (ch in str) {
        val idx = enDigits.indexOf(ch)
        if (idx != -1) sb.append(bnDigits[idx]) else sb.append(ch)
    }
    return sb.toString()
}

fun formatTaka(amount: Number): String {
    return "৳${toBnNumber(amount.toLong())}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishiTopAppBar(
    currentRole: UserRole,
    unreadNotificationsCount: Int,
    onRoleSwitchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sleek Interface curved top header
    Surface(
        color = KrishiGreenPrimary,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("krishi_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Title / Current User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (currentRole) {
                            UserRole.FARMER -> "👨‍🌾"
                            UserRole.BUYER -> "🏢"
                            UserRole.ADMIN -> "🛡️"
                        },
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (currentRole) {
                            UserRole.FARMER -> "কৃষক ড্যাশবোর্ড"
                            UserRole.BUYER -> "ক্রেতা মার্কেট"
                            UserRole.ADMIN -> "সুপার অ্যাডমিন"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.clickable { onRoleSwitchClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (currentRole) {
                                    UserRole.FARMER -> "ভেরিফাইড কৃষক ✅"
                                    UserRole.BUYER -> "ভেরিফাইড ক্রেতা ✅"
                                    UserRole.ADMIN -> "সিস্টেম অ্যাডমিন ⚡"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "রোল পরিবর্তন",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Right: Role Switch & Notification Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Switcher pill
                Surface(
                    onClick = onRoleSwitchClick,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("role_switcher_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (currentRole) {
                                UserRole.FARMER -> "কৃষক"
                                UserRole.BUYER -> "ক্রেতা"
                                UserRole.ADMIN -> "অ্যাডমিন"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "রোল পরিবর্তন",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Sleek circular notification button with orange dot
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { onNotificationsClick() }
                        .testTag("notifications_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "বিজ্ঞপ্তি",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .clip(CircleShape)
                                .background(KrishiOrangeLight)
                                .border(1.5.dp, KrishiGreenPrimary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerifiedBadge(
    isVerified: Boolean,
    modifier: Modifier = Modifier,
    text: String = if (isVerified) "যাচাইকৃত কৃষক ✅" else "যাচাই প্রক্রিয়াধীন"
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFB300)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isVerified) Color(0xFF2E7D32) else Color(0xFFF57F17),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isVerified) Color(0xFF1B5E20) else Color(0xFFBF360C)
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.PENDING, OrderStatus.PAYMENT_PENDING -> Triple(Color(0xFFFFF3E0), KrishiOrange, Icons.Default.Schedule)
        OrderStatus.PAYMENT_CONFIRMED, OrderStatus.CONFIRMED -> Triple(Color(0xFFE8F5E9), KrishiGreenPrimary, Icons.Default.CheckCircle)
        OrderStatus.PREPARING -> Triple(Color(0xFFF1F8E9), Color(0xFF558B2F), Icons.Default.Agriculture)
        OrderStatus.COLLECTED -> Triple(Color(0xFFE0F2F1), Color(0xFF00695C), Icons.Default.Inventory)
        OrderStatus.IN_TRANSIT -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.LocalShipping)
        OrderStatus.DELIVERED -> Triple(Color(0xFFEDE7F6), Color(0xFF4527A0), Icons.Default.DoneAll)
        OrderStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.Verified)
        OrderStatus.CANCELLED -> Triple(Color(0xFFFFEBEE), KrishiError, Icons.Default.Cancel)
        OrderStatus.DISPUTED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Warning)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.labelBn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = Color.White,
    accentColor: Color = KrishiGreenPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, KrishiOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = KrishiTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (icon != null) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = KrishiTextTertiary
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    maxStars: Int = 5,
    starSize: Int = 24
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            IconButton(
                onClick = { onRatingChanged?.invoke(i) },
                enabled = onRatingChanged != null,
                modifier = Modifier.size(starSize.dp)
            ) {
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i star",
                    tint = if (isFilled) Color(0xFFFFB300) else Color(0xFFBDBDBD),
                    modifier = Modifier.size((starSize - 4).dp)
                )
            }
        }
    }
}

@Composable
fun LargeActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = KrishiGreenSecondary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("action_button_${text.take(8)}")
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Sleek Interface Bottom Navigation Bar
@Composable
fun <T> SleekBottomNavigationBar(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    iconProvider: (T) -> ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, KrishiOutline),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item == selectedItem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onItemSelected(item) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (selected) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = KrishiGreenContainer,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconProvider(item),
                                    contentDescription = labelProvider(item),
                                    tint = KrishiGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = labelProvider(item),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiGreenPrimary
                        )
                    } else {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconProvider(item),
                                contentDescription = labelProvider(item),
                                tint = KrishiTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = labelProvider(item),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// Sleek Interface Card for Buyer Demands
@Composable
fun SleekDemandCard(
    demand: BuyerDemand,
    onOfferClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUrgent: Boolean = false
) {
    val accentBorderColor = if (isUrgent || demand.status == DemandStatus.ACTIVE) KrishiOrange else KrishiGreenSecondary

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, KrishiOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left border accent (border-l-4)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentBorderColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${demand.productTitle} দরকার - ${toBnNumber(demand.requiredQuantity)} ${demand.unit.labelBn}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍 ", fontSize = 12.sp)
                            Text(
                                text = demand.requiredLocation,
                                fontSize = 13.sp,
                                color = KrishiTextSecondary
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KrishiOrangeContainer
                    ) {
                        Text(
                            text = if (isUrgent) "জরুরী" else "সক্রিয়",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Divider(color = KrishiOutlineVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বাজেট",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextTertiary
                        )
                        Text(
                            text = "৳${toBnNumber(demand.minExpectedPrice.toInt())}-${toBnNumber(demand.maxExpectedPrice.toInt())}/${demand.unit.labelBn}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrishiTextPrimary
                        )
                    }

                    Button(
                        onClick = onOfferClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KrishiGreenSecondary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("অফার দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
