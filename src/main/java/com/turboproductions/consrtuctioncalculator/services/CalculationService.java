/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.ConstructionCalculationRepository;
import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.dao.RoomCalculationRepository;
import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.services.helpers.RoomValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculationService {
  private final RoomCalculationRepository roomCalculationRepository;
  private final ConstructionCalculationRepository constructionCalculationRepository;
  private final MaterialRepository materialRepository;
  private final RoomValidator roomValidator;

  public String handleConstructionCalculationCreation(
      ConstructionCalculation calculation, List<RoomCalculation> rooms) {
    calculateRoomDetails(rooms);
    String errMsg = roomValidator.validateRooms(rooms);
    if (errMsg == null) {
      setConstructionRooms(calculation, rooms);
      calculateConstructionDetails(calculation);
    }
    constructionCalculationRepository.save(calculation);
    return errMsg;
  }

  public ConstructionCalculation getCalculation(UUID id) {
    Optional<ConstructionCalculation> calculation = constructionCalculationRepository.findById(id);
    return calculation.orElse(null);
  }

  public void deleteCalculationById(UUID id) {
    constructionCalculationRepository.deleteById(id);
  }

  public List<ConstructionCalculation> getAllCalculations() {
    return constructionCalculationRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
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
    int roomNumber = 0;
    for (RoomCalculation room : rooms) {
      roomNumber++;
      room.setRoomNumber(String.format("Room %s", roomNumber));
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

  private void setConstructionRooms(
      ConstructionCalculation calculation, List<RoomCalculation> roomsList) {
    roomsList.forEach(room -> room.setConstructionCalculation(calculation));
    Set<RoomCalculation> roomsSet = new HashSet<>(roomsList);
    calculation.setRoomCalculations(roomsSet);
  }
}
