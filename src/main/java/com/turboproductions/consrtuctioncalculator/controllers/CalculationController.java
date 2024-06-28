/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.services.ExcelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/construction")
public class CalculationController {
  @Autowired ExcelImportService excelImportService;

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
    if (errMsg != null) {
      model.addAttribute("message", errMsg);
    }
    return "import-data-page";
  }
}
