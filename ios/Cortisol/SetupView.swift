//
//  SetupView.swift
//  Cortisol
//
//  Created by Hannes Vandecasteele on 5/1/25.
//

import SwiftUI

struct SetupView: View {
    @State private var selectedDiseases: [String: Bool] = [
        "Addison's Disease": false
    ]

    @AppStorage("hasCompletedSetup") var hasCompletedSetup: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("Welcome! Please select your condition:")
                .font(.title2)
                .bold()
                .padding(.top, 20)

            VStack(alignment: .leading, spacing: 12) {
                ForEach(selectedDiseases.keys.sorted(), id: \.self) { disease in
                    Toggle(isOn: Binding(
                        get: { selectedDiseases[disease] ?? false },
                        set: { selectedDiseases[disease] = $0 }
                    )) {
                        Text(disease)
                    }
                    .toggleStyle(.button)
                }
            }
            .padding(.horizontal)

            Spacer()

            Button(action: saveAndContinue) {
                Text("Continue")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(isAnySelected ? Color.blue : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            .disabled(!isAnySelected)
            .padding(.horizontal, 20)
            .padding(.bottom, 50)
        }
        .padding()
    }

    var isAnySelected: Bool {
        selectedDiseases.values.contains(true)
    }

    private func saveAndContinue() {
        let diseasesJson = try? JSONEncoder().encode(selectedDiseases)
        UserDefaults.standard.set(diseasesJson, forKey: "Diseases")
        hasCompletedSetup = true
    }
}
