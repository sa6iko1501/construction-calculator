/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.helpers;

import static com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidator.validateExcelDataTemplate;
import static com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidator.validateMaterialProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidator;
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

public class MaterialValidatorTest {
  private static final String INVALID_NAME = "Invalid value for name.";
  private static final String INVALID_PRICE = "Value '%s' invalid for price of item '%s'";

  @Test
  void validateMaterialPropertiesName() {
    Material material = new Material("", MaterialType.FLOOR, 2.23);
    // Empty name
    assertEquals(
        INVALID_NAME,
        MaterialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Null name
    material.setName(null);
    assertEquals(
        INVALID_NAME,
        MaterialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Blank name
    material.setName("  ");
    assertEquals(
        INVALID_NAME,
        MaterialValidator.validateMaterialProperties(
            material.getName(), material.getPricePerSqMeter()));

    // Valid name
    material.setName("Red Paint");
    assertNull(validateMaterialProperties(material.getName(), material.getPricePerSqMeter()));
  }

  @Test
  void validateMaterialPropertiesPrice() {
    Material material = new Material("Some material", MaterialType.CEILING, -2);

    // Negative price
    assertEquals(
        String.format(INVALID_PRICE, material.getPricePerSqMeter(), material.getName()),
        validateMaterialProperties(material.getName(), material.getPricePerSqMeter()));

    // Price of zero
    material.setPricePerSqMeter(0);
    assertEquals(
        String.format(INVALID_PRICE, material.getPricePerSqMeter(), material.getName()),
        validateMaterialProperties(material.getName(), material.getPricePerSqMeter()));

    // Valid price
    material.setPricePerSqMeter(1.99);
    assertNull(validateMaterialProperties(material.getName(), material.getPricePerSqMeter()));
  }

  @Test
  void validateEmptyExcelFile() {
    MultipartFile emptyFile =
        new MockMultipartFile(
            "file",
            "empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]);
    assertEquals("Empty file.", validateExcelDataTemplate(emptyFile));
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
    assertEquals("Error in import file.", validateExcelDataTemplate(nullFileName));
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
    assertEquals("Error in import file.", validateExcelDataTemplate(emptyFileName));
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
    assertEquals(String.format("Incorrect file format for file '%s'", wrongFileType.getOriginalFilename()), validateExcelDataTemplate(wrongFileType));
  }

  @Test
  void validateWrongColumnCount() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row row = sheet.createRow(0);
    row.createCell(0, CellType.STRING).setCellValue("WOOD TILE");
    row.createCell(1, CellType.STRING).setCellValue("FLOOR");
    row.createCell(2, CellType.NUMERIC).setCellValue("12.3");
    row.createCell(3, CellType.STRING).setCellValue("EXTRA CELL");

    workbook.write(output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    workbook.close();
    output.close();

    MultipartFile wrongColumnNumbers =
            new MockMultipartFile(
                    "file",
                    "file.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    input);
    assertEquals(String.format("Problem with file '%s' in row number '%s'", wrongColumnNumbers.getOriginalFilename(), 1), validateExcelDataTemplate(wrongColumnNumbers));
  }

}
