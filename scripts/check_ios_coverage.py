#!/usr/bin/env python3
"""iOS line-coverage gate (standard library only).

Usage:
    check_ios_coverage.py <bundle.xcresult> <min-percent> [--scope all|presentation] [--json report.json]

Reads `xcrun xccov view --report --json <bundle>`, drops test targets and the explicit
exclusion list below, computes line coverage over what remains and exits 1 when it is
below <min-percent>. `--json` reads a pre-exported xccov report instead (used by tests/CI debugging).

Scopes:
  all           every non-test file (Domain, Data, app target) minus EXCLUDED_FILES
  presentation  app-target files under Features/, DesignSystem/ and App/AppRouter.swift only
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys

# Excluded by name: app entry point, DI wiring, test-only fakes. Keep in sync with CLAUDE.md.
EXCLUDED_FILES = {
    "CheckInSOSApp.swift",
    "AppContainer.swift",
}
EXCLUDED_PATH_PARTS = ("/Fakes/", "/Previews/", "/.build/", "/checkouts/")

PRESENTATION_PATH_PARTS = ("/Features/", "/DesignSystem/")
PRESENTATION_FILES = {"AppRouter.swift"}


def is_test_target(name: str) -> bool:
    base = name.removesuffix(".xctest")
    return base.endswith("Tests") or base.endswith("UITests")


def included(path: str, scope: str) -> bool:
    name = path.rsplit("/", 1)[-1]
    if name in EXCLUDED_FILES or any(part in path for part in EXCLUDED_PATH_PARTS):
        return False
    if scope == "presentation":
        return any(part in path for part in PRESENTATION_PATH_PARTS) or name in PRESENTATION_FILES
    return True


def load_report(bundle: str | None, json_path: str | None) -> dict:
    if json_path:
        with open(json_path, encoding="utf-8") as fh:
            return json.load(fh)
    out = subprocess.run(
        ["xcrun", "xccov", "view", "--report", "--json", bundle],
        check=True, capture_output=True, text=True,
    ).stdout
    return json.loads(out)


def measure(report: dict, scope: str) -> tuple[int, int, list[tuple[str, int, int]]]:
    covered = executable = 0
    rows: dict[str, tuple[int, int]] = {}
    for target in report.get("targets", []):
        if is_test_target(target.get("name", "")):
            continue
        for f in target.get("files", []):
            path = f.get("path", f.get("name", ""))
            if not included(path, scope):
                continue
            # The same file can appear in several targets (e.g. merged bundles); count it once.
            c, e = f.get("coveredLines", 0), f.get("executableLines", 0)
            prev = rows.get(path)
            if prev is None or c > prev[0]:
                rows[path] = (c, e)
    for c, e in rows.values():
        covered += c
        executable += e
    return covered, executable, sorted((p, c, e) for p, (c, e) in rows.items())


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("bundle", help="path to .xcresult (ignored when --json is given)")
    parser.add_argument("minimum", type=float, help="minimum line coverage percent, e.g. 80")
    parser.add_argument("--scope", choices=("all", "presentation"), default="all")
    parser.add_argument("--json", dest="json_path", help="pre-exported xccov JSON report")
    args = parser.parse_args(argv)

    covered, executable, rows = measure(load_report(args.bundle, args.json_path), args.scope)
    if executable == 0:
        print(f"error: no executable lines in scope '{args.scope}' — wrong bundle or exclusions?", file=sys.stderr)
        return 1

    for path, c, e in rows:
        pct = 100.0 * c / e if e else 100.0
        print(f"{pct:6.1f}%  {c:5d}/{e:<5d}  {path.split('/Sources/')[-1].split('/CheckInSOS/')[-1]}")
    percent = 100.0 * covered / executable
    status = "PASS" if percent >= args.minimum else "FAIL"
    print(f"\n{status}: {args.scope} line coverage {percent:.2f}% ({covered}/{executable}), minimum {args.minimum:g}%")
    return 0 if percent >= args.minimum else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
