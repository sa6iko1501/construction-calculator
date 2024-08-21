/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.ConstructionCalculationRepository;
import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.dao.RoomCalculationRepository;
import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.services.helpers.RoomValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculationService {
  private final RoomCalculationRepository roomCalculationRepository;
  private final ConstructionCalculationRepository constructionCalculationRepository;
  private final MaterialRepository materialRepository;
  private final RoomValidator roomValidator;

  public String handleConstructionCalculationCreation(
      ConstructionCalculation calculation, List<RoomCalculation> rooms, User user) {
    calculateRoomDetails(rooms);
    setRoomNumbers(rooms);
    String errMsg = roomValidator.validateRooms(rooms);
    if (errMsg == null) {
      setConstructionRooms(calculation, rooms);
      calculateConstructionDetails(calculation);
      calculation.setUser(user);
    }
    saveConstructionCalculation(calculation);
    return errMsg;
  }

  public void updateRoomsAndCalculationsOnMaterialUpdate(Material material) {
    List<RoomCalculation> roomCalculations = new ArrayList<>();
    switch (material.getType()) {
      case WALL:
        {
          roomCalculations =
              roomCalculationRepository.findRoomCalculationsByWallMaterial(material.getName());
          if (!roomCalculations.isEmpty()) {
            roomCalculations.forEach(x -> x.setWallMaterialPrice(material.getPricePerSqMeter()));
          } else {
            return;
          }
          break;
        }
      case FLOOR:
        {
          roomCalculations =
              roomCalculationRepository.findRoomCalculationsByFloorMaterial(material.getName());
          if (!roomCalculations.isEmpty()) {
            roomCalculations.forEach(x -> x.setFloorMaterialPrice(material.getPricePerSqMeter()));
          } else {
            return;
          }

          break;
        }
      case CEILING:
        {
          roomCalculations =
              roomCalculationRepository.findRoomCalculationsByCeilingMaterial(material.getName());
          if (!roomCalculations.isEmpty()) {
            roomCalculations.forEach(x -> x.setCeilingMaterialPrice(material.getPricePerSqMeter()));
          } else {
            return;
          }
          break;
        }
    }
    calculateRoomDetails(roomCalculations);
    roomCalculations.forEach(
        x -> {
          calculateConstructionDetails(x.getConstructionCalculation());
          saveConstructionCalculation(x.getConstructionCalculation());
        });
  }

  public void deleteCalculationById(UUID id) {
    constructionCalculationRepository.deleteById(id);
  }

  public void saveConstructionCalculation(ConstructionCalculation calculation) {
    constructionCalculationRepository.save(calculation);
  }

  public List<ConstructionCalculation> getAllCalculations(User user) {
    return constructionCalculationRepository.findConstructionCalculationsByUserOrderByDate(user);
  }

  public ConstructionCalculation getCalculation(UUID id) {
    Optional<ConstructionCalculation> calculation = constructionCalculationRepository.findById(id);
    return calculation.orElse(null);
  }

  private void calculateConstructionDetails(ConstructionCalculation calculation) {
    List<RoomCalculation> roomCalculations = calculation.getRoomCalculations().stream().toList();
    int numberOfRooms = roomCalculations.size();
    BigDecimal price = BigDecimal.ZERO;
    BigDecimal sqM = BigDecimal.ZERO;
    for (RoomCalculation roomCalculation : roomCalculations) {
      price =
          price
              .add(BigDecimal.valueOf(roomCalculation.getRoomPrice()))
              .setScale(2, RoundingMode.HALF_UP);
      sqM =
          sqM.add(BigDecimal.valueOf(roomCalculation.getRoomArea()))
              .setScale(2, RoundingMode.HALF_UP);
    }
    calculation.setCalculationPrice(price.doubleValue());
    calculation.setSquareMeters(sqM.doubleValue());
    calculation.setNumberOfRooms(numberOfRooms);
  }

  private void calculateRoomDetails(List<RoomCalculation> rooms) {
    List<Material> materials = materialRepository.findAll();
    for (RoomCalculation room : rooms) {
      Material wallMaterial =
          materials.stream()
              .filter(m -> m.getName().equals(room.getWallMaterial()))
              .findFirst()
              .orElse(null);
      Material floorMaterial =
          materials.stream()
              .filter(m -> m.getName().equals(room.getFloorMaterial()))
              .findFirst()
              .orElse(null);
      Material ceilingMaterial =
          materials.stream()
              .filter(m -> m.getName().equals(room.getCeilingMaterial()))
              .findFirst()
              .orElse(null);
      if (wallMaterial == null || floorMaterial == null || ceilingMaterial == null) {
        throw new RuntimeException(
            String.format(
                "Error occurred while trying to set material price for room `%s`",
                room.getRoomId()));
      }
      BigDecimal wallSqM = BigDecimal.valueOf(room.getWallSqM());
      BigDecimal wallPrice =
          BigDecimal.valueOf(wallMaterial.getPricePerSqMeter())
              .multiply(wallSqM)
              .setScale(2, RoundingMode.HALF_UP);
      BigDecimal ceilingSqM = BigDecimal.valueOf(room.getCeilingSqM());
      BigDecimal ceilingPrice =
          BigDecimal.valueOf(ceilingMaterial.getPricePerSqMeter())
              .multiply(ceilingSqM)
              .setScale(2, RoundingMode.HALF_UP);
      BigDecimal floorSqM = BigDecimal.valueOf(room.getFloorSqM());
      BigDecimal floorPrice =
          BigDecimal.valueOf(floorMaterial.getPricePerSqMeter())
              .multiply(floorSqM)
              .setScale(2, RoundingMode.HALF_UP);
      BigDecimal roomPrice =
          wallPrice.add(ceilingPrice).add(floorPrice).setScale(2, RoundingMode.HALF_UP);
      BigDecimal roomArea = wallSqM.add(ceilingSqM).add(floorSqM).setScale(2, RoundingMode.HALF_UP);
      room.setWallMaterialPrice(wallPrice.doubleValue());
      room.setCeilingMaterialPrice(ceilingPrice.doubleValue());
      room.setFloorMaterialPrice(floorPrice.doubleValue());
      room.setRoomPrice(roomPrice.doubleValue());
      room.setRoomArea(roomArea.doubleValue());
    }
  }

  private void setRoomNumbers(List<RoomCalculation> rooms) {
    for (int i = 0; i < rooms.size(); i++) {
      rooms.get(i).setRoomNumber(String.format("Room %s", i + 1));
    }
  }

  private void setConstructionRooms(
      ConstructionCalculation calculation, List<RoomCalculation> roomsList) {
    roomsList.forEach(room -> room.setConstructionCalculation(calculation));
    Set<RoomCalculation> roomsSet = new HashSet<>(roomsList);
    calculation.setRoomCalculations(roomsSet);
  }
}
