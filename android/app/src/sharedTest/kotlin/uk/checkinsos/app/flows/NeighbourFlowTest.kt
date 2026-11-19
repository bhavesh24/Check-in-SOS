package uk.checkinsos.app.flows

import org.junit.Test

class NeighbourFlowTest : FlowTest() {
    @Test fun allOkReturnsToList() {
        home.chooseNeighbour().assertListShown().simulateAlert().accept().confirmReached().allOk()
    }

    @Test fun emergency999ShowsDemoMessageThenAlive() {
        home.chooseNeighbour().simulateAlert().accept().confirmReached()
            .alertEmergencyServices()
            .call("999")
            .answerAlive(yes = true)
    }

    @Test fun emergency111CancelThenCallAndReportNotAlive() {
        home.chooseNeighbour().simulateAlert().accept().confirmReached()
            .alertEmergencyServices()
            .cancelDialog()
            .alertEmergencyServices()
            .call("111")
            .answerAlive(yes = false)
    }
}
