/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.dto.ExcelImportResult;
import com.turboproductions.consrtuctioncalculator.models.dto.ImportedRow;
import com.turboproductions.consrtuctioncalculator.services.helpers.ExcelParser;
import com.turboproductions.consrtuctioncalculator.services.helpers.MaterialValidator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {
  @Mock private MaterialRepository materialRepository;
  @Mock private MaterialValidator materialValidator;
  @Mock private ExcelParser excelParser;

  @InjectMocks private MaterialService materialService;

  private List<Material> mockMaterials;

  @BeforeEach
  public void setUp() {
    mockMaterials =
        Arrays.asList(
            new Material("Material1", MaterialType.WALL, 10.0),
            new Material("Material2", MaterialType.FLOOR, 20.0));
  }

  @Test
  void testHandleExcelExport() throws IOException {
    ByteArrayInputStream result = materialService.handleExcelExport(mockMaterials);

    assertNotNull(result);
    try (Workbook workbook = new XSSFWorkbook(result)) {
      Sheet sheet = workbook.getSheet("Materials");
      assertNotNull(sheet);

      Row row0 = sheet.getRow(0);
      assertNotNull(row0);
      assertEquals("Material1", row0.getCell(0).getStringCellValue());
      assertEquals("WALL", row0.getCell(1).getStringCellValue());
      assertEquals(10.0, row0.getCell(2).getNumericCellValue());

      Row row1 = sheet.getRow(1);
      assertNotNull(row1);
      assertEquals("Material2", row1.getCell(0).getStringCellValue());
      assertEquals("FLOOR", row1.getCell(1).getStringCellValue());
      assertEquals(20.0, row1.getCell(2).getNumericCellValue());
    }
  }

  @Test
  void testGetTemplate() throws IOException {
    ByteArrayInputStream result = materialService.getTemplate();

    assertNotNull(result);
    try (Workbook workbook = new XSSFWorkbook(result)) {
      Sheet sheet = workbook.getSheet("Template for importing");
      assertNotNull(sheet);

      Row firstRow = sheet.getRow(0);
      assertNotNull(firstRow);
      assertEquals("Blue Paint", firstRow.getCell(0).getStringCellValue());
      assertEquals("WALL", firstRow.getCell(1).getStringCellValue());
      assertEquals(0.40, firstRow.getCell(2).getNumericCellValue());

      Row lastRow = sheet.getRow(5);
      assertNotNull(lastRow);
      assertEquals("DryWall", lastRow.getCell(0).getStringCellValue());
      assertEquals("WALL", lastRow.getCell(1).getStringCellValue());
      assertEquals(4.20, lastRow.getCell(2).getNumericCellValue());
    }
  }

  @Test
  void testGetMaterial() {
    UUID materialId = UUID.randomUUID();
    Material material = new Material("Material1", MaterialType.WALL, 10.0);
    material.setMaterialId(materialId);

    when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

    Material result = materialService.getMaterial(materialId);

    assertNotNull(result);
    assertEquals(material, result);
  }

  @Test
  void testDeleteMaterialById() {
    UUID materialId = UUID.randomUUID();

    doNothing().when(materialRepository).deleteById(materialId);

    materialService.deleteMaterialById(materialId);

    verify(materialRepository, times(1)).deleteById(materialId);
  }

  @Test
  void testGetAllMaterials() {
    when(materialRepository.findAll()).thenReturn(mockMaterials);

    List<Material> result = materialService.getAllMaterials();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(mockMaterials, result);
  }

  @Test
  void testHandleExcelImport() {
    List<ImportedRow> rows =
        List.of(
            new ImportedRow("Red Paint", "WALL", 11.49),
            new ImportedRow("Wood Tiles", "FLOOR", 8.99),
            new ImportedRow("Suspended Ceiling Tiles", "CEILING", 15.99));
    ExcelImportResult excelImportResult = new ExcelImportResult(rows);
    MultipartFile multipartMock = mock(MultipartFile.class);
    when(materialValidator.validateExcelDataTemplate(any(MultipartFile.class))).thenReturn(null);
    when(excelParser.parseExcelSheet(any(MultipartFile.class)))
        .thenReturn(Optional.of(excelImportResult));
    String result = materialService.handleExcelImport(multipartMock);
    assertNull(result);
  }

  @Test
  void testHandleCreateMaterial() {
    Material material = new Material("Red Paint", MaterialType.WALL, 11.49);
    assertNull(materialService.handleCreateMaterial(material));
  }

  @Test
  void testHandleUpdateMaterial() {
    Material material = new Material("Red Paint", MaterialType.WALL, 11.49);
    doReturn(Optional.of(material)).when(materialRepository).findById(material.getMaterialId());
    assertNull(materialService.handleUpdateMaterial(material));
  }
}
