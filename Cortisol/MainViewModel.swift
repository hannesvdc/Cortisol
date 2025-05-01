//
//  MainViewModel.swift
//  Cortisol
//
//  Created by Hannes Vandecasteele on 5/1/25.
//

import Foundation
import Combine
import UserNotifications
import SwiftUI

class MainViewModel: ObservableObject {
    @Published var fourHourText: String = "04:00:00"
    @Published var eightHourText: String = "08:00:00"
    @Published var isWakeButtonEnabled: Bool = true
    @Published var shouldReset: Bool = false
    @Published var showAlert: Bool = false
    @Published var alertMessage: String = ""

    private var timer: Timer?
    private let fourHours: TimeInterval = 4 * 60 * 60
    private let eightHours: TimeInterval = 8 * 60 * 60
    private let alarmStartTimeKey = "alarm_start_time"

    init() {
        checkExistingAlarms()
        requestNotificationPermission()
    }

    func resetView() {
        timer?.invalidate()
        isWakeButtonEnabled = true
        fourHourText = "04:00:00"
        eightHourText = "08:00:00"
        UserDefaults.standard.removeObject(forKey: alarmStartTimeKey)

        shouldReset = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.shouldReset = false
        }
    }

    func startAlarms() {
        let startTime = Date()
        UserDefaults.standard.set(startTime.timeIntervalSince1970, forKey: alarmStartTimeKey)
        isWakeButtonEnabled = false

        scheduleNotifications()
        startTimer()
    }

    private func startTimer() {
        timer?.invalidate()

        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            guard let alarmStart = UserDefaults.standard.value(forKey: self.alarmStartTimeKey) as? Double else { return }

            let elapsed = Date().timeIntervalSince1970 - alarmStart
            let remaining = max(self.eightHours - elapsed, 0)

            if remaining <= 0 {
                self.triggerReset("8 hour timer ended. Please take 2.5mg Hydrocortisol.")
                return
            }

            let hours = Int(remaining) / 3600
            let minutes = (Int(remaining) % 3600) / 60
            let seconds = Int(remaining) % 60

            if elapsed < self.fourHours {
                self.fourHourText = String(format: "%02d:%02d:%02d", 4 - hours, 59 - minutes, 59 - seconds)
            } else {
                self.fourHourText = "Alarm has Passed"
            }

            self.eightHourText = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private func triggerReset(_ message: String) {
        resetView()
        showAlert = true
        alertMessage = message
        vibrate()
    }

    private func checkExistingAlarms() {
        if let alarmStart = UserDefaults.standard.value(forKey: alarmStartTimeKey) as? Double {
            let elapsed = Date().timeIntervalSince1970 - alarmStart
            if elapsed >= eightHours {
                resetView()
            } else {
                isWakeButtonEnabled = false
                startTimer()
            }
        }
    }

    private func vibrate() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.warning)
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    private func scheduleNotifications() {
        let content4h = UNMutableNotificationContent()
        content4h.title = "Cortisol Alert"
        content4h.body = "Please take 5 mg Hydrocortisol"
        content4h.sound = UNNotificationSound.default

        let trigger4h = UNTimeIntervalNotificationTrigger(timeInterval: fourHours, repeats: false)
        let request4h = UNNotificationRequest(identifier: "fourHourAlarm", content: content4h, trigger: trigger4h)

        let content8h = UNMutableNotificationContent()
        content8h.title = "Cortisol Alert"
        content8h.body = "Please take 2.5 mg Hydrocortisol"
        content8h.sound = UNNotificationSound.default

        let trigger8h = UNTimeIntervalNotificationTrigger(timeInterval: eightHours, repeats: false)
        let request8h = UNNotificationRequest(identifier: "eightHourAlarm", content: content8h, trigger: trigger8h)

        UNUserNotificationCenter.current().add(request4h)
        UNUserNotificationCenter.current().add(request8h)
    }
}
