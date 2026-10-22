# Defense demo checklist

1. Run the program from IntelliJ and choose `1`, destination `Campus`, payload `1.2`; note the workflow ID and `COMPLETED` status.
2. Choose `4` to show that route, drone reservation, delivery, and workflow were written to SQLite.
3. Choose `2`, destination `Campus B failure`, payload `1.0`; show that the route becomes `CANCELLED` and no drone reservation was committed.
4. Choose `3`, destination `Campus C failure`, payload `1.0`; show the drone becomes `RELEASED` and the route becomes `CANCELLED`.
5. Choose `6` to show invalid input is rejected.
6. Explain restart persistence: stop and restart the app, then choose `4`; the same rows remain.
7. Explain `5` recovery. For a real interrupted-chain demonstration, set a breakpoint after A or B, terminate the JVM while paused, restart and run recovery.
