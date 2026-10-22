package edu.assignment.dronedispatch.facade;

import edu.assignment.dronedispatch.persistence.*;
import java.util.List;

/** Narrow administrative facade for inspection, recovery, and manual compensation. */
public final class DispatchAdminFacade {

 private final DispatchProcessRepository processes;
 private final DroneDispatchFacade dispatchFacade;

 public DispatchAdminFacade(DispatchProcessRepository processes, DroneDispatchFacade dispatchFacade) {
  this.processes = processes;
  this.dispatchFacade = dispatchFacade;
 }

 public List<DispatchProcess> inspectAll() {
  return processes.findAll();
 }

 public void compensate(String workflowId) {
  DispatchProcess p = processes.findById(workflowId)
          .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
  dispatchFacade.compensatePersisted(p, "Manual administrative compensation");
 }

 public int recoverIncompleteProcesses() {
  List<DispatchProcess> incomplete = processes.findIncomplete();
  for (DispatchProcess p : incomplete) {
   dispatchFacade.compensatePersisted(
           p,
           "Startup/manual recovery from persisted status " + p.status()
   );
  }
  return incomplete.size();
 }
}