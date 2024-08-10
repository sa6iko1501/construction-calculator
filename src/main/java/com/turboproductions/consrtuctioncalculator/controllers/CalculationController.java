/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.dto.ConstructionCalculationDto;
import com.turboproductions.consrtuctioncalculator.services.CalculationService;
import com.turboproductions.consrtuctioncalculator.services.MaterialService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/calculation")
public class CalculationController {
  private final MaterialService materialService;
  private final CalculationService calculationService;

  public CalculationController(
      MaterialService materialService, CalculationService calculationService) {
    this.materialService = materialService;
    this.calculationService = calculationService;
  }

  @GetMapping("/rooms")
  public String getSelectRoomsPage(Model model) {
    return "rooms-number-page";
  }

  @GetMapping("/select-materials")
  public String getSelectMaterialsPage(@RequestParam("numRooms") int numRooms, Model model) {
    if (numRooms <= 0 || numRooms > 100) {
      return "homepage";
    }
    ConstructionCalculationDto calculationDto =
        new ConstructionCalculationDto(new ArrayList<>(), null);
    for (int i = 0; i < numRooms; i++) {
      calculationDto.addRoomCalculation(new RoomCalculation());
    }
    List<Material> availableMaterials = materialService.getAllMaterials();
    if (availableMaterials.size() >= 3) {
      List<Material> floorMaterials =
          materialService.filterByType(availableMaterials, MaterialType.FLOOR);
      List<Material> wallMaterials =
          materialService.filterByType(availableMaterials, MaterialType.WALL);
      List<Material> ceilingMaterials =
          materialService.filterByType(availableMaterials, MaterialType.CEILING);
      if (floorMaterials.isEmpty() || wallMaterials.isEmpty() || ceilingMaterials.isEmpty()) {
        model.addAttribute("message", "Not enough available materials");
        return "homepage";
      }
      model.addAttribute("floorMaterials", floorMaterials);
      model.addAttribute("wallMaterials", wallMaterials);
      model.addAttribute("ceilingMaterials", ceilingMaterials);
      model.addAttribute("calculationDto", calculationDto);
      return "calculate-rooms-page";
    }
    model.addAttribute("message", "No available materials.");
    return "homepage";
  }

  @PostMapping("/select-materials")
  public String getRoomDetails(
      @ModelAttribute("roomsDTO") ConstructionCalculationDto constructionCalculationDto,
      Model model) {
    ConstructionCalculation calculation = new ConstructionCalculation();
    calculation.setName(constructionCalculationDto.getCalculationName());
    String message =
        calculationService.handleConstructionCalculationCreation(
            calculation, constructionCalculationDto.getRooms());
    if (message != null) {
      model.addAttribute("message", message);
      return "homepage";
    }
    return getCalculations(model);
  }

  @GetMapping("/calculations")
  public String getCalculations(Model model) {
    List<ConstructionCalculation> calculations = calculationService.getAllCalculations();
    if (!calculations.isEmpty()) {
      model.addAttribute("calculations", calculations);
      return "calculations-page";
    }
    model.addAttribute("message", "You currently have no calculations.");
    return "homepage";
  }

  @GetMapping("/info/{id}")
  public String getCalculationInfo(Model model, @PathVariable("id") UUID id) {
    ConstructionCalculation calculation = calculationService.getCalculation(id);
    if (calculation != null) {
      model.addAttribute("calculation", calculation);
      model.addAttribute("rooms", calculation.getRoomCalculations().stream().toList());
      return "calculation-details-page";
    }
    model.addAttribute("message", "Invalid Calculation");
    return "homepage";
  }

  @PostMapping("/delete/{id}")
  public String deleteCalculation(Model model, @PathVariable("id") UUID id) {
    calculationService.deleteCalculationById(id);
    return getCalculations(model);
  }
}
