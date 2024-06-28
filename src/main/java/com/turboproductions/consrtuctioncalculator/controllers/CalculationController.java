/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.services.ExcelImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/construction")
public class CalculationController {
  private final ExcelImportService excelImportService;

  public CalculationController(ExcelImportService excelImportService) {
    this.excelImportService = excelImportService;
  }

  @GetMapping("/calculation")
  String getCalculationPage(Model model) {
    return "calculation-page";
  }

  @GetMapping("/import")
  String getImportPage(Model model) {
    return "import-data-page";
  }

  @PostMapping("/import")
  String importData(Model model, MultipartFile excelFile) {
    String errMsg = excelImportService.handleExcelImport(excelFile);
    model.addAttribute("message", errMsg == null ? "Import Successful!" : errMsg);

    return "import-data-page";
  }
}
