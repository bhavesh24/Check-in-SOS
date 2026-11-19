import Domain
import Foundation
import Testing
@testable import CheckInSOS

@MainActor @Suite struct LovedOnesViewModelTests {
    let store = FakeStore(adults: [adult("oa-1", "Margaret Wilson", score: 76), adult("oa-3", "Edith Clarke", score: nil)])

    @Test func loadsRowsAndSelects() async {
        let selected = Recorder<String>()
        let vm = LovedOnesViewModel(getLovedOnes: GetLovedOnes(olderAdults: store), onSelect: selected.record)
        await vm.load()
        #expect(vm.lovedOnes.map(\.id) == ["oa-1", "oa-3"])
        #expect(vm.lovedOnes.map(vm.subtitle(for:)) == ["Score 76", "No score yet"])
        vm.select(vm.lovedOnes[1])
        #expect(selected.values == ["oa-3"])
    }

    @Test func failureShowsError() async {
        await store.setFailing(true)
        let vm = LovedOnesViewModel(getLovedOnes: GetLovedOnes(olderAdults: store), onSelect: { _ in })
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
                        onEditNeighbour: edit.record, onTimeline: timeline.record)
    }

    @Test func loadsStatus() async {
        let vm = makeVM()
        #expect(vm.title.isEmpty && vm.neighbourText.isEmpty)
        await vm.load()
        #expect(vm.title == "Margaret Wilson")
        #expect(vm.adult?.latestScore == 76)
        #expect(vm.neighbourText == "1st level: Alex Brown")
        #expect(vm.scoreCaption == "Current score")
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
                ["Alex", "", "Brown", "alex@example.com", "7123456789"])
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
        #expect(neighbour.mobile == "07123456789")
    }

    @Test(arguments: [
        ("", "Brown", "a@b.co", "7123456789", "First name is required"),
        ("Sam", " ", "a@b.co", "7123456789", "Last name is required"),
        ("Sam", "Brown", "nope", "7123456789", "Enter a valid email"),
        ("Sam", "Brown", "a@b.co", "12", "Enter a valid UK mobile number"),
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
        #expect(EditNeighbourViewModel.nationalNumber("07123456789") == "7123456789")
        #expect(EditNeighbourViewModel.nationalNumber("+447123456789") == "+447123456789")
        #expect(EditNeighbourViewModel.storedNumber(" 7123 456789 ") == "07123 456789")
        #expect(EditNeighbourViewModel.storedNumber("07123456789") == "07123456789")
        #expect(EditNeighbourViewModel.storedNumber("+447123456789") == "+447123456789")
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
