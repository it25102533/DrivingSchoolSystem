package com.drivingschool.system.backend.manager;

/**
 * Servlet containers may store {@link Long} session attributes as {@link Integer}.
 */
final class ManagerSessionSupport {

    private ManagerSessionSupport() {
    }

    static Long managerIdFromSession(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Long l) {
            return l;
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
