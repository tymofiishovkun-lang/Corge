import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
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
