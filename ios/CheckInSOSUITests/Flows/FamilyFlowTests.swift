import XCTest

final class FamilyFlowTests: UITestCase {
    func testStatusShowsScoreAndNeighbour() {
        home.chooseFamily()
            .assertLovedOnesShown()
            .openStatus("oa-1", name: "Margaret Wilson")
            .assertStatus(scoreLabel: "Score 76 out of 100", neighbour: "Alex Brown")
    }

    func testEditNeighbourSavesAndStatusShowsNewName() {
        home.chooseFamily()
            .openStatus("oa-1", name: "Margaret Wilson")
            .editNeighbour()
            .assertField("firstName", "Alex")
            .assertField("lastName", "Brown")
            .assertField("email", "alex.brown@example.com")
            .assertField("mobile", "7700900123")
            .replace("firstName", with: "Sam")
            .save()
            .assertStatus(scoreLabel: "Score 76 out of 100", neighbour: "Sam Brown")
    }

    func testEditNeighbourValidation() {
        home.chooseFamily()
            .openStatus("oa-2", name: "Arthur Patel")
            .editNeighbour()
            .replace("firstName", with: "")
            .save()
            .assertError("First name is required")
            .replace("firstName", with: "Priya")
            .replace("email", with: "not-an-email")
            .save()
            .assertError("Enter a valid email")
            .replace("email", with: "p@example.com")
            .replace("mobile", with: "12")
            .save()
            .assertError("Enter a valid UK mobile number")
    }

    func testTimelineShowsEventsInOrder() {
        home.chooseFamily()
            .openStatus("oa-2", name: "Arthur Patel")
            .assertStatus(scoreLabel: "Score 38 out of 100", neighbour: "Priya Shah")
            .openTimeline()
            .assertTimeline([
                "Score 38, 06:00",
                "Notification sent to 1st level, 06:01",
                "1st level acknowledged, 06:10",
                "Neighbour reached, 06:25",
                "Escalation to 2nd level, 06:30",
                "2nd level acknowledged, 06:40",
                "In care in hospital, 07:30",
                "Recovered, 08:50",
            ])
    }

    func testPersonWithoutScoreHasEmptyTimeline() {
        let robot = home.chooseFamily()
            .openStatus("oa-3", name: "Edith Clarke")
            .assertStatus(scoreLabel: "No score yet", neighbour: "Tom Hughes")
            .openTimeline()
        robot.assertLabel("timeline.empty", "No events yet")
        robot.assertMissing("timeline.event.0")
    }
}
