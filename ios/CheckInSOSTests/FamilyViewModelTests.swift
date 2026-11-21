import Domain
import Foundation
import Testing
@testable import CheckInSOS

@MainActor @Suite struct LovedOnesViewModelTests {
    let store = FakeStore(adults: [
        adult("oa-1", "Margaret Wilson", score: 76, lastCheckIn: t0.addingTimeInterval(-3600)),
        adult("oa-2", "Arthur Patel", score: 38, lastCheckIn: t0.addingTimeInterval(-86_400)),
        adult("oa-4", "Ada Lane", score: 50, lastCheckIn: t0.addingTimeInterval(-2 * 86_400)),
        adult("oa-3", "Edith Clarke", score: nil),
    ])

    func makeVM(onSelect: @escaping (String) -> Void = { _ in }) -> LovedOnesViewModel {
        LovedOnesViewModel(getLovedOnes: GetLovedOnes(olderAdults: store), time: FixedTimeProvider(t0),
                           timeZone: TimeZone(identifier: "UTC")!, onSelect: onSelect)
    }

    @Test func loadsRowsAndSelects() async {
        let selected = Recorder<String>()
        let vm = makeVM(onSelect: selected.record)
        await vm.load()
        #expect(vm.lovedOnes.map(\.id) == ["oa-1", "oa-2", "oa-4", "oa-3"])
        #expect(vm.lovedOnes.map(vm.subtitle(for:)) == [
            "Last check-in Today, 08:00", "Last check-in Yesterday, 09:00", "Last check-in 13 Jan, 09:00",
            "No check-in yet",
        ])
        vm.select(vm.lovedOnes[1])
        #expect(selected.values == ["oa-2"])
    }

    @Test func failureShowsError() async {
        await store.setFailing(true)
        let vm = makeVM()
        await vm.load()
        #expect(vm.errorMessage == DisplayText.genericError)
    }
}

@MainActor @Suite struct StatusViewModelTests {
    let store = FakeStore()
    let edit = Recorder<String>()
    let timeline = Recorder<String>()

    func makeVM(_ id: String = "oa-1") -> StatusViewModel {
        StatusViewModel(olderAdultId: id, getStatus: GetLovedOneStatus(olderAdults: store),
                        getScoreHistory: GetScoreHistory(olderAdults: store), time: FixedTimeProvider(t0),
                        timeZone: TimeZone(identifier: "UTC")!, onEditNeighbour: edit.record, onTimeline: timeline.record)
    }

    @Test func loadsStatus() async {
        let vm = makeVM()
        #expect(vm.title.isEmpty && vm.neighbourText.isEmpty)
        await vm.load()
        #expect(vm.title == "Margaret Wilson")
        #expect(vm.adult?.latestScore == 76)
        #expect(vm.neighbourText == "1st level: Alex Brown")
        #expect(vm.scoreCaption == "Current score")
        #expect(vm.band == .good && vm.neighbourName == "Alex Brown")
        #expect(vm.neighbourCaption == "1st level neighbour")
        #expect(vm.lastCheckInText == "No check-in yet")
        #expect(!vm.showsTrend)
    }

    @Test func loadsLastCheckInAndTrend() async throws {
        try await store.recordScore(olderAdultId: "oa-1", score: 72, at: t0.addingTimeInterval(-86_400))
        try await store.recordScore(olderAdultId: "oa-1", score: 64, at: t0.addingTimeInterval(-3600))
        let vm = makeVM()
        await vm.load()
        #expect(vm.lastCheckInText == "Last check-in Today, 08:00")
        #expect(vm.band == .mild)
        #expect(vm.showsTrend && vm.history.map(\.score) == [72, 64])
        #expect(vm.trendAccessibilityText == "Recent check-ins: 72, 64")
    }

    @Test func navigationIntents() {
        let vm = makeVM()
        vm.editNeighbour()
        vm.openTimeline()
        #expect(edit.values == ["oa-1"] && timeline.values == ["oa-1"])
    }

    @Test func missingPersonShowsError() async {
        let vm = makeVM("nope")
        await vm.load()
        #expect(vm.errorMessage == DisplayText.genericError)
    }
}

@MainActor @Suite struct EditNeighbourViewModelTests {
    let store = FakeStore()
    let saved = Recorder<Void>()

    func makeVM(_ id: String = "oa-1") -> EditNeighbourViewModel {
        EditNeighbourViewModel(olderAdultId: id, getStatus: GetLovedOneStatus(olderAdults: store),
                               updateNeighbour: UpdateNeighbour(olderAdults: store), onSaved: { saved.record(()) })
    }

