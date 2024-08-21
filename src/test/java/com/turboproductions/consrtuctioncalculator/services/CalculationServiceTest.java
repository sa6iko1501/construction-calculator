/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import com.turboproductions.consrtuctioncalculator.services.helpers.RoomValidator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
  }

  @Test
  void handleConstructionCalculationCreationTest() {
    when(materialRepository.findAll()).thenReturn(mockMaterials);
    calculationService.handleConstructionCalculationCreation(
        mockCalculation, mockRooms, new User());
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
    mockRooms.forEach(x -> x.setConstructionCalculation(mockCalculation));
    // Manually set the second room up since it doesn't have materials that will be updated
    RoomCalculation roomCalculation = mockRooms.get(1);
    roomCalculation.setFloorMaterialPrice(84.32);
    roomCalculation.setWallMaterialPrice(27.05);
    roomCalculation.setCeilingMaterialPrice(9.92);
    roomCalculation.setRoomPrice(121.29);
    roomCalculation.setRoomArea(83.6);

    // Mock db calls
    when(roomCalculationRepository.findRoomCalculationsByFloorMaterial(any(String.class)))
        .thenReturn(
            mockRooms.stream()
                .filter(x -> x.getFloorMaterial().equals("Floor Tiles"))
                .collect(Collectors.toList()));

    when(roomCalculationRepository.findRoomCalculationsByWallMaterial(any(String.class)))
        .thenReturn(
            mockRooms.stream().filter(x -> x.getWallMaterial().equals("Wallpaper")).toList());

    when(roomCalculationRepository.findRoomCalculationsByCeilingMaterial(any(String.class)))
        .thenReturn(
            mockRooms.stream().filter(x -> x.getCeilingMaterial().equals("Ceiling Tile")).toList());

    when(materialRepository.findAll()).thenReturn(mockMaterials);

    // Calls to method after updating materials
    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Floor Tiles"))
                .findAny()
                .orElse(null)));

    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Wallpaper"))
                .findAny()
                .orElse(null)));

    calculationService.updateRoomsAndCalculationsOnMaterialUpdate(
        Objects.requireNonNull(
            mockMaterials.stream()
                .filter(x -> x.getName().equals("Ceiling Tile"))
                .findAny()
                .orElse(null)));

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
}
