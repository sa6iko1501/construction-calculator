/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import com.turboproductions.consrtuctioncalculator.models.dto.ExcelImportResult;
import com.turboproductions.consrtuctioncalculator.models.dto.ImportedRow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelParser {
  public Optional<ExcelImportResult> parseExcelSheet(MultipartFile excelFile) {
    try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
      List<ImportedRow> results = new ArrayList<>();
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        ImportedRow importedRow =
            new ImportedRow(
                row.getCell(0).getStringCellValue(),
                row.getCell(1).getStringCellValue(),
                row.getCell(2).getNumericCellValue());
        results.add(importedRow);
      }
      return Optional.of(new ExcelImportResult(results));
    } catch (IOException ex) {
      return Optional.empty();
    }
  }
}