    @Test func prefillsOnceWithNationalNumber() async {
        let vm = makeVM()
        await vm.load()
        #expect([vm.firstName, vm.middleName, vm.lastName, vm.email, vm.mobile] ==
                ["Alex", "", "Brown", "alex@example.com", "7700900123"])
        vm.firstName = "Sam"
        await vm.load()
        #expect(vm.firstName == "Sam")
        #expect(vm.heading == "Your 1st level notified person" && vm.mobilePrefix == "+44")
    }

    @Test func savesValidNeighbour() async throws {
        let vm = makeVM()
        await vm.load()
        vm.firstName = "Sam"
        vm.lastName = "Green"
        await vm.save()
        #expect(saved.values.count == 1)
        #expect(vm.errorMessage == nil)
        let neighbour = try await store.olderAdult(id: "oa-1").neighbour
        #expect(neighbour.displayName == "Sam Green")
        #expect(neighbour.mobile == "07700900123")
    }

    @Test(arguments: [
        ("", "Brown", "a@example.com", "7700900123", "First name is required"),
        ("Sam", " ", "a@example.com", "7700900123", "Last name is required"),
        ("Sam", "Brown", "nope", "7700900123", "Enter a valid email"),
        ("Sam", "Brown", "a@example.com", "12", "Enter a valid UK mobile number"),
    ])
    func validationErrors(first: String, last: String, email: String, mobile: String, message: String) async {
        let vm = makeVM()
        await vm.load()
        vm.firstName = first
        vm.lastName = last
        vm.email = email
        vm.mobile = mobile
        await vm.save()
        #expect(vm.errorMessage == message)
        #expect(saved.values.isEmpty)
    }

    @Test func failuresShowGenericError() async {
        let missing = makeVM("nope")
        await missing.load()
        #expect(missing.errorMessage == DisplayText.genericError)
        #expect(!missing.isLoaded)

        let vm = makeVM()
        await vm.load()
        await store.setFailing(true)
        await vm.save()
        #expect(vm.errorMessage == DisplayText.genericError)
    }

    @Test func mobileNumberConversion() {
        #expect(EditNeighbourViewModel.nationalNumber("07700900123") == "7700900123")
        #expect(EditNeighbourViewModel.nationalNumber("+447700900123") == "+447700900123")
        #expect(EditNeighbourViewModel.storedNumber(" 7700 900123 ") == "07700 900123")
        #expect(EditNeighbourViewModel.storedNumber("07700900123") == "07700900123")
        #expect(EditNeighbourViewModel.storedNumber("+447700900123") == "+447700900123")
        #expect(EditNeighbourViewModel.storedNumber("") == "")
    }
}

@MainActor @Suite struct TimelineViewModelTests {
    @Test func rowsOldestFirstWithTimesAndColours() async throws {
        let store = FakeStore()
        _ = try await store.append(olderAdultId: "oa-2", kind: .careOutcome(.recovered), date: t0)
        _ = try await store.append(olderAdultId: "oa-2", kind: .score(38), date: t0.addingTimeInterval(-3 * 3600))
        let vm = TimelineViewModel(olderAdultId: "oa-2", getTimeline: GetTimeline(timeline: store),
                                   timeZone: TimeZone(identifier: "UTC")!)
        await vm.load()
        #expect(vm.rows.map(\.title) == ["Score 38", "Recovered"])
        #expect(vm.rows.map(\.time) == ["06:00", "09:00"])
        #expect(vm.rows.map(\.severity) == [.critical, .ok])
        #expect(vm.rows.map(\.kind) == [.score(38), .careOutcome(.recovered)])
        #expect(vm.rows.map(\.testID) == ["timeline.event.0", "timeline.event.1"])
        #expect(!vm.isEmpty)
    }

    @Test func emptyAndFailure() async {
        let store = FakeStore()
        let vm = TimelineViewModel(olderAdultId: "oa-3", getTimeline: GetTimeline(timeline: store), timeZone: .gmt)
        #expect(!vm.isEmpty)
        await vm.load()
        #expect(vm.isEmpty)
        #expect(vm.emptyText == "No events yet")

        await store.setFailing(true)
        let failing = TimelineViewModel(olderAdultId: "oa-3", getTimeline: GetTimeline(timeline: store), timeZone: .gmt)
        await failing.load()
        #expect(failing.errorMessage == DisplayText.genericError)
    }
}
