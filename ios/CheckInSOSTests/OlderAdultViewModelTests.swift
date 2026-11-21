import Domain
import Testing
@testable import CheckInSOS

@MainActor @Suite struct HomeViewModelTests {
    @Test func optionsMatchSpecOrderAndIds() {
        let recorder = Recorder<Role>()
        let vm = HomeViewModel(onSelect: recorder.record)
        #expect(vm.options.map(\.title) == ["Old age adult", "Concerned family", "Hospital", "Neighbour"])
        #expect(vm.options.map(\.id) == ["home.role.olderAdult", "home.role.family", "home.role.hospital",
                                         "home.role.neighbour"])
        #expect(vm.options.map(\.description) == ["Do your daily check-in", "See how your loved ones are doing",
                                                  "Respond to patient alerts", "Help someone living nearby"])
        vm.select(.hospital)
        #expect(recorder.values == [.hospital])
    }

    @Test func routerMapsRolesToFirstScreens() {
        #expect(AppRouter.route(for: .olderAdult) == .assessment)
        #expect(AppRouter.route(for: .family) == .lovedOnes)
        #expect(AppRouter.route(for: .hospital) == .patients)
        #expect(AppRouter.route(for: .neighbour) == .neighbours)
    }

    @Test func routerPushPopAndPopTo() {
        let router = AppRouter()
        router.push(.neighbours)
        router.push(.noCheckIn)
        router.push(.assessment)
        router.popTo(.neighbours)
        #expect(router.path == [.neighbours])
        router.push(.assessment)
        router.pop()
        #expect(router.path == [.neighbours])
        router.popTo(.patients)
        #expect(router.path.isEmpty)
        router.push(.lovedOnes)
        router.popToRoot()
        #expect(router.path.isEmpty)
    }
}

@MainActor @Suite struct AssessmentViewModelTests {
    let store = FakeStore()
    let submitted = Recorder<AssessmentResult>()
    let missed = Recorder<Void>()

    func makeVM() -> AssessmentViewModel {
        AssessmentViewModel(
            submitAssessment: SubmitAssessment(olderAdults: store, alerts: store, timeline: store,
                                               time: FixedTimeProvider(t0)),
            olderAdultId: "oa-1", onSubmitted: submitted.record, onMissedCheckIn: { missed.record(()) })
    }

    @Test func submitDisabledUntilBothSectionsAnswered() async {
        let vm = makeVM()
        #expect(!vm.canSubmit)
        vm.select(PhysicalWellbeing.good)
        #expect(!vm.canSubmit)
        await vm.submit()
        #expect(submitted.values.isEmpty)
        vm.select(Mood.pleasant)
        #expect(vm.canSubmit)
        #expect(vm.step == 1 && vm.totalSteps == 2)
    }

    @Test func goodAnswersScore76WithoutNotification() async {
        let vm = makeVM()
        vm.select(PhysicalWellbeing.good)
        vm.select(Mood.pleasant)
        await vm.submit()
        #expect(submitted.values == [AssessmentResult(score: 76, neighbourNotified: false)])
        #expect(!vm.isSubmitting)
    }

    @Test func unwellAnswersScore38AndNotify() async {
        let vm = makeVM()
        vm.select(PhysicalWellbeing.ill)
        vm.select(Mood.anxious)
        await vm.submit()
        #expect(submitted.values == [AssessmentResult(score: 38, neighbourNotified: true)])
    }

    @Test func failureShowsError() async {
        await store.setFailing(true)
        let vm = makeVM()
        vm.select(PhysicalWellbeing.fine)
        vm.select(Mood.happy)
        await vm.submit()
        #expect(submitted.values.isEmpty)
        #expect(vm.errorMessage == DisplayText.genericError)
    }

    @Test func missedCheckInDemoLink() {
        makeVM().showMissedCheckIn()
        #expect(missed.values.count == 1)
    }

    @Test func answerCopyAndTestIds() {
        #expect(PhysicalWellbeing.allCases.map(\.title) == ["Fine", "Good", "Ill", "Very sick"])
        #expect(PhysicalWellbeing.verySick.testID == "assessment.physical.verySick")
        #expect(Mood.allCases.map(\.title) == ["Happy", "Pleasant", "Anxious", "Depressed"])
        #expect(Mood.depressed.testID == "assessment.mood.depressed")
    }
}

@MainActor @Suite struct ScoreViewModelTests {
    @Test func scoreAndDone() {
        let done = Recorder<Void>()
        let vm = ScoreViewModel(score: 38, neighbourNotified: true, onDone: { done.record(()) })
        #expect(vm.notificationText == "A notification has been sent to your neighbour")
        #expect(vm.reassuranceText == "You're doing well today.")
        #expect(vm.step == 2 && vm.totalSteps == 2)
        #expect(vm.band == .moderate && !vm.showsReassurance)
        let ring = { (score: Int) in ScoreViewModel(score: score, neighbourNotified: false, onDone: {}) }
        #expect([100, 70, 69, 50, 49, 30, 29, 0].map { ring($0).band } ==
                [.good, .good, .mild, .mild, .moderate, .moderate, .severe, .severe])
        #expect(ring(70).showsReassurance && !ring(69).showsReassurance)
        #expect(ScoreNumeral.accessibilityLabel(for: 38) == "Score 38 out of 100")
        #expect(ScoreNumeral.accessibilityLabel(for: nil) == "No score yet")
        vm.done()
        #expect(done.values.count == 1)
    }

    @Test func noCheckInCopy() {
        #expect(NoCheckInViewModel().message ==
                "As you have not checked in, a notification has been sent to your neighbour")
    }

    @Test func sharedCopy() {
        #expect(DisplayText.score(76) == "Score 76")
        #expect(DisplayText.score(nil) == "No score yet")
        #expect(DisplayText.countdown(seconds: 1200) == "20:00")
        #expect(DisplayText.countdown(seconds: 61) == "01:01")
        #expect(DisplayText.countdown(seconds: 0) == "Time expired")
        #expect(StepProgress.text(step: 2, total: 4) == "Step 2 of 4")
    }
}
