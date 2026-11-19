import Observation

/// Shown when the older adult has not checked in by the threshold time. Static copy; back only.
@MainActor @Observable
final class NoCheckInViewModel {
    let message = "As you have not checked in, a notification has been sent to your neighbour"
}
