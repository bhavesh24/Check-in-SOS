import XCTest

/// Launches the app with fresh mock data and the fixed clock (`-ui-testing`).
class UITestCase: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["-ui-testing"]
        app.launch()
    }

    var home: HomeRobot { HomeRobot(app).assertShown() }
}
