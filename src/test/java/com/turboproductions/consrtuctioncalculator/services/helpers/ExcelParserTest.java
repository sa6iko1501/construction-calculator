/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import static com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidatorTest.createTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.turboproductions.consrtuctioncalculator.models.dto.ExcelImportResult;
import com.turboproductions.consrtuctioncalculator.models.dto.ImportedRow;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class ExcelParserTest {
  private final ExcelParser excelParser = new ExcelParser();

  @Test
  void testParseExcelSheet() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row1 = sheet.createRow(0);
    row1.createCell(0, CellType.STRING).setCellValue("Ceramic tile");
    row1.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row1.createCell(2, CellType.NUMERIC).setCellValue(12.69);
    Row row2 = sheet.createRow(1);
    row2.createCell(0, CellType.STRING).setCellValue("White paint");
    row2.createCell(1, CellType.STRING).setCellValue("WALL");
    row2.createCell(2, CellType.NUMERIC).setCellValue(8.50);
    MultipartFile file = createTestFile(workbook);
    Optional<ExcelImportResult> excelImportResult = excelParser.parseExcelSheet(file);
    assert (excelImportResult.isPresent());
    List<ImportedRow> parsedResults = excelImportResult.get().getRows();
    assertEquals(parsedResults.getFirst().getName(), row1.getCell(0).getStringCellValue());
    assertEquals(parsedResults.getFirst().getType(), row1.getCell(1).getStringCellValue());
    assertEquals(parsedResults.getFirst().getValue(), row1.getCell(2).getNumericCellValue());
    assertEquals(parsedResults.getLast().getName(), row2.getCell(0).getStringCellValue());
    assertEquals(parsedResults.getLast().getType(), row2.getCell(1).getStringCellValue());
    assertEquals(parsedResults.getLast().getValue(), row2.getCell(2).getNumericCellValue());
  }
}
