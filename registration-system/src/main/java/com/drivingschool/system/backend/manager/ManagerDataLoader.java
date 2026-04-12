package com.drivingschool.system.backend.manager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures the three demo manager accounts exist with correct BCrypt hashes and {@link ManagerRole}.
 * If the table has legacy rows (wrong emails) but not admin@gmail.com, all rows are cleared and re-seeded.
 */
@Component
@Order(5)
public class ManagerDataLoader implements CommandLineRunner {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerDataLoader(ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean migrateLegacy = managerRepository.count() > 0
                && managerRepository.findByEmailIgnoreCase("admin@gmail.com").isEmpty();
        if (migrateLegacy) {
            managerRepository.deleteAll();
        }
        boolean emptyTable = managerRepository.count() == 0;
        boolean resetPasswords = migrateLegacy || emptyTable;

        ensureManager(
                "admin@gmail.com",
                "admin123",
                "Administrator",
                "Administration",
                ManagerRole.ADMIN,
                resetPasswords);
        ensureManager(
                "opslead@gmail.com",
                "ops123",
                "Operations Lead",
                "Operations",
                ManagerRole.OPERATIONS_LEAD,
                resetPasswords);
        ensureManager(
                "fleet@gmail.com",
                "fleet123",
                "Fleet Manager",
                "Fleet",
                ManagerRole.FLEET_MANAGER,
                resetPasswords);

        if (migrateLegacy || emptyTable) {
            printBanner();
        }
    }

    private void ensureManager(
            String email,
            String plainPassword,
            String fullName,
            String department,
            ManagerRole role,
            boolean resetPasswords) {
        managerRepository.findByEmailIgnoreCase(email).ifPresentOrElse(
                m -> {
                    m.setUsername(email);
                    m.setRole(role);
                    if (resetPasswords) {
                        m.setPasswordHash(passwordEncoder.encode(plainPassword));
                    }
                    m.setFullName(fullName);
                    m.setDepartment(department);
                    managerRepository.save(m);
                },
                () -> {
                    Manager m = new Manager();
                    m.setUsername(email);
                    m.setPasswordHash(passwordEncoder.encode(plainPassword));
                    m.setFullName(fullName);
                    m.setEmail(email);
                    m.setDepartment(department);
                    m.setRole(role);
                    managerRepository.save(m);
                });
    }

    private void printBanner() {
        System.out.println();
        System.out.println("========== RoadSync — seeded manager logins (database) ==========");
        System.out.println("  email                 | password        | role");
        System.out.println("  admin@gmail.com       | admin123        | ADMIN");
        System.out.println("  opslead@gmail.com     | ops123          | OPERATIONS_LEAD");
        System.out.println("  fleet@gmail.com       | fleet123        | FLEET_MANAGER");
        System.out.println("  (Passwords are stored as BCrypt hashes in table `managers`.)");
        System.out.println("==================================================================");
        System.out.println();
    }
}
