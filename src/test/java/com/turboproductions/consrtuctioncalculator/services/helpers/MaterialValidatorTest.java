/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MaterialValidatorTest {
  private static final String INVALID_NAME = "Invalid value for name.";
  private static final String INVALID_PRICE = "Value '%s' invalid for price of item '%s'.";
  private static final String BAD_CELL_ERR_MSG = "Bad cell at cell row '%s'.";
  private static final String INVALID_TYPE =
      "Value '%s' at row '%s' is an invalid Material type. Types can be FLOOR, WALL and CEILING.";
  private static final String INVALID_PRICE_ROW =
      "Value '%s' invalid for price of item '%s' at row '%s'.";
  private final MaterialValidator materialValidator = new MaterialValidator();

  @Test
  void validateMaterialPropertiesName() {
    Material material = new Material("", MaterialType.FLOOR, 2.23);
    // Empty name
    assertEquals(
        INVALID_NAME,
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Null name
    material.setName(null);
    assertEquals(
        INVALID_NAME,
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Blank name
    material.setName("  ");
    assertEquals(
        INVALID_NAME,
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Valid name
    material.setName("Red Paint");
    assertNull(
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));
  }

  @Test
  void validateMaterialPropertiesPrice() {
    Material material = new Material("Some material", MaterialType.CEILING, -2);

    // Negative price
    assertEquals(
        String.format(INVALID_PRICE, material.getPricePerSqMeter(), material.getName()),
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Price of zero
    material.setPricePerSqMeter(0);
    assertEquals(
        String.format(INVALID_PRICE, material.getPricePerSqMeter(), material.getName()),
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Valid price
    material.setPricePerSqMeter(1.99);
    assertNull(
        materialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));
  }

  @Test
  void validateEmptyExcelFile() {
    MultipartFile emptyFile =
        new MockMultipartFile(
            "file",
            "empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]);
    assertEquals("Empty file.", materialValidator.validateExcelDataTemplate(emptyFile));
  }

  @Test
  void validateNullFileName() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    workbook.write(output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    workbook.close();
    output.close();

    MultipartFile nullFileName =
        new MockMultipartFile(
            "file",
            null,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            input);
    assertEquals(
        "Error in import file.", materialValidator.validateExcelDataTemplate(nullFileName));
  }

  @Test
  void validateEmptyFileName() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    workbook.write(output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    workbook.close();
    output.close();

    MultipartFile emptyFileName =
        new MockMultipartFile(
            "file",
            null,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            input);
    assertEquals(
        "Error in import file.", materialValidator.validateExcelDataTemplate(emptyFileName));
  }

  @Test
  void validateWrongFileType() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    workbook.write(output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    workbook.close();
    output.close();

    MultipartFile wrongFileType =
        new MockMultipartFile(
            "file",
            "file.bat",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            input);
    assertEquals(
        String.format("Incorrect file format for file '%s'.", wrongFileType.getOriginalFilename()),
        materialValidator.validateExcelDataTemplate(wrongFileType));
  }

  @Test
  void validateWrongColumnCount() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("WOOD TILE");
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue("12.3");
    row.createCell(3, CellType.STRING).setCellValue("EXTRA CELL");

    MultipartFile wrongColumnNumbers = createTestFile(workbook);
    assertEquals(
        String.format(
            "Problem with file '%s' in row number '%s'.",
            wrongColumnNumbers.getOriginalFilename(), 1),
        materialValidator.validateExcelDataTemplate(wrongColumnNumbers));
  }

  @Test
  void validateExcelDataRowWithBlankName() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue((String) null);
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue(12.3);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithBlankType() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue((String) null);
    row.createCell(2, CellType.NUMERIC).setCellValue(12.3);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithBlankPrice() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue((String) null);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithEmptyName() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("");
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue(12);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithWhitespaceName() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue(" ");
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue(12);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithEmptyType() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Name");
    row.createCell(1, CellType.STRING).setCellValue("");
    row.createCell(2, CellType.NUMERIC).setCellValue(12);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithWhitespaceType() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue(" ");
    row.createCell(2, CellType.NUMERIC).setCellValue(12);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(BAD_CELL_ERR_MSG, 1), materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithWrongType() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue("WRONG TYPE");
    row.createCell(2, CellType.NUMERIC).setCellValue(12);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(INVALID_TYPE, row.getCell(1).getStringCellValue(), 1),
        materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithInvalidPrice() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue("CEILING");
    row.createCell(2, CellType.NUMERIC).setCellValue(-0.1);
    MultipartFile file = createTestFile(workbook);
    assertEquals(
        String.format(
            INVALID_PRICE_ROW,
            row.getCell(2).getNumericCellValue(),
            row.getCell(0).getStringCellValue(),
            1),
        materialValidator.validateExcelDataTemplate(file));
  }

  @Test
  void validateExcelDataRowWithValidValues() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("Some name");
    row.createCell(1, CellType.STRING).setCellValue("CEILING");
    row.createCell(2, CellType.NUMERIC).setCellValue(12.3);
    MultipartFile file = createTestFile(workbook);
    assertNull(materialValidator.validateExcelDataTemplate(file));
  }

  public static MultipartFile createTestFile(Workbook workbook) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    workbook.write(output);
    workbook.close();
    output.close();
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    return new MockMultipartFile(
        "file",
        "file.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        input);
  }
}
