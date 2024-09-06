/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.turboproductions.consrtuctioncalculator.dao.ConstructionCalculationRepository;
import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.dao.RoomCalculationRepository;
import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.MaterialType;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.ConstructionActivityRequest;
import com.turboproductions.consrtuctioncalculator.services.helpers.RoomValidator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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

@ExtendWith(MockitoExtension.class)
public class CalculationServiceTest {
  @Mock private RoomValidator roomValidator;
  @Mock private MaterialRepository materialRepository;
  @Mock private ConstructionCalculationRepository calculationRepository;
  @Mock private RoomCalculationRepository roomCalculationRepository;
  @InjectMocks private CalculationService calculationService;
  private List<RoomCalculation> mockRooms;
  private ConstructionCalculation mockCalculation;
  private List<Material> mockMaterials;
  private User mockUser;
  private UUID mockUserId;

  @BeforeEach
  void setUp() {
    mockRooms =
        Arrays.asList(
            new RoomCalculation(
                mockCalculation, "Floor Tiles", 18.8, "Wallpaper", 72.6, "Ceiling Tile", 18.8),
            new RoomCalculation(
                mockCalculation, "Wooden Tiles", 12.4, "Red Paint", 58.8, "White Paint", 12.4),
            new RoomCalculation(
                mockCalculation, "Floor Tiles", 16.2, "Wallpaper", 72.6, "Ceiling Tile", 16.2));
    mockCalculation = new ConstructionCalculation();
    mockCalculation.setName("Test Calculation");
    mockMaterials =
        Arrays.asList(
            new Material("Floor Tiles", MaterialType.FLOOR, 4.12),
            new Material("Wooden Tiles", MaterialType.FLOOR, 6.8),
            new Material("Wallpaper", MaterialType.WALL, 5.2),
            new Material("Red Paint", MaterialType.WALL, 0.46),
            new Material("Ceiling Tile", MaterialType.CEILING, 3.99),
            new Material("White Paint", MaterialType.CEILING, 0.80));
    mockUser = new User();
    mockUserId = mockUser.getUserId();
  }

  @Test
  void handleConstructionCalculationCreationTest() {
    when(materialRepository.findAllByUserOrderByType(eq(mockUser))).thenReturn(mockMaterials);
    calculationService.handleConstructionCalculationCreation(mockCalculation, mockRooms, mockUser);
    // Assert first room prices and area (Desmos Scientific Calculator used to manually calculate
    // prices with test data)
    assertEquals(77.46, mockRooms.get(0).getFloorMaterialPrice());
    assertEquals(75.01, mockRooms.get(0).getCeilingMaterialPrice());
    assertEquals(377.52, mockRooms.get(0).getWallMaterialPrice());
    assertEquals(529.99, mockRooms.get(0).getRoomPrice());
    assertEquals(110.2, mockRooms.get(0).getRoomArea());

    // Assert calculation area, prices and number of rooms (Desmos scientific calculator used to
    // manually calculate prices with test data)
    assertEquals(3, mockCalculation.getNumberOfRooms());
    assertEquals(298.8, mockCalculation.getSquareMeters());
    assertEquals(1160.18, mockCalculation.getCalculationPrice());
  }

