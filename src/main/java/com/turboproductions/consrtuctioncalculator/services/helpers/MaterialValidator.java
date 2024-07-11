/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

public class MaterialValidator {
  private static final String BAD_CELL_ERR_MSG = "Bad cell at cell row '%s'";

  /**
   * Method to validate the file that was imported to make sure it adheres to all the requirements
   * for importing.
   *
   * @param excelFile the imported {@link MultipartFile}.
   * @return String containing whatever is wrong with the file imported or {@code null} if the file
   *     passes all checks
   */
  public static String validateExcelDataTemplate(MultipartFile excelFile) {
    if (excelFile.isEmpty()) {
      return "Empty file";
    }

    String fileName = excelFile.getOriginalFilename();
    if (fileName == null || fileName.trim().isEmpty()) {
      return "Error in import file";
    }

    String errMsg = validateExcelFileType(fileName);
    if (errMsg != null) {
      return errMsg;
    }

    try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        if (row.getLastCellNum() != 3) {
          return String.format(
              "Problem with file '%s' in row number '%s'", fileName, row.getRowNum() + 1);
        }
        errMsg = validateExcelDataRow(row);
        if (errMsg != null) {
          return errMsg;
        }
      }
    } catch (IOException ex) {
      return String.format("Unsupported file type for '%s'", fileName);
    }
    return null;
  }

  /**
   * Method to validate user input
   *
   * @param name the Material name
   * @param pricePerSqM the price per square meter
   * @return
   */
  public static String validateMaterialProperties(String name, double pricePerSqM) {
    if (name == null || name.isBlank() || name.isEmpty()) {
      return "Invalid value for name";
    }
    if (!isValidBigDecimal(pricePerSqM)) {
      return String.format(
          "Value '%s' invalid for price of item '%s'", String.valueOf(pricePerSqM), name);
    }
    return null;
  }

  private static String validateExcelFileType(String fileName) {
    return fileName.endsWith(".xls") || fileName.endsWith(".xlsx")
        ? null
        : String.format("Incorrect file format for file '%s'", fileName);
  }

  private static String validateExcelDataRow(Row row) {
    if (row.getCell(0) == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(1) == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(2) == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(0).getCellType() != CellType.STRING) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(1).getCellType() != CellType.STRING) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(2).getCellType() != CellType.NUMERIC) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(0).getStringCellValue() == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(1).getStringCellValue() == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (!isValidMaterialType(row.getCell(1).getStringCellValue())) {
      return String.format(
          "Value '%s' at row '%s' is an invalid Material type. Types can be FLOOR, WALL and CEILING",
          row.getCell(1).getStringCellValue(), row.getRowNum() + 1);
    }
    if (!isValidBigDecimal(row.getCell(2).getNumericCellValue())) {
      return String.format(
          "Value '%s' invalid for price of item '%s' at row '%s'",
          row.getCell(2), row.getCell(0), row.getRowNum() + 1);
    }
    return null;
  }

  private static boolean isValidMaterialType(String value) {
    if (value.equals(MaterialType.WALL.toString())
        || value.equals(MaterialType.FLOOR.toString())
        || value.equals(MaterialType.CEILING.toString())) {
      return true;
    }
    return false;
  }

  private static boolean isValidBigDecimal(double value) {
    BigDecimal bigDecimalValue = BigDecimal.valueOf(value);
    return bigDecimalValue.doubleValue() >= 0;
  }
}
