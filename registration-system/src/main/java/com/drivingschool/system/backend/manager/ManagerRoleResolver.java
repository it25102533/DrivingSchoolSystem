package com.drivingschool.system.backend.manager;

/**
 * Ensures {@link Manager#getRole()} is set (e.g. legacy rows) using the login email.
 */
public final class ManagerRoleResolver {

    private ManagerRoleResolver() {
    }

    public static ManagerRole inferFromEmail(String email) {
        if (email == null) {
            return ManagerRole.OPERATIONS_LEAD;
        }
        String e = email.trim().toLowerCase();
        if ("admin@gmail.com".equals(e)) {
            return ManagerRole.ADMIN;
        }
        if ("fleet@gmail.com".equals(e)) {
            return ManagerRole.FLEET_MANAGER;
        }
        if ("opslead@gmail.com".equals(e)) {
            return ManagerRole.OPERATIONS_LEAD;
        }
        return ManagerRole.OPERATIONS_LEAD;
    }

    /** Persist role if missing so interceptors and templates always see a role. */
    public static void ensureRolePersisted(Manager manager, ManagerRepository managerRepository) {
        if (manager.getRole() != null) {
            return;
        }
        manager.setRole(inferFromEmail(manager.getEmail()));
        managerRepository.save(manager);
    }
}