  @Test
  void updateRoomsAndCalculationsOnMaterialUpdateTest() {
    // Update material's prices
    mockMaterials.stream()
        .filter(x -> x.getName().equals("Floor Tiles"))
        .forEach(m -> m.setPricePerSqMeter(2.12));
    mockMaterials.stream()
        .filter(x -> x.getName().equals("Wallpaper"))
        .forEach(m -> m.setPricePerSqMeter(3.2));
    mockMaterials.stream()
        .filter(x -> x.getName().equals("Ceiling Tile"))
        .forEach(m -> m.setPricePerSqMeter(4.99));

    mockCalculation.setRoomCalculations(new HashSet<>(mockRooms));
    mockCalculation.setName("2 Bedroom apartment");
    mockRooms.forEach(x -> x.setConstructionCalculation(mockCalculation));
    // Manually set the second room up since it doesn't have materials that will be updated
    RoomCalculation roomCalculation = mockRooms.get(1);
    roomCalculation.setFloorMaterialPrice(84.32);
    roomCalculation.setWallMaterialPrice(27.05);
    roomCalculation.setCeilingMaterialPrice(9.92);
    roomCalculation.setRoomPrice(121.29);
    roomCalculation.setRoomArea(83.6);

    // Mock db calls
    when(roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndFloorMaterial(
            eq(mockUserId), eq("Floor Tiles")))
        .thenReturn(
            mockRooms.stream()
                .filter(x -> x.getFloorMaterial().equals("Floor Tiles"))
                .collect(Collectors.toList()));

    when(roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndWallMaterial(
            eq(mockUserId), eq("Wallpaper")))
        .thenReturn(
            mockRooms.stream().filter(x -> x.getWallMaterial().equals("Wallpaper")).toList());

    when(roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndCeilingMaterial(
            eq(mockUserId), eq("Ceiling Tile")))
        .thenReturn(
            mockRooms.stream().filter(x -> x.getCeilingMaterial().equals("Ceiling Tile")).toList());

    when(materialRepository.findAllByUserOrderByType(eq(mockUser))).thenReturn(mockMaterials);

    // Calls to method after updating materials
    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Floor Tiles"))
                .findAny()
                .orElse(null)),
        mockUser);

    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Wallpaper"))
                .findAny()
                .orElse(null)),
        mockUser);

    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Ceiling Tile"))
                .findAny()
                .orElse(null)),
        mockUser);

    // Assert prices were successfully updated for the first room(Desmos scientific calculator used
    // to manually calculate prices with test data)
    RoomCalculation updatedFirstRoom =
        mockRooms.stream()
            .filter(x -> x.getWallMaterial().equals("Wallpaper"))
            .findFirst()
            .orElse(null);
    assertEquals(232.32, updatedFirstRoom.getWallMaterialPrice());
    assertEquals(39.86, updatedFirstRoom.getFloorMaterialPrice());
    assertEquals(93.81, updatedFirstRoom.getCeilingMaterialPrice());
    assertEquals(365.99, updatedFirstRoom.getRoomPrice());

    // Assert prices were successfully updated for the whole Calculation(Desmos scientific
    // calculator used to manually calculate prices with test data)
    assertEquals(834.78, mockCalculation.getCalculationPrice());
  }

  @Test
  void handleExcelExportTest() throws IOException {
    // Calculate ConstructionCalculation properties
    when(materialRepository.findAllByUserOrderByType(eq(mockUser))).thenReturn(mockMaterials);
    calculationService.handleConstructionCalculationCreation(mockCalculation, mockRooms, mockUser);

    ByteArrayInputStream result = calculationService.handleExcelExport(mockCalculation);

    assertNotNull(result);
    try (Workbook workbook = new XSSFWorkbook(result)) {
      Sheet sheet = workbook.getSheet(mockCalculation.getName());
      assertNotNull(sheet);

      // Assert Construction Information row
      Row constructionDataRow = sheet.getRow(1);
      assertNotNull(constructionDataRow);
      assertEquals(mockCalculation.getName(), constructionDataRow.getCell(0).getStringCellValue());
      assertEquals(
          mockCalculation.getNumberOfRooms(), constructionDataRow.getCell(1).getNumericCellValue());
      assertEquals(
          mockCalculation.getSquareMeters(), constructionDataRow.getCell(2).getNumericCellValue());
      assertEquals(
          mockCalculation.getCalculationPrice(),
          constructionDataRow.getCell(3).getNumericCellValue());
      assertEquals(
          mockCalculation.getDate().toString().replace("T", " "),
          constructionDataRow.getCell(4).getStringCellValue());

      List<RoomCalculation> derivedFromSheet = translateDataRowsToRooms(4, 3, sheet);
      List<RoomCalculation> derivedFromCalculation =
          mockCalculation.getRoomCalculations().stream()
              .sorted(Comparator.comparing(RoomCalculation::getRoomNumber))
              .toList();

      // Assert sheet data matches the data in the ConstructionCalculation object
      RoomCalculation lastFromCalculation = derivedFromCalculation.getLast();
      RoomCalculation lastFromSheet = derivedFromSheet.getLast();
      assertEquals(lastFromCalculation.getRoomNumber(), lastFromSheet.getRoomNumber());
      assertEquals(lastFromCalculation.getFloorMaterial(), lastFromSheet.getFloorMaterial());
      assertEquals(lastFromCalculation.getWallMaterial(), lastFromSheet.getWallMaterial());
      assertEquals(lastFromCalculation.getCeilingMaterial(), lastFromSheet.getCeilingMaterial());
      assertEquals(lastFromCalculation.getFloorSqM(), lastFromSheet.getFloorSqM());
      assertEquals(lastFromCalculation.getWallSqM(), lastFromSheet.getWallSqM());
      assertEquals(lastFromCalculation.getCeilingSqM(), lastFromSheet.getCeilingSqM());
      assertEquals(
          lastFromCalculation.getFloorMaterialPrice(), lastFromSheet.getFloorMaterialPrice());
      assertEquals(
          lastFromCalculation.getWallMaterialPrice(), lastFromSheet.getWallMaterialPrice());
      assertEquals(
          lastFromCalculation.getCeilingMaterialPrice(), lastFromSheet.getCeilingMaterialPrice());
      assertEquals(lastFromCalculation.getRoomArea(), lastFromSheet.getRoomArea());
      assertEquals(lastFromCalculation.getRoomPrice(), lastFromSheet.getRoomPrice());
    }
  }

  @Test
  void setCalculationActivityTest() {
    ConstructionActivityRequest request =
        new ConstructionActivityRequest(mockCalculation.getCalculationId(), false);
    when(calculationRepository.findById(request.getConstructionId()))
        .thenReturn(Optional.of(mockCalculation));

    calculationService.setCalculationActivity(request);

    verify(calculationRepository, times(1)).save(eq(mockCalculation));
  }

  @Test
  void setCalculationActivityNullCalcTest() {
    ConstructionActivityRequest request =
        new ConstructionActivityRequest(mockCalculation.getCalculationId(), true);
    when(calculationRepository.findById(request.getConstructionId())).thenReturn(Optional.empty());

    assertEquals(
        String.format("No Calculation with the id '%s' found.", mockCalculation.getCalculationId()),
        calculationService.setCalculationActivity(request));

    // Make sure the db doesn't get called when no calc found
    verify(calculationRepository, times(0)).save(any());
  }

  @Test
  void getCalculationTest() {
    when(calculationRepository.findById(any(UUID.class)))
        .thenReturn(Optional.ofNullable(mockCalculation));
    assertNotNull(calculationService.getCalculation(mockCalculation.getCalculationId()));
  }

  @Test
  void getAllCalculationsTest() {
    when(calculationRepository.findConstructionCalculationsByUserOrderByDate(any(User.class)))
        .thenReturn(List.of(mockCalculation));
    assertNotNull(calculationService.getAllCalculations(new User()));
  }

  @Test
  void deleteAllCalculationsTest() {
    UUID calculationId = UUID.randomUUID();

    doNothing().when(calculationRepository).deleteById(any(UUID.class));
    calculationService.deleteCalculationById(calculationId);

    verify(calculationRepository, times(1)).deleteById(calculationId);
  }

  private List<RoomCalculation> translateDataRowsToRooms(
      int beginningRow, int numberOfRows, Sheet sheet) {
    List<RoomCalculation> roomsDerivedFromSheet = new ArrayList<>();
    for (int i = beginningRow; i < beginningRow + numberOfRows; i++) {
      Row roomRow = sheet.getRow(i);
      String roomName = roomRow.getCell(0).getStringCellValue();
      String floorMaterial = roomRow.getCell(1).getStringCellValue();
      String wallMaterial = roomRow.getCell(2).getStringCellValue();
      String ceilingMaterial = roomRow.getCell(3).getStringCellValue();
      double floorArea = roomRow.getCell(4).getNumericCellValue();
      double wallArea = roomRow.getCell(5).getNumericCellValue();
      double ceilingArea = roomRow.getCell(6).getNumericCellValue();
      double floorPrice = roomRow.getCell(7).getNumericCellValue();
      double wallPrice = roomRow.getCell(8).getNumericCellValue();
      double ceilingPrice = roomRow.getCell(9).getNumericCellValue();
      double totalArea = roomRow.getCell(10).getNumericCellValue();
      double totalPrice = roomRow.getCell(11).getNumericCellValue();
      RoomCalculation roomCalculation =
          new RoomCalculation(
              null, floorMaterial, floorArea, wallMaterial, wallArea, ceilingMaterial, ceilingArea);
      roomCalculation.setRoomNumber(roomName);
      roomCalculation.setFloorMaterialPrice(floorPrice);
      roomCalculation.setWallMaterialPrice(wallPrice);
      roomCalculation.setCeilingMaterialPrice(ceilingPrice);
      roomCalculation.setRoomArea(totalArea);
      roomCalculation.setRoomPrice(totalPrice);
      roomsDerivedFromSheet.add(roomCalculation);
    }
    return roomsDerivedFromSheet.stream()
        .sorted(Comparator.comparing(RoomCalculation::getRoomNumber))
        .collect(Collectors.toList());
  }
}
