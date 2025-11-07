package org.app.corge.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.app.corge.data.model.Theme
import org.app.corge.data.repository.ThemeRepository
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.darwin.NSObject

class IOSBillingRepository(
    private val themeRepository: ThemeRepository
) : BillingRepository {

    private val delegate = BillingDelegate(themeRepository)

    override suspend fun getThemes(): List<Theme> =
        themeRepository.getAllThemes()

    override suspend fun purchaseTheme(themeId: String): PurchaseResult =
        delegate.purchaseTheme(themeId)

    override suspend fun restorePurchases(): PurchaseResult =
        delegate.restorePurchases()
}

private class BillingDelegate(
    private val themeRepository: ThemeRepository
) : NSObject(),
    SKProductsRequestDelegateProtocol,
    SKPaymentTransactionObserverProtocol {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val themeProducts = setOf("wabi", "kintsugi")
    private val products: MutableMap<String, SKProduct> = mutableMapOf()
    private val purchaseContinuations = mutableMapOf<String, (PurchaseResult) -> Unit>()

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
        fetchProducts()
    }

    private fun fetchProducts() {
        val request = SKProductsRequest(productIdentifiers = themeProducts)
        request.delegate = this
        request.start()
    }

    suspend fun purchaseTheme(themeId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            if (themeId == "light") {
                continuation.resume(PurchaseResult.Success) {}
                return@suspendCancellableCoroutine
            }

            val product = products[themeId]
            if (product == null) {
                continuation.resume(PurchaseResult.Error("Product not found: $themeId")) {}
                return@suspendCancellableCoroutine
            }

            purchaseContinuations[themeId] = { result -> continuation.resume(result) {} }
            val payment = SKPayment.paymentWithProduct(product)
            SKPaymentQueue.defaultQueue().addPayment(payment)
        }

    suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            purchaseContinuations["restore"] = { result ->
                continuation.resume(result) {}
            }
            SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
        }

    override fun productsRequest(
        request: SKProductsRequest,
        didReceiveResponse: SKProductsResponse
    ) {
        (didReceiveResponse.products ?: emptyList<Any>()).forEach { any ->
            val product = any as? SKProduct ?: return@forEach
            val id = product.productIdentifier ?: return@forEach
            if (id in themeProducts) {
                products[id] = product
            }
        }
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        updatedTransactions: List<*>
    ) {
        updatedTransactions.forEach { any ->
            val tx = any as? SKPaymentTransaction ?: return@forEach
            when (tx.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> handlePurchased(tx)
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> handleFailed(tx)
                else -> {}
            }
        }
    }

    private fun handlePurchased(transaction: SKPaymentTransaction) {
        val themeId = transaction.payment.productIdentifier ?: run {
            SKPaymentQueue.defaultQueue().finishTransaction(transaction)
            return
        }
        val callback = purchaseContinuations.remove(themeId) ?: purchaseContinuations.remove("restore")

        scope.launch {
            try {
                themeRepository.markThemePurchased(themeId)
                themeRepository.setCurrentTheme(themeId)
                callback?.invoke(PurchaseResult.Success)
            } catch (t: Throwable) {
                callback?.invoke(PurchaseResult.Error(t.message ?: "Unknown error"))
            } finally {
                SKPaymentQueue.defaultQueue().finishTransaction(transaction)
            }
        }
    }

    private fun handleFailed(transaction: SKPaymentTransaction) {
        val themeId = transaction.payment.productIdentifier
        val cb = if (themeId != null) purchaseContinuations.remove(themeId)
        else purchaseContinuations.remove("restore")

        cb?.invoke(PurchaseResult.Failure)
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }

    fun dispose() {
        SKPaymentQueue.defaultQueue().removeTransactionObserver(this)
        purchaseContinuations.clear()
        products.clear()
        scope.cancel()
    }
}