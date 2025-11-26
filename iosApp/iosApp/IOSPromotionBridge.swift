import Foundation
import StoreKit
import ComposeApp

@objc(IOSPromotionBridge)
@objcMembers
public class IOSPromotionBridge: NSObject, SKPaymentTransactionObserver {

    public static let shared = IOSPromotionBridge()

    private override init() {
        super.init()
    }

    public func paymentQueue(_ queue: SKPaymentQueue,
                             shouldAddStorePayment payment: SKPayment,
                             for product: SKProduct) -> Bool {

        let id = product.productIdentifier
        print("🍏 Swift Promotion: promoted IAP tapped → \(id)")

        PromotionConnector.shared.triggerPromotion(productId: id)

        return true
    }

    public func paymentQueue(_ queue: SKPaymentQueue,
                             updatedTransactions transactions: [SKPaymentTransaction]) {
    }

    public func paymentQueueRestoreCompletedTransactionsFinished(_ queue: SKPaymentQueue) {
    }

    public func paymentQueue(_ queue: SKPaymentQueue,
                             restoreCompletedTransactionsFailedWithError error: Error) {
    }
}
