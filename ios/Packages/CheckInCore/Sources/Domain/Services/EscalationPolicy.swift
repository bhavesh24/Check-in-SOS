import Foundation

/// How long a recipient has to acknowledge an alert, by score.
public enum EscalationPolicy {
    /// Placeholder: no timeout is specified for a missed check-in.
    public static let missedCheckInTimeout: TimeInterval = 15 * 60

    /// True if a score should alert the 1st-level neighbour.
    public static func shouldAlert(score: Int) -> Bool { !ScoreCalculator.isGood(score) }

    /// 50–69 → 30 min, 30–49 → 20 min, 0–29 → 15 min, ≥70 → nil (no alert), nil score → missed check-in timeout.
    public static func acknowledgementTimeout(forScore score: Int?) -> TimeInterval? {
        guard let score else { return missedCheckInTimeout }
        switch score {
        case ScoreCalculator.goodThreshold...: return nil
        case 50...: return 30 * 60
        case 30...: return 20 * 60
        default: return 15 * 60
        }
    }
}
