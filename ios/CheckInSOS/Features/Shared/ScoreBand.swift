import Domain

/// How a check-in score is coloured on the Score ring. Follows the EscalationPolicy bands.
enum ScoreBand: Equatable {
    /// ≥ 70: no alert.
    case good
    /// 50–69.
    case mild
    /// 30–49.
    case moderate
    /// 0–29.
    case severe

    init(score: Int) {
        switch score {
        case ScoreCalculator.goodThreshold...: self = .good
        case 50...: self = .mild
        case 30...: self = .moderate
        default: self = .severe
        }
    }
}
