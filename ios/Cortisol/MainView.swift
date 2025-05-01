//
//  MainView.swift
//  Cortisol
//
//  Created by Hannes Vandecasteele on 5/1/25.
//

import SwiftUI

struct MainView: View {
    @StateObject private var viewModel = MainViewModel()

    var body: some View {
        VStack(spacing: 40) {
            Text("Your Daily Treatment Plan")
                .font(.title)
                .bold()
                .padding(.top, 20)

            VStack(spacing: 10) {
                Text("Time until 5 mg hydrocortisol")
                    .font(.headline)

                Text(viewModel.fourHourText)
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(.red)
            }

            VStack(spacing: 10) {
                Text("Time until 2.5 mg hydrocortisol")
                    .font(.headline)

                Text(viewModel.eightHourText)
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(.red)
            }

            Spacer()

            Button(action: {
                viewModel.startAlarms()
            }) {
                Text("Start Treatment (Wake)")
                    .font(.title3)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(viewModel.isWakeButtonEnabled ? Color.blue : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .disabled(!viewModel.isWakeButtonEnabled)
            .padding(.horizontal)
            .padding(.bottom, 50)
        }
        .padding()
        .alert(isPresented: $viewModel.showAlert) {
            Alert(title: Text("Cortisol Alert"), message: Text(viewModel.alertMessage), dismissButton: .default(Text("OK")))
        }
    }
}
