//
//  CortisolApp.swift
//  Cortisol
//
//  Created by Hannes Vandecasteele on 4/30/25.
//

import SwiftUI

@main
struct CortisolApp: App {
    @AppStorage("hasCompletedSetup") var hasCompletedSetup: Bool = false

    var body: some Scene {
        WindowGroup {
            if hasCompletedSetup {
                MainView()
            } else {
                SetupView()
            }
        }
    }
}
