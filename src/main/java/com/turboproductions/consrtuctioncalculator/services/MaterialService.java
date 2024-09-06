/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.User;
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
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class MaterialService {
  private final MaterialRepository materialRepository;
  private final CalculationService calculationService;
  private final MaterialValidator materialValidator;
  private final ExcelParser excelParser;

  public ByteArrayInputStream handleExcelExport(List<Material> materials) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Materials");
      int rowNum = 0;
      for (Material material : materials) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0, CellType.STRING).setCellValue(material.getName());
        row.createCell(1, CellType.STRING).setCellValue(material.getType().toString());
        row.createCell(2, CellType.NUMERIC).setCellValue(material.getPricePerSqMeter());
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
        row.createCell(1, CellType.STRING).setCellValue(material.getType().toString());
        row.createCell(2, CellType.NUMERIC).setCellValue(material.getPricePerSqMeter());
      }
      workbook.write(outputStream);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (IOException ex) {
      return null;
    }
  }

  public String handleExcelImport(MultipartFile excelFile, User user) {
    String errMessage = materialValidator.validateExcelDataTemplate(excelFile);
    if (errMessage == null) {
      ExcelImportResult importResult = excelParser.parseExcelSheet(excelFile).orElse(null);
      if (importResult != null) {
        List<Material> materialsToBeSaved =
            importResult.getRows().stream().map(this::toMaterial).toList();
        materialsToBeSaved.forEach(x -> x.setUser(user));
        errMessage = saveAllMaterials(materialsToBeSaved);
      }
    }
    return errMessage;
  }

  public String handleUpdateMaterial(Material material, User user) {
    Material toBeUpdated = getMaterial(material.getMaterialId());
    if (toBeUpdated != null) {
      String errMsg =
          materialValidator.validateMaterialProperties(
              material.getName(), material.getPricePerSqMeter());
      if (errMsg == null) {
        material.setUser(user);
        errMsg = saveMaterial(material);
        calculationService.updateRoomsAndCalculationsOnMaterialUpdate(material, user);
      }
      return errMsg;
    }
    return "Error with Material";
  }

  public String handleCreateMaterial(Material material, User user) {
    if (material != null) {
      String msg =
          materialValidator.validateMaterialProperties(
              material.getName(), material.getPricePerSqMeter());
      if (msg == null) {
        material.setUser(user);
        msg = saveMaterial(material);
      }
      return msg;
    }
    return "Error with Material";
  }

  public Material getMaterial(UUID id) {
    Optional<Material> material = materialRepository.findById(id);
    return material.orElse(null);
  }

  public void deleteMaterialById(UUID id) {
    if (id != null) {
      materialRepository.deleteById(id);
    }
  }

  public List<Material> getAllMaterials(User user) {
    return materialRepository.findAllByUserOrderByType(user);
  }

  public List<Material> filterByType(List<Material> materials, MaterialType materialType) {
    return materials.stream().filter(m -> m.getType() == materialType).toList();
  }

  private String saveAllMaterials(List<Material> materials) {
    try {
      materialRepository.saveAll(materials);
      return null;
    } catch (DataIntegrityViolationException ex) {
      return "Import names cannot contain any duplicates.";
    }
  }

  private String saveMaterial(Material material) {
    try {
      materialRepository.save(material);
      return null;
    } catch (DataIntegrityViolationException ex) {
      return String.format("A material with name '%s' already exists.", material.getName());
    }
  }

  private List<Material> loadDataForTemplate() {
    return List.of(
        new Material("Blue Paint", MaterialType.WALL, 0.40),
        new Material("Red Paint", MaterialType.WALL, 0.42),
        new Material("Ceramic Tiles", MaterialType.FLOOR, 6.50),
        new Material("Wooden Tiles", MaterialType.FLOOR, 9.69),
        new Material("Wallpaper", MaterialType.WALL, 8.20),
        new Material("Drywall", MaterialType.WALL, 4.20),
        new Material("Ceiling Tile", MaterialType.CEILING, 6.60),
        new Material("Hanged Ceiling", MaterialType.CEILING, 7.17));
  }

  private Material toMaterial(ImportedRow importedRow) {
    MaterialType type = null;
    switch (importedRow.getType()) {
      case "WALL":
        {
          type = MaterialType.WALL;
          break;
        }
      case "FLOOR":
        {
          type = MaterialType.FLOOR;
          break;
        }
      case "CEILING":
        {
          type = MaterialType.CEILING;
          break;
        }
    }
    if (type != null) {
      return new Material(importedRow.getName(), type, importedRow.getValue());
    } else {
      throw new IllegalArgumentException("MaterialType cannot be `null` but was `null`");
    }
  }
}
