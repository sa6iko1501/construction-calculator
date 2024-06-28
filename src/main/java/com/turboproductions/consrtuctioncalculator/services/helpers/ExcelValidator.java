/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import java.io.IOException;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

public class ExcelValidator {
  private static final String BAD_CELL_ERR_MSG = "Bad cell at cell row '%s'";

  /**
   * Method to validate the file we imported to make sure it adheres to all the requirements for
   * importing.
   *
   * @param excelFile the imported {@link MultipartFile}.
   * @return String containing whatever is wrong with the file imported or {@code null} if the file
   *     passes all checks
   */
  public static String validateExcelDataTemplate(MultipartFile excelFile) {
    String errMsg = excelFile.isEmpty() ? "Empty file" : null;
    if (errMsg == null) {
      try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
        String fileName = excelFile.getOriginalFilename();
        errMsg =
            (fileName == null || fileName.isEmpty() || fileName.isBlank())
                ? "Error in import file"
                : null;
        if (errMsg == null) {
          errMsg = validateExcelFileType(fileName);
          if (errMsg == null) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
              if (row.getLastCellNum() != 2) {
                errMsg =
                    String.format(
                        "Problem with file '%s' in row number '%s'", fileName, row.getRowNum() + 1);
                break;
              }
              errMsg = validateExcelDataRow(row);
              if (errMsg != null) {
                break;
              }
            }
            return errMsg;
          }
        }
        return errMsg;
      } catch (IOException ex) {
        ex.printStackTrace();
        errMsg = String.format("Unsupported file type for '%s'", excelFile.getOriginalFilename());
        return errMsg;
      }
    }
    return errMsg;
  }

  private static String validateExcelFileType(String fileName) {
    return fileName.endsWith(".xls") || fileName.endsWith(".xlsx")
        ? null
        : String.format("Incorrect file format for file '%s'", fileName);
  }

  private static String validateExcelDataRow(Row row) {
    if (row.getCell(0).getCellType() != CellType.STRING) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(1).getCellType() != CellType.NUMERIC) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (row.getCell(0).getStringCellValue() == null) {
      return String.format(BAD_CELL_ERR_MSG, row.getRowNum() + 1);
    }
    if (!isValidBigDecimal(row.getCell(1).getNumericCellValue())) {
      return String.format(
          "Value '%s' invalid for price of item '%s' at row '%s'",
          row.getCell(1), row.getCell(0), row.getRowNum() + 1);
    }
    return null;
  }

  private static boolean isValidBigDecimal(double value) {
    BigDecimal bigDecimalValue = new BigDecimal(value);
    return !(bigDecimalValue.doubleValue() <= 0);
  }
}
