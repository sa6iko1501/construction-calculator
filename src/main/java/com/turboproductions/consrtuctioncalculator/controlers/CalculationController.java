/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controlers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/construction/")
public class CalculationController {
  @GetMapping("calculation")
  String getCalculationPage(Model model) {
    return "calculation-page";
  }

  @GetMapping("import")
  String getImportPage(Model model) {
    return "import-data-page";
  }
}
