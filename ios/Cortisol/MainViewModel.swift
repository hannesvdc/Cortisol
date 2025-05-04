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

class MainViewModel: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    @Published var fourHourText: String = "4:00:00"
    @Published var eightHourText: String = "8:00:00"
    @Published var isWakeButtonEnabled: Bool = true

    private var fourHourTimer: Timer?
    private var eightHourTimer: Timer?
    
    private let fourHours: TimeInterval = 4 * 60 * 60
    private let eightHours: TimeInterval = 8 * 60 * 60
    
    private let alarmStartTimeKey = "alarm_start_time"

    override init() {
        super.init()

        checkExistingAlarms()
        requestNotificationPermission()
        
        UNUserNotificationCenter.current().delegate = self
    }

    func resetView() {
        fourHourTimer?.invalidate()
        eightHourTimer?.invalidate()

        isWakeButtonEnabled = true
        fourHourText = "4:00:00"
        eightHourText = "8:00:00"

        UserDefaults.standard.removeObject(forKey: alarmStartTimeKey)
    }

    func startAlarms() {
        let startTime = Date()
        UserDefaults.standard.set(startTime.timeIntervalSince1970, forKey: alarmStartTimeKey)
        isWakeButtonEnabled = false

        startFourHourTimer()
        startEightHourTimer()
    }

    private func startFourHourTimer() {
        fourHourTimer?.invalidate()

        guard let alarmStart = UserDefaults.standard.value(forKey: alarmStartTimeKey) as? Double else { return }

        fourHourTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] timer in
            guard let self = self else { return }

            let elapsed = Date().timeIntervalSince1970 - alarmStart
            let remaining = max(self.fourHours - elapsed, 0)

            if remaining <= 0 {
                self.fourHourText = "Alarm has Passed"
                timer.invalidate()
                self.startRepeatingNotifications(message: "Please take 5 mg Hydrocortisol")
                return
            }

            let hours = Int(remaining) / 3600
            let minutes = (Int(remaining) % 3600) / 60
            let seconds = Int(remaining) % 60

            self.fourHourText = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private func startEightHourTimer() {
        eightHourTimer?.invalidate()

        guard let alarmStart = UserDefaults.standard.value(forKey: alarmStartTimeKey) as? Double else { return }

        eightHourTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] timer in
            guard let self = self else { return }

            let elapsed = Date().timeIntervalSince1970 - alarmStart
            let remaining = max(self.eightHours - elapsed, 0)

            if remaining <= 0 {
                timer.invalidate()
                self.resetView()
                self.startRepeatingNotifications(message: "Please take 2.5 mg Hydrocortisol")
                return
            }

            let hours = Int(remaining) / 3600
            let minutes = (Int(remaining) % 3600) / 60
            let seconds = Int(remaining) % 60

            self.eightHourText = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
    
    private func checkExistingAlarms() {
        if let alarmStart = UserDefaults.standard.value(forKey: alarmStartTimeKey) as? Double {
            let elapsed = Date().timeIntervalSince1970 - alarmStart
            if elapsed >= eightHours {
                resetView()
            } else {
                isWakeButtonEnabled = false
                startFourHourTimer()
                startEightHourTimer()
            }
        }
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }
    
    func stopAlarms() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
    
    func startRepeatingNotifications(message: String) {
        // Cancel any existing pending notifications first
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()

        for minuteOffset in 0..<60 {
            let content = UNMutableNotificationContent()
            content.title = "Medication Reminder"
            content.body = message
            content.sound = UNNotificationSound.default

            let interval = max(1, minuteOffset * 30)
            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: TimeInterval(interval), repeats: false)

            let request = UNNotificationRequest(identifier: "repeatingAlarm_\(minuteOffset)", content: content, trigger: trigger)

            UNUserNotificationCenter.current().add(request)
        }
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {

        // When app is foreground: show banner + play sound
        completionHandler([.banner, .sound])
    }
}
