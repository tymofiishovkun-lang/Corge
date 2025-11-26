import Foundation
import StoreKit
import ComposeApp

class PurchaseIntentObserver: NSObject {

    private var task: Task<Void, Never>? = nil

    override init() {
        super.init()
        startListening()
    }

    func startListening() {
        if #available(iOS 16.4, *) {
            task = Task {
                for await intent in PurchaseIntent.intents {
                    let productId = intent.product.id

                    print(" PurchaseIntent received → \(productId)")

                    PromotionConnector.shared.triggerPromotion(productId: productId)
                }
            }
        } else {
            print(" PurchaseIntent API is not available on iOS < 16.4")
        }
    }

    deinit {
        task?.cancel()
    }
}