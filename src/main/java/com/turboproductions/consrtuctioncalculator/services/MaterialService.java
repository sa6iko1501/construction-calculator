/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.dto.ExcelImportResult;
import com.turboproductions.consrtuctioncalculator.models.dto.ImportedRow;
import com.turboproductions.consrtuctioncalculator.services.helpers.ExcelParser;
import com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class MaterialService {
  private final MaterialRepository materialRepository;

  public ByteArrayInputStream handleExcelExport(List<Material> materials) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Materials");
      int rowNum = 0;
      for (Material material : materials) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0, CellType.STRING).setCellValue(material.getName());
        row.createCell(1, CellType.NUMERIC).setCellValue(material.getPricePerSqMeter());
      }
      workbook.write(outputStream);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (IOException ex) {
      return null;
    }
  }

  public ByteArrayInputStream getTemplate() {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Template for importing");
      List<Material> materials = loadDataForTemplate();
      int rowNum = 0;
      for (Material material : materials) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0, CellType.STRING).setCellValue(material.getName());
        row.createCell(1, CellType.NUMERIC).setCellValue(material.getPricePerSqMeter());
      }
      workbook.write(outputStream);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (IOException ex) {
      return null;
    }
  }

  public String handleExcelImport(MultipartFile excelFile) {
    String errMessage = MaterialValidator.validateExcelDataTemplate(excelFile);
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
      materialRepository.saveAll(materials);
      return null;
    } catch (DataIntegrityViolationException ex) {
      ex.printStackTrace();
      return "Import names cannot contain any duplicates";
    }
  }

  public void deleteMaterialById(UUID id) {
    if (id != null) {
      materialRepository.deleteById(id);
    }
  }

  public List<Material> getAllMaterials() {
    return materialRepository.findAll();
  }

  public Material getMaterial(UUID id) {
    Optional<Material> material = materialRepository.findById(id);
    return material.orElse(null);
  }

  public String handleUpdateMaterial(Material material) {
    Material toBeUpdated = getMaterial(material.getMaterialId());
    if (toBeUpdated != null) {
      String errMsg =
          MaterialValidator.validateMaterialProperties(
              material.getName(), material.getPricePerSqMeter());
      if (errMsg == null) {
        materialRepository.save(material);
      }
      return errMsg;
    }
    return "Error with Material";
  }

  public String handleCreateMaterial(Material material) {
    if (material != null) {
      String msg =
          MaterialValidator.validateMaterialProperties(
              material.getName(), material.getPricePerSqMeter());
      if (msg == null) {
        materialRepository.save(material);
      }
      return msg;
    }
    return "Error with Material";
  }

  private List<Material> loadDataForTemplate() {
    return List.of(
        new Material("Blue Paint", 0.40),
        new Material("Red Paint", 0.42),
        new Material("Tiles", 6.50),
        new Material("Wooden Tiles", 9.69),
        new Material("Wallpaper", 8.20),
        new Material("DryWall", 4.20));
  }

  private Material toMaterial(ImportedRow importedRow) {
    return new Material(importedRow.getName(), importedRow.getValue());
  }
}
