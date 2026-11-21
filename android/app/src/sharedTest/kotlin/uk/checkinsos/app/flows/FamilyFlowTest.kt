package uk.checkinsos.app.flows

import org.junit.Test

class FamilyFlowTest : FlowTest() {
    @Test fun statusShowsScoreAndNeighbour() {
        home.chooseFamily()
            .assertLovedOnesShown()
            .openStatus("oa-1", "Margaret Wilson")
            .assertStatus("Score 76 out of 100", "Alex Brown")
            .assertCheckIns("Last check-in Today, 08:00", "Recent check-ins: 72, 70, 74, 68, 73, 78, 76")
    }

    @Test fun editNeighbourSavesAndStatusShowsNewName() {
        home.chooseFamily()
            .openStatus("oa-1", "Margaret Wilson")
            .editNeighbour()
            .assertField("firstName", "Alex")
            .assertField("lastName", "Brown")
            .assertField("email", "alex.brown@example.com")
            .assertField("mobile", "7700900123")
            .replace("firstName", "Sam")
            .save()
            .assertStatus("Score 76 out of 100", "Sam Brown")
    }

    @Test fun editNeighbourValidation() {
        home.chooseFamily()
            .openStatus("oa-2", "Arthur Patel")
            .editNeighbour()
            .replace("firstName", "")
            .save()
            .assertError("First name is required")
            .replace("firstName", "Priya")
            .replace("email", "not-an-email")
            .save()
            .assertError("Enter a valid email")
            .replace("email", "p@example.com")
            .replace("mobile", "12")
            .save()
            .assertError("Enter a valid UK mobile number")
    }

    @Test fun timelineShowsEventsInOrder() {
        home.chooseFamily()
            .openStatus("oa-2", "Arthur Patel")
            .assertStatus("Score 38 out of 100", "Priya Shah")
            .openTimeline()
            .assertTimeline(
                listOf(
                    "Score 38" to "06:00",
                    "Notification sent to 1st level" to "06:01",
                    "1st level acknowledged" to "06:10",
                    "Neighbour reached" to "06:25",
                    "Escalation to 2nd level" to "06:30",
                    "2nd level acknowledged" to "06:40",
                    "In care in hospital" to "07:30",
                    "Recovered" to "08:50",
                ),
            )
    }

    @Test fun personWithoutScoreHasEmptyTimeline() {
        val robot = home.chooseFamily()
            .openStatus("oa-3", "Edith Clarke")
            .assertStatus("No score yet", "Tom Hughes")
            .assertCheckIns("No check-in yet", null)
            .openTimeline()
        robot.assertText("timeline.empty", "No events yet")
        robot.assertMissing("timeline.event.0")
    }
}
