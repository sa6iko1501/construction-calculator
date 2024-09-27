/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.ConstructionActivityRequest;
import com.turboproductions.consrtuctioncalculator.models.dto.ConstructionCalculationDto;
import com.turboproductions.consrtuctioncalculator.services.CalculationService;
import com.turboproductions.consrtuctioncalculator.services.MaterialService;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  public String getSelectRoomsPage() {
    return "rooms-number-page";
  }

  @GetMapping("/select-materials")
  public String getSelectMaterialsPage(
      @RequestParam("numRooms") int numRooms,
      Model model,
      @AuthenticationPrincipal User authenticatedUser) {
    if (numRooms <= 0 || numRooms > 100) {
      return "homepage";
    }
    ConstructionCalculationDto calculationDto =
        new ConstructionCalculationDto(new ArrayList<>(), null);
    for (int i = 0; i < numRooms; i++) {
      calculationDto.addRoomCalculation(new RoomCalculation());
    }
    List<Material> availableMaterials = materialService.getAllMaterials(authenticatedUser);
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
  public String createRoom(
      @ModelAttribute("roomsDTO") ConstructionCalculationDto constructionCalculationDto,
      RedirectAttributes model,
      @AuthenticationPrincipal User authenticatedUser) {
    ConstructionCalculation calculation = new ConstructionCalculation();
    calculation.setName(constructionCalculationDto.getCalculationName());
    String message =
        calculationService.handleConstructionCalculationCreation(
            calculation, constructionCalculationDto.getRooms(), authenticatedUser);
    if (message != null) {
      model.addFlashAttribute("message", message);
      return "homepage";
    }
    return "redirect:calculations";
  }

  @PostMapping("/calculations/setActivity")
  public ResponseEntity<String> updateActiveStatus(
      @RequestBody ConstructionActivityRequest request) {
    String msg = calculationService.setCalculationActivity(request);
    return ResponseEntity.ok(msg);
  }

  @GetMapping("/export/{id}")
  public ResponseEntity<InputStreamResource> exportCalculation(
      @PathVariable("id") UUID calculationId, @AuthenticationPrincipal User authenticatedUser) {
    ConstructionCalculation calculation = calculationService.getCalculation(calculationId);
    ByteArrayInputStream inputStream = calculationService.handleExcelExport(calculation);
    HttpHeaders headers = new HttpHeaders();
    String headerValue = String.format("attachment; filename=%s.xlsx", calculation.getName());
    headers.add("Content-Disposition", headerValue);
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(new InputStreamResource(inputStream));
  }

  @GetMapping("/calculations")
  public String getCalculations(Model model, @AuthenticationPrincipal User authenticatedUser) {
    List<ConstructionCalculation> calculations =
        calculationService.getAllCalculations(authenticatedUser);
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
  public String deleteCalculation(RedirectAttributes model, @PathVariable("id") UUID id) {
    calculationService.deleteCalculationById(id);
    model.addFlashAttribute("message", "Successfully deleted calculation.");
    return "redirect:/calculation/calculations";
  }
}
