/// Converts an assessment into a 0–100 wellbeing score.
///
/// PLACEHOLDER FORMULA: none is specified. Weights were chosen so the prototype's
/// examples reproduce (Good + Pleasant = 76, Ill + Anxious = 38). Needs clinical input — see docs/screens.md.
public enum ScoreCalculator {
    /// Scores at or above this are "good" and do not notify anyone.
    public static let goodThreshold = 70

    public static func score(_ assessment: Assessment) -> Int {
        points(for: assessment.physical) + points(for: assessment.mood)
    }

    public static func isGood(_ score: Int) -> Bool { score >= goodThreshold }

    static func points(for physical: PhysicalWellbeing) -> Int {
        switch physical {
        case .fine: 50
        case .good: 38
        case .ill: 20
        case .verySick: 5
        }
    }

    static func points(for mood: Mood) -> Int {
        switch mood {
        case .happy: 50
        case .pleasant: 38
        case .anxious: 18
        case .depressed: 5
        }
    }
}
