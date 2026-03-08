package com.merkost.honq.presentation.screens.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallBottomSheet(
    premiumManager: PremiumManager,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    val colors = HonqTheme.colors
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isPurchasing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HonqSpacing.md),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textMuted.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HonqSpacing.lg, vertical = HonqSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unlock Honq Pro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(HonqSpacing.xs))

            Text(
                text = "Get the most out of your test preparation",
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            val features = listOf(
                "Unlimited mock tests",
                "Practice by category",
                "Smart Practice (spaced repetition)",
                "Detailed statistics & analytics",
                "Weakest questions review",
                "Track unanswered questions"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(HonqSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.correct,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            HonqButton(
                text = if (isPurchasing) "" else "Unlock for $4.99",
                onClick = {
                    isPurchasing = true
                    error = null
                    scope.launch {
                        premiumManager.purchasePro()
                            .onSuccess { purchased ->
                                isPurchasing = false
                                if (purchased) {
                                    onPurchaseSuccess()
                                }
                            }
                            .onFailure { e ->
                                isPurchasing = false
                                error = e.message ?: "Purchase failed"
                            }
                    }
                },
                enabled = !isPurchasing,
                loading = isPurchasing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(HonqSpacing.xs))

            Text(
                text = "One-time purchase \u00B7 No subscription",
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(HonqSpacing.md))

            Text(
                text = "Restore Purchase",
                fontSize = 14.sp,
                color = colors.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable {
                        isPurchasing = true
                        error = null
                        scope.launch {
                            premiumManager.restorePurchase()
                                .onSuccess { restored ->
                                    isPurchasing = false
                                    if (restored) {
                                        onPurchaseSuccess()
                                    } else {
                                        error = "No previous purchase found"
                                    }
                                }
                                .onFailure { e ->
                                    isPurchasing = false
                                    error = e.message ?: "Restore failed"
                                }
                        }
                    }
                    .padding(HonqSpacing.sm)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(HonqSpacing.sm))
                Text(
                    text = error!!,
                    fontSize = 12.sp,
                    color = colors.incorrect,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.lg))
        }
    }
}
