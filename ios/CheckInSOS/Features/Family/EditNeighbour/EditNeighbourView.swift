import SwiftUI

struct EditNeighbourView: View {
    @Environment(\.roleTheme) private var theme
    @State var viewModel: EditNeighbourViewModel
    @FocusState private var focusedField: String?

    var body: some View {
        FlowScreen {
            Text(viewModel.heading)
                .font(.title2.weight(.bold))
                .foregroundStyle(Theme.text)
                .accessibilityAddTraits(.isHeader)
            // Above the fields so it stays visible while the keyboard is open.
            if let error = viewModel.errorMessage {
                ErrorBanner(message: error, testID: "editNeighbour.error")
            }
            field("First name*", text: $viewModel.firstName, id: "editNeighbour.firstName")
            field("Middle name", text: $viewModel.middleName, id: "editNeighbour.middleName")
            field("Last name*", text: $viewModel.lastName, id: "editNeighbour.lastName")
            field("Email*", text: $viewModel.email, id: "editNeighbour.email")
                .keyboardType(.emailAddress)
                .textContentType(.emailAddress)
            VStack(alignment: .leading, spacing: 6) {
                fieldLabel("Mobile*")
                HStack(spacing: 8) {
                    Text(viewModel.mobilePrefix)
                        .font(.body.weight(.semibold))
                        .foregroundStyle(Theme.text)
                        .padding(.horizontal, 12)
                        .frame(minHeight: Theme.minTouchTarget)
                        .background(theme.surfaceTint, in: RoundedRectangle(cornerRadius: 10))
                        .accessibilityLabel("Country code plus 44")
                    input("Mobile", text: $viewModel.mobile, id: "editNeighbour.mobile")
                        .keyboardType(.phonePad)
                }
            }
        } actions: {
            Button("Save") { Task { await viewModel.save() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isSaving)
                .accessibilityIdentifier("editNeighbour.save")
        }
        .toolbar {
            // The phone pad has no return key, so give every keyboard a way to close.
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("Done") { focusedField = nil }
                    .accessibilityIdentifier("keyboard.done")
            }
        }
        .navigationTitle("Edit Neighbour")
        .withBackButton()
        .task { await viewModel.load() }
    }

    private func field(_ title: String, text: Binding<String>, id: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            fieldLabel(title)
            input(title.replacingOccurrences(of: "*", with: ""), text: text, id: id)
        }
    }

    private func fieldLabel(_ title: String) -> some View {
        Text(title)
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(Theme.secondaryText)
            .accessibilityHidden(true)
    }

    private func input(_ label: String, text: Binding<String>, id: String) -> some View {
        // Vertical axis so long values wrap instead of clipping at large Dynamic Type sizes.
        TextField(label, text: text, axis: .vertical)
            .lineLimit(1...4)
            .font(.body)
            .foregroundStyle(Theme.text)
            .autocorrectionDisabled()
            .textInputAutocapitalization(id.hasSuffix("email") ? .never : .words)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .frame(minHeight: Theme.minTouchTarget)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Theme.secondaryText, lineWidth: 1))
            .focused($focusedField, equals: id)
            .accessibilityLabel(label)
            .accessibilityIdentifier(id)
    }
}
