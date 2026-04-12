package com.drivingschool.system.backend.manager;

/**
 * RoadSync professional roles — access rules enforced by {@link ManagerRoleAccessInterceptor}.
 */
public enum ManagerRole {
    /** Full system access — registrations, users, bookings, packages, payments, moderation, reports. */
    ADMIN,
    /** Vehicle fleet only — add/update/remove vehicles; view lessons for vehicle usage. */
    FLEET_MANAGER,
    /** Scheduling, instructors, progress, student–instructor matching; view vehicles read-only. */
    OPERATIONS_LEAD
}
