import SwiftUI
import StoreKit
import ComposeApp

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        KoinStarter.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {

    var purchaseIntentObserver: PurchaseIntentObserver?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: UIApplication.LaunchOptionsKey? = nil
    ) -> Bool {

        SKPaymentQueue.default().add(IOSPromotionBridge.shared)

        purchaseIntentObserver = PurchaseIntentObserver()

        print("⚡ PurchaseIntentObserver initialized")
        return true
    }
}
