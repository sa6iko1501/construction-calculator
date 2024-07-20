/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.services.MaterialService;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/material")
public class MaterialController {
  private final MaterialService materialService;

  public MaterialController(MaterialService materialService) {
    this.materialService = materialService;
  }

  @GetMapping("/import")
  String getImportPage(Model model) {
    return "import-data-page";
  }

  @GetMapping("/export")
  ResponseEntity<InputStreamResource> exportMaterials(Model model) {
    List<Material> materials = materialService.getAllMaterials();
    ByteArrayInputStream inputStream = materialService.handleExcelExport(materials);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=materials.xlsx");
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(new InputStreamResource(inputStream));
  }

  @GetMapping()
  String getMaterialsPage(Model model) {
    List<Material> materials = materialService.getAllMaterials();
    if (!materials.isEmpty()) {
      model.addAttribute("materials", materials);
    }
    return "materials-page";
  }

  @PostMapping("/import")
  String importData(Model model, MultipartFile excelFile) {
    String errMsg = materialService.handleExcelImport(excelFile);
    model.addAttribute("message", errMsg == null ? "Import Successful!" : errMsg);
    return "import-data-page";
  }

  @GetMapping("/template")
  ResponseEntity<InputStreamResource> getTemplate(Model model) {
    ByteArrayInputStream inputStream = materialService.getTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=materials.xlsx");
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(new InputStreamResource(inputStream));
  }

  @PostMapping("/delete/{id}")
  String deleteMaterial(Model model, @PathVariable("id") UUID id) {
    materialService.deleteMaterialById(id);
    List<Material> materials = materialService.getAllMaterials();
    if (!materials.isEmpty()) {
      model.addAttribute("materials", materials);
      model.addAttribute("message", "Material successfully deleted");
      return "materials-page";
    } else {
      model.addAttribute("message", "You currently have no materials loaded");
      return "homepage";
    }
  }

  @GetMapping("/update/{id}")
  String getUpdateMaterialPage(Model model, @PathVariable("id") UUID id) {
    if (id != null) {
      Material material = materialService.getMaterial(id);
      if (material != null) {
        model.addAttribute("material", material);
        model.addAttribute("materialTypes", MaterialType.values());
        return "update-material-page";
      }
      model.addAttribute("message", "Invalid material.");
      return "homepage";
    }
    model.addAttribute("message", "Invalid material.");
    return "homepage";
  }

  @PostMapping("/update")
  String updateMaterial(Model model, @ModelAttribute("material") Material material) {
    String msg = materialService.handleUpdateMaterial(material);
    if (msg != null) {
      model.addAttribute("message", msg);
    }
    return getMaterialsPage(model);
  }

  @GetMapping("/create")
  String getMaterialCreationPage(Model model) {
    Material material = new Material("", null, 0);
    model.addAttribute("material", material);
    model.addAttribute("materialTypes", MaterialType.values());
    return "create-material-page";
  }

  @PostMapping("/create")
  String createMaterial(Model model, @ModelAttribute("material") Material material) {
    String errMsg = materialService.handleCreateMaterial(material);
    if (errMsg != null) {
      model.addAttribute("message", errMsg);
    }
    return getMaterialsPage(model);
  }
}
