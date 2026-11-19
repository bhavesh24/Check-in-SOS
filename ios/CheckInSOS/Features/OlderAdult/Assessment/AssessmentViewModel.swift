import Domain
import Observation

/// Daily check-in: one physical answer + one mood answer, then `SubmitAssessment`.
@MainActor @Observable
final class AssessmentViewModel {
    private(set) var physical: PhysicalWellbeing?
    private(set) var mood: Mood?
    private(set) var isSubmitting = false
    private(set) var errorMessage: String?

    let step = 1
    let totalSteps = FlowSteps.olderAdult

    var canSubmit: Bool { physical != nil && mood != nil && !isSubmitting }

    private let submitAssessment: SubmitAssessment
    private let olderAdultId: String
    private let onSubmitted: (AssessmentResult) -> Void
    private let onMissedCheckIn: () -> Void

    init(submitAssessment: SubmitAssessment, olderAdultId: String,
         onSubmitted: @escaping (AssessmentResult) -> Void, onMissedCheckIn: @escaping () -> Void) {
        self.submitAssessment = submitAssessment
        self.olderAdultId = olderAdultId
        self.onSubmitted = onSubmitted
        self.onMissedCheckIn = onMissedCheckIn
    }

    func select(_ physical: PhysicalWellbeing) { self.physical = physical }
    func select(_ mood: Mood) { self.mood = mood }

    func submit() async {
        guard let physical, let mood, !isSubmitting else { return }
        isSubmitting = true
        errorMessage = nil
        defer { isSubmitting = false }
        do {
            let result = try await submitAssessment(olderAdultId: olderAdultId,
                                                    assessment: Assessment(physical: physical, mood: mood))
            onSubmitted(result)
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func showMissedCheckIn() { onMissedCheckIn() }
}

extension PhysicalWellbeing {
    var title: String {
        switch self {
        case .fine: "Fine"
        case .good: "Good"
        case .ill: "Ill"
        case .verySick: "Very sick"
        }
    }

    var testID: String { "assessment.physical.\(rawValue)" }
}

extension Mood {
    var title: String {
        switch self {
        case .happy: "Happy"
        case .pleasant: "Pleasant"
        case .anxious: "Anxious"
        case .depressed: "Depressed"
        }
    }

    var testID: String { "assessment.mood.\(rawValue)" }
}
