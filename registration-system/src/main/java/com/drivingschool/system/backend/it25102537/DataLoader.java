package com.drivingschool.system.backend.it25102537;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final PackageRepository packageRepository;

    public DataLoader(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @Override
    public void run(String... args) {
        if (packageRepository.count() != 0) {
            return;
        }

        LessonPackage bronze = new LessonPackage();
        bronze.setPackageName("Bronze - 5 Lessons");
        bronze.setNumberOfLessons(5);
        bronze.setBasePrice(250.0);

        LessonPackage silver = new LessonPackage();
        silver.setPackageName("Silver - 10 Lessons");
        silver.setNumberOfLessons(10);
        silver.setBasePrice(450.0);

        LessonPackage gold = new LessonPackage();
        gold.setPackageName("Gold - 20 Lessons");
        gold.setNumberOfLessons(20);
        gold.setBasePrice(800.0);

        packageRepository.save(bronze);
        packageRepository.save(silver);
        packageRepository.save(gold);

        System.out.println("Default driving packages loaded!");
    }
}
