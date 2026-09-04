package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.components.KrishiTopAppBar
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.buyer.BuyerMainScreen
import com.example.ui.screens.buyer.BuyerOfferManagementDialog
import com.example.ui.screens.dialogs.*
import com.example.ui.screens.farmer.FarmerMainScreen
import com.example.ui.screens.orders.OrderDetailDialog
import com.example.ui.theme.KrishiBackground
import com.example.ui.theme.KrishiTextPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KrishiViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KrishiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KrishiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KrishiApp(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val farmers by viewModel.farmers.collectAsState()
    val buyers by viewModel.buyers.collectAsState()
    val currentFarmer by viewModel.currentFarmer.collectAsState()
    val currentBuyer by viewModel.currentBuyer.collectAsState()
    val offers by viewModel.offers.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = KrishiBackground,
        contentColor = KrishiTextPrimary,
        topBar = {
            KrishiTopAppBar(
                currentRole = uiState.currentRole,
                unreadNotificationsCount = notifications.count { !it.isRead },
                onRoleSwitchClick = { viewModel.openRoleSwitcher() },
                onNotificationsClick = { viewModel.openNotifications() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentRole) {
                UserRole.FARMER -> FarmerMainScreen(viewModel = viewModel)
                UserRole.BUYER -> BuyerMainScreen(viewModel = viewModel)
                UserRole.ADMIN -> AdminMainScreen(viewModel = viewModel)
            }
        }
    }

    // --- Active Dialogs & Modals ---

    // 1. Add Product (Farmer)
    if (uiState.showAddProductDialog) {
        AddProductDialog(
            onDismiss = { viewModel.closeAddProductDialog() },
            onSubmit = { title, cat, qty, unit, price, minP, loc, availDate, harvDate, grade, desc ->
                viewModel.submitProduct(title, cat, qty, unit, price, minP, loc, availDate, harvDate, grade, desc)
            }
        )
    }

    // 2. Add Demand (Buyer)
    if (uiState.showAddDemandDialog) {
        AddBuyerDemandDialog(
            onDismiss = { viewModel.closeAddDemandDialog() },
            onSubmit = { title, cat, qty, unit, loc, reqDate, minP, maxP, grade, note ->
                viewModel.submitDemand(title, cat, qty, unit, loc, reqDate, minP, maxP, grade, note)
            }
        )
    }

    // 3. Send Offer on Demand (Farmer)
    uiState.activeDemandForOffer?.let { demand ->
        FarmerSendOfferDialog(
            demand = demand,
            onDismiss = { viewModel.closeOfferDialog() },
            onSubmit = { qty, unit, price, grade, date, note ->
                viewModel.submitOffer(demand.id, qty, unit, price, grade, date, note)
            }
        )
    }

    // 4. View and Accept Offers on Demand (Buyer)
    uiState.activeDemandForOfferManagement?.let { demand ->
        BuyerOfferManagementDialog(
            demand = demand,
            allOffers = offers,
            onDismiss = { viewModel.closeDemandOfferManagement() },
            onAcceptOffer = { offerId -> viewModel.acceptOffer(offerId) }
        )
    }

    // 5. Order Detail (Farmer or Buyer)
    uiState.activeOrderForDetail?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { viewModel.closeOrderDetail() },
            onPayDeposit = { viewModel.payDeposit(order.id) },
            onAdvanceTransport = { nextStatus -> viewModel.advanceTransport(order.id, nextStatus) },
            onOpenVerification = { viewModel.openVerificationDialog(order) },
            onOpenDispute = { viewModel.openDisputeDialog(order) },
            onOpenRating = { viewModel.openRatingDialog(order) },
            onOpenChat = { viewModel.openChat(order) },
            onUpdateStatus = { newStatus -> viewModel.updateOrderStatus(order.id, newStatus) }
        )
    }

    // 6. Direct Order Chat
    uiState.activeOrderForChat?.let { order ->
        OrderChatDialog(
            order = order,
            messages = chatMessages,
            onDismiss = { viewModel.closeChat() },
            onSendMessage = { text -> viewModel.sendChatMessage(order.id, text) }
        )
    }

    // 7. Dispute / Problem Report
    uiState.activeOrderForDispute?.let { order ->
        DisputeReportDialog(
            order = order,
            onDismiss = { viewModel.closeDisputeDialog() },
            onSubmit = { type, desc -> viewModel.submitDispute(order.id, type, desc) }
        )
    }

    // 8. Rating & Review
    uiState.activeOrderForRating?.let { order ->
        RatingReviewDialog(
            order = order,
            onDismiss = { viewModel.closeRatingDialog() },
            onSubmit = { stars, quality, accuracy, comm, rel, comment ->
                viewModel.submitRating(order.id, stars, quality, accuracy, comm, rel, comment)
            }
        )
    }

    // 9. Weight & Quality Verification (Collection stage)
    uiState.activeOrderForVerification?.let { order ->
        WeightVerificationDialog(
            order = order,
            onDismiss = { viewModel.closeVerificationDialog() },
            onSubmit = { actualWeight, grade, notes ->
                viewModel.submitWeightVerification(order.id, actualWeight, grade, notes)
            }
        )
    }

    // 10. System Notifications
    if (uiState.showNotificationsSheet) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { viewModel.closeNotifications() }
        )
    }

    // 11. Role Switcher
    if (uiState.showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentRole = uiState.currentRole,
            farmers = farmers,
            buyers = buyers,
            currentFarmer = currentFarmer,
            currentBuyer = currentBuyer,
            onDismiss = { viewModel.closeRoleSwitcher() },
            onSelectRole = { viewModel.switchRole(it) },
            onSelectFarmer = { viewModel.selectFarmerProfile(it) },
            onSelectBuyer = { viewModel.selectBuyerProfile(it) }
        )
    }
}

