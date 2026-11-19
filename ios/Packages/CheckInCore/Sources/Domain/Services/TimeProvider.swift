import Foundation

/// Injectable clock so countdowns and timestamps are deterministic in tests.
/// (Named to avoid clashing with Swift's `Clock` protocol.)
public protocol TimeProvider: Sendable {
    func now() -> Date
}

public struct SystemTimeProvider: TimeProvider {
    public init() {}
    public func now() -> Date { Date() }
}

public struct FixedTimeProvider: TimeProvider {
    public let date: Date
    public init(_ date: Date) { self.date = date }
    public func now() -> Date { date }
}
