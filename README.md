# Assignment 4 — Facade over a three-stage Delivery Drone Dispatch chain

A runnable Java 21 console application for the **Delivery Drone Dispatch** variant (A: Route Planner + Weather Checker; B: Battery Manager + Payload Sensor; C: Landing Pad Controller + Delivery Confirmation).

The common client calls one operation, `DroneDispatchFacade.dispatch(...)`. The three subsystems remain independent and each has its own business rules, stateful entity, and SQLite repository. The facade persists progress in `dispatch_process` after each committed stage and uses persisted IDs/statuses to compensate a failed chain.

## Assignment requirements implemented

- **Three independent subsystems:** each has four functional classes plus its status enum (five classes), including business logic, an entity with guarded status transitions, and a repository that talks directly to SQLite. No subsystem calls or imports another subsystem.
- **Two facades:** `DroneDispatchFacade` for A → B → C; `DispatchAdminFacade` for inspection/recovery/manual compensation.
- **Durable state:** SQLite database on disk for each subsystem's records and for the chain's progress.
- **Both compensation depths:** B failure compensates A; C failure compensates B and then A.
- **Additional failure case:** unsafe weather, invalid payload, or an unavailable landing pad.
- **Independent subsystem demos:** `RouteSubsystemDemo`, `DroneSubsystemDemo`, and `DeliverySubsystemDemo` each demonstrate their subsystem without using either other subsystem or a facade.
- **Tests:** happy path, both compensation depths, persisted-state reload/recovery, input/weather/landing-pad failures, and entity transition rules. The test helper `dispatchSuccessfullyTo(...)` is a small domain-specific test DSL.
- **Coverage:** JaCoCo generates a line-coverage report at `target/site/jacoco/index.html`; the assignment target is at least 80% line coverage across the three subsystems and facades. Run the report and inspect the measured result before submission.
- **Documentation:** rendered vector class diagram in `docs/uml-class-diagram.svg` (with editable PlantUML source in `docs/uml-class-diagram.puml`), Least Knowledge before/after, and Facade-vs-Adapter reflection are included below.

## Requirements

- JDK 21 or newer
- IntelliJ IDEA (Community Edition is enough)
- Maven 3.9 or newer (used for SQLite/JUnit dependencies, repeatable builds, tests, and JaCoCo coverage)
- Internet access on the first build so Maven can download dependencies

## Open and run in IntelliJ IDEA

1. Extract this ZIP.
2. In IntelliJ IDEA choose **Open** and select the `delivery-drone-dispatch` folder containing `pom.xml`.
3. Set **Project SDK** to Java 21 and allow Maven to import dependencies.
4. Run `src/main/java/edu/assignment/dronedispatch/cli/Main.java`.

From a terminal in the project root:

```bash
mvn clean test
mvn exec:java -Dexec.mainClass="edu.assignment.dronedispatch.cli.Main"
```

The first command runs the tests and generates the JaCoCo report. The second launches the interactive demo.

## Live demo checklist

From the console menu:

- `1` — successful A → B → C dispatch
- `2` — force B to fail after A has committed; A is compensated
- `3` — force C to fail after A and B have committed; B is compensated first, then A
- `4` — inspect persisted workflow and subsystem records
- `5` — recover incomplete workflows from persisted statuses
- `6` — submit an invalid request to demonstrate an early failure
- `7` — manually compensate a workflow by ID
- `0` — exit

Every run prints a workflow ID. The main demo database is `data/dispatch.db` relative to the project working directory and survives application restarts. To reset the demo, stop the application and delete `data/dispatch.db` (and `data/dispatch.db-wal` / `data/dispatch.db-shm` if present).

### Show that subsystems work independently

Run any one of these main classes directly in IntelliJ:

- `edu.assignment.dronedispatch.demo.RouteSubsystemDemo`
- `edu.assignment.dronedispatch.demo.DroneSubsystemDemo`
- `edu.assignment.dronedispatch.demo.DeliverySubsystemDemo`

Each uses its own SQLite demo file under `data/` and does not call the facade or either other subsystem.

## Restart recovery and compensation

The facade writes `STARTED`, `A_COMPLETED`, `B_COMPLETED`, and `COMPLETED` workflow states as the chain progresses. If a later stage fails, the facade reloads the workflow record from SQLite and uses its persisted identifiers to decide what to compensate. If the application is interrupted mid-chain, restart it and select menu item `5`; recovery reads unfinished workflows from disk rather than relying on in-memory flags.

For a live interruption demo, use a debugger and pause/terminate the application after a stage status has been persisted, then restart and choose option `5`. The automated recovery test simulates an interrupted `A_COMPLETED` workflow and reopens the same database file before invoking recovery.

## Principle of Least Knowledge — before / after

**Before Facade:** the client would need knowledge of route planning, weather validation, route persistence, battery checks, payload validation, drone persistence, landing-pad availability, delivery confirmation, workflow persistence, and the reverse compensation order. That creates many immediate collaborators and a deep call sequence.

```java
// Illustrative anti-pattern: the client coordinates every subsystem itself.
RoutePlan route = routePlanner.plan(destination, weatherChecker);
routeRepository.save(route);
// Then the client must check battery and payload, reserve/save a drone,
// check the landing pad, confirm/save delivery, and undo earlier writes on failure.
```

**After Facade:** the client knows one immediate collaborator and invokes one domain operation:

```java
DispatchResult result = droneDispatchFacade.dispatch(request, FailureMode.NORMAL);
```

The facade owns sequencing, durable progress, failure handling, and compensation. The subsystems do not know about one another.

## Facade vs Adapter

Facade is the right pattern because the intent is to provide one simplified, coordinated operation across three independent subsystems. In this domain, route planning, drone preparation, and delivery confirmation must execute in order, with persisted progress and reverse compensation if a later stage fails. An Adapter has a different intent: translating one interface into another so incompatible interfaces can collaborate. This assignment is a workflow-coordination problem, not primarily an interface-compatibility problem.

## Tests and coverage

Run:

```bash
mvn clean test
```

Open `target/site/jacoco/index.html` and verify the required **80% line coverage** for the three subsystem packages and the facade package. Keep the generated report available for the defense. Do not claim a coverage percentage until it is measured in your environment.

## Git submission

Create a repository on GitHub, then run these commands from this project folder (replace the URL with your repository URL):

```bash
git init
git add .
git commit -m "Implement Delivery Drone Dispatch Facade"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Commit tests alongside the code they cover, as required by the assignment.

## Defense notes

- Show that no subsystem imports or calls either of the other subsystems.
- Run a successful dispatch and inspect the persisted state.
- Run menu options `2` and `3` and point out the different compensation depths.
- Explain why SQLite, not in-memory flags, is the source of truth for workflow progress.
- The weather, battery, payload, and landing-pad checks are deterministic simulations for the assignment, not real flight-control software.
