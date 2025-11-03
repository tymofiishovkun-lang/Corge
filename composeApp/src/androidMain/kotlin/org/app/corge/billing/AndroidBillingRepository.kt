package org.app.corge.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.app.corge.CurrentActivityHolder
import org.app.corge.data.model.Theme
import org.app.corge.data.repository.ThemeRepository

class AndroidBillingRepository(
    private val context: Context,
    private val themeRepository: ThemeRepository
) : BillingRepository {

    @Volatile private var isPurchaseInProgress = false
    private var purchaseContinuation: CancellableContinuation<PurchaseResult>? = null
    private var isBillingReady = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) handlePurchases(purchases)
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    purchaseContinuation?.resume(PurchaseResult.Failure) {}
                    isPurchaseInProgress = false
                    purchaseContinuation = null
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    scope.launch { restorePurchases() }
                    purchaseContinuation?.resume(PurchaseResult.Success) {}
                    isPurchaseInProgress = false
                    purchaseContinuation = null
                }
                else -> {
                    purchaseContinuation?.resume(
                        PurchaseResult.Error("Billing error: ${billingResult.debugMessage}")
                    ) {}
                    isPurchaseInProgress = false
                    purchaseContinuation = null
                }
            }
        }
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isBillingReady = false
                Log.w("Billing", "Service Google Play Billing disconnected ⚠️")
                Handler(Looper.getMainLooper()).postDelayed({
                    billingClient.startConnection(this)
                }, 2000)
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
                    Log.d("Billing", "BillingClient ready ✅")
                    restorePurchasesSilently()
                } else {
                    isBillingReady = false
                    Log.e("Billing", "Connection error: ${result.debugMessage}")
                }
            }
        })
    }

    override suspend fun getThemes(): List<Theme> = themeRepository.getAllThemes()

    override suspend fun purchaseTheme(themeId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            if (!isBillingReady) {
                continuation.resume(PurchaseResult.Error("Billing not ready")) {}; return@suspendCancellableCoroutine
            }
            if (isPurchaseInProgress) {
                continuation.resume(PurchaseResult.Error("Purchase already in progress")) {}; return@suspendCancellableCoroutine
            }

            scope.launch {
                val productDetails = queryProduct(themeId)
                if (productDetails == null) {
                    continuation.resume(PurchaseResult.Error("Product not found")) {}; return@launch
                }

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    ).build()

                val activity = CurrentActivityHolder.current()
                if (activity == null) {
                    continuation.resume(PurchaseResult.Error("No activity in foreground")) {}; return@launch
                }

                isPurchaseInProgress = true
                purchaseContinuation = continuation

                val br = billingClient.launchBillingFlow(activity, flowParams)
                if (br.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    restorePurchases()
                    continuation.resume(PurchaseResult.Success) {}
                    isPurchaseInProgress = false
                    purchaseContinuation = null
                }
            }

            continuation.invokeOnCancellation {
                isPurchaseInProgress = false
                purchaseContinuation = null
            }
        }

    override suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            if (!isBillingReady) {
                continuation.resume(PurchaseResult.Error("Billing not ready")) {}; return@suspendCancellableCoroutine
            }

            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
                    handlePurchases(purchases)
                    continuation.resume(PurchaseResult.Success) {}
                } else {
                    continuation.resume(PurchaseResult.Failure) {}
                }
            }
        }

    private suspend fun queryProduct(productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()
        return billingClient.queryProductDetails(params).productDetailsList?.firstOrNull()
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> {
                    purchaseContinuation?.resume(PurchaseResult.Failure) {}
                    isPurchaseInProgress = false
                    purchaseContinuation = null
                }
                Purchase.PurchaseState.PURCHASED -> {
                    if (!purchase.isAcknowledged) {
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(params) { br ->
                            if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                                finalizeThemeGrant(purchase)
                            } else {
                                Log.e("Billing", "Ack failed: ${br.debugMessage}")
                                purchaseContinuation?.resume(PurchaseResult.Error("Acknowledge failed")) {}
                                isPurchaseInProgress = false
                                purchaseContinuation = null
                            }
                        }
                    } else {
                        finalizeThemeGrant(purchase)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun finalizeThemeGrant(purchase: Purchase) {
        val themeId = purchase.products.firstOrNull() ?: return
        scope.launch {
            themeRepository.markThemePurchased(themeId)
            themeRepository.setCurrentTheme(themeId)
        }
        purchaseContinuation?.resume(PurchaseResult.Success) {}
        isPurchaseInProgress = false
        purchaseContinuation = null
    }

    private fun restorePurchasesSilently() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchases ->
            if (purchases.isNotEmpty()) {
                handlePurchases(purchases)
            }
        }
    }
}