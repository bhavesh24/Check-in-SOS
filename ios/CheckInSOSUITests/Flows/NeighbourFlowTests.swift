import XCTest

final class NeighbourFlowTests: UITestCase {
    func testAllOkReturnsToList() {
        home.chooseNeighbour()
            .assertListShown()
            .simulateAlert()
            .accept()
            .confirmReached()
            .allOk()
    }

    func testEmergency999ShowsDemoMessageThenAlive() {
        home.chooseNeighbour()
            .simulateAlert()
            .accept()
            .confirmReached()
            .alertEmergencyServices()
            .call("999")
            .answerAlive(true)
    }

    func testEmergency111CancelThenCallAndReportNotAlive() {
        home.chooseNeighbour()
            .simulateAlert()
            .accept()
            .confirmReached()
            .alertEmergencyServices()
            .cancelSheet()
            .alertEmergencyServices()
            .call("111")
            .answerAlive(false)
    }
}
