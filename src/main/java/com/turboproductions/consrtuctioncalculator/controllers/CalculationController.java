/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.dto.RoomCalculationDTO;
import com.turboproductions.consrtuctioncalculator.services.MaterialService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/calculation")
public class CalculationController {
  private final MaterialService materialService;

  public CalculationController(MaterialService materialService) {
    this.materialService = materialService;
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
    RoomCalculationDTO roomsDTO = new RoomCalculationDTO(new ArrayList<>());
    for (int i = 0; i < numRooms; i++) {
      roomsDTO.addRoomCalculation(new RoomCalculation());
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
      model.addAttribute("roomsDTO", roomsDTO);
      return "calculation-page";
    }
    model.addAttribute("message", "No available materials.");
    return "homepage";
  }

  @PostMapping("/select-materials")
  public String getRoomDetails(
      @ModelAttribute("roomsDTO") RoomCalculationDTO roomsDTO, Model model) {
    return "homepage";
  }
}
