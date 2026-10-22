package edu.assignment.dronedispatch.common;

public record DispatchResult(String workflowId, String status, String message) {
}
