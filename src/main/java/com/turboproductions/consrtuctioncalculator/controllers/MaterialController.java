/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.services.MaterialService;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/material")
public class MaterialController {
  private final MaterialService materialService;

  public MaterialController(MaterialService materialService) {
    this.materialService = materialService;
  }

  @GetMapping("/import")
  String getImportPage() {
    return "import-data-page";
  }

  @GetMapping("/export")
  ResponseEntity<InputStreamResource> exportMaterials(
      @AuthenticationPrincipal User authenticatedUser) {
    List<Material> materials = materialService.getAllMaterials(authenticatedUser);
    ByteArrayInputStream inputStream = materialService.handleExcelExport(materials);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=materials.xlsx");
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(new InputStreamResource(inputStream));
  }

  @GetMapping("/materials")
  String getMaterialsPage(Model model, @AuthenticationPrincipal User authenticatedUser) {
    List<Material> materials = materialService.getAllMaterials(authenticatedUser);
    if (!materials.isEmpty()) {
      model.addAttribute("materials", materials);
    }
    return "materials-page";
  }

  @PostMapping("/import")
  String importData(
      RedirectAttributes model,
      MultipartFile excelFile,
      @AuthenticationPrincipal User authenticatedUser) {
    String errMsg = materialService.handleExcelImport(excelFile, authenticatedUser);
    model.addFlashAttribute("message", errMsg == null ? "Import Successful!" : errMsg);
    return "redirect:/home";
  }

  @GetMapping("/template")
  ResponseEntity<InputStreamResource> getTemplate() {
    ByteArrayInputStream inputStream = materialService.getTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=materials.xlsx");
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(new InputStreamResource(inputStream));
  }

  @PostMapping("/delete/{id}")
  String deleteMaterial(
      RedirectAttributes model,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User authenticatedUser) {
    materialService.deleteMaterialById(id);
    List<Material> materials = materialService.getAllMaterials(authenticatedUser);
    if (!materials.isEmpty()) {
      model.addFlashAttribute("message", "Material successfully deleted");
      return "redirect:/material/materials";
    } else {
      model.addAttribute("message", "You currently have no materials loaded");
      return "homepage";
    }
  }

  @GetMapping("/update/{id}")
  String getUpdateMaterialPage(Model model, @PathVariable("id") UUID id) {
    Material material = materialService.getMaterial(id);
    if (material != null) {
      model.addAttribute("material", material);
      model.addAttribute("materialTypes", MaterialType.values());
      return "update-material-page";
    }
    model.addAttribute("message", "Invalid material.");
    return "homepage";
  }

  @PostMapping("/update")
  String updateMaterial(
      RedirectAttributes model,
      @ModelAttribute("material") Material material,
      @AuthenticationPrincipal User authenticatedUser) {
    String msg = materialService.handleUpdateMaterial(material, authenticatedUser);
    if (msg != null) {
      model.addFlashAttribute("message", msg);
    }
    return "redirect:/material/materials";
  }

  @GetMapping("/create")
  String getMaterialCreationPage(Model model) {
    Material material = new Material("", null, 0);
    model.addAttribute("material", material);
    model.addAttribute("materialTypes", MaterialType.values());
    return "create-material-page";
  }

  @PostMapping("/create")
  String createMaterial(
      RedirectAttributes model,
      @ModelAttribute("material") Material material,
      @AuthenticationPrincipal User authenticatedUser) {
    String errMsg = materialService.handleCreateMaterial(material, authenticatedUser);
    if (errMsg != null) {
      model.addFlashAttribute("message", errMsg);
    }
    return "redirect:/material/materials";
  }
}
