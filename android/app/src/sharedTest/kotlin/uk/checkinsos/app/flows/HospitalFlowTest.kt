package uk.checkinsos.app.flows

import org.junit.Test

class HospitalFlowTest : FlowTest() {
    @Test fun inCareRecoveringRecovered() {
        home.chooseHospital().assertListShown().simulateAlert().accept()
            .answer("inCare.yes").assertQuestion("Are they recovering?", step = 3)
            .answer("recovering.yes").assertQuestion("Have they recovered?", step = 4)
            .answer("recovered.yes").assertListShown()
    }

    @Test fun notRecoveringBadConditionAlive() {
        home.chooseHospital().simulateAlert().accept()
            .answer("inCare.yes")
            .answer("recovering.no").assertQuestion("Are they in bad condition?", step = 4)
            .answer("badCondition.yes").assertQuestion("Is your patient alive?", step = 5)
            .answer("patientAlive.yes").assertListShown()
    }

    @Test fun notInBadConditionAndDeceasedBranches() {
        home.chooseHospital().simulateAlert().accept()
            .answer("inCare.yes").answer("recovering.no").answer("badCondition.no").assertListShown()
            .simulateAlert().accept()
            .answer("inCare.yes").answer("recovering.no").answer("badCondition.yes")
            .answer("patientAlive.no").assertListShown()
    }
}
