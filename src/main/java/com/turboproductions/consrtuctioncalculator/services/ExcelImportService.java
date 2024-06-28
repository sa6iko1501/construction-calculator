/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.MaterialDao;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.dto.ExcelImportResult;
import com.turboproductions.consrtuctioncalculator.models.dto.ImportedRow;
import com.turboproductions.consrtuctioncalculator.services.helpers.ExcelParser;
import com.turboproductions.consrtuctioncalculator.services.helpers.ExcelValidator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ExcelImportService {
  private final MaterialDao materialDao;

  public String handleExcelImport(MultipartFile excelFile) {
    String errMessage = ExcelValidator.validateExcelDataTemplate(excelFile);
    if (errMessage == null) {
      ExcelImportResult importResult = ExcelParser.parseExcelSheet(excelFile).orElse(null);
      if (importResult != null) {
        errMessage =
            saveAllMaterials(
                importResult.getRows().stream().map(this::toMaterial).collect(Collectors.toList()));
      }
    }
    return errMessage;
  }

  private String saveAllMaterials(List<Material> materials) {
    try {
      materialDao.saveAll(materials);
      return null;
    } catch (DataIntegrityViolationException ex) {
      ex.printStackTrace();
      return "Import names cannot contain any duplicates";
    }
  }

  private Material toMaterial(ImportedRow importedRow) {
    return new Material(importedRow.getName(), importedRow.getValue());
  }
}
