import Foundation

/// "Are you" — physical wellbeing answer.
public enum PhysicalWellbeing: String, CaseIterable, Sendable {
    case fine, good, ill, verySick
}

/// "Are you" — mood answer.
public enum Mood: String, CaseIterable, Sendable {
    case happy, pleasant, anxious, depressed
}

/// A completed daily check-in.
public struct Assessment: Equatable, Sendable {
    public let physical: PhysicalWellbeing
    public let mood: Mood

    public init(physical: PhysicalWellbeing, mood: Mood) {
        self.physical = physical
        self.mood = mood
    }
}

/// Outcome of `SubmitAssessment`.
public struct AssessmentResult: Equatable, Sendable {
    public let score: Int
    public let neighbourNotified: Bool

    public init(score: Int, neighbourNotified: Bool) {
        self.score = score
        self.neighbourNotified = neighbourNotified
    }
}
