// swift-tools-version: 6.0
import PackageDescription

// Clean Architecture core for Check-in SOS.
// Domain: pure Swift (Foundation only). Data depends on Domain. The app target (Presentation) depends on both.
let package = Package(
    name: "CheckInCore",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "Domain", targets: ["Domain"]),
        .library(name: "Data", targets: ["Data"]),
    ],
    targets: [
        .target(name: "Domain"),
        .target(name: "Data", dependencies: ["Domain"]),
        .testTarget(name: "DomainTests", dependencies: ["Domain"]),
        .testTarget(name: "DataTests", dependencies: ["Data", "Domain"]),
    ]
)
