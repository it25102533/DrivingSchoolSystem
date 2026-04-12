package com.drivingschool.system.backend.it25102535;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class VehicleController {

    private static final String T = "frontend/it25102535/";

    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/addVehicle")
    public String showAddVehicleForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return T + "add-vehicle";
    }

    @PostMapping("/saveVehicle")
    public String saveVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
        return "redirect:/vehicles";
    }

    @GetMapping("/vehicles")
    public String viewVehicles(Model model) {
        model.addAttribute("allVehicles", vehicleRepository.findAll());
        return T + "vehicle-list";
    }
}
