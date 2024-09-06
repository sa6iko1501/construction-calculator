/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.ConstructionCalculationRepository;
import com.turboproductions.consrtuctioncalculator.dao.MaterialRepository;
import com.turboproductions.consrtuctioncalculator.dao.RoomCalculationRepository;
import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.Material;
import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.ConstructionActivityRequest;
import com.turboproductions.consrtuctioncalculator.services.helpers.RoomValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    calculateRoomDetails(rooms, user);
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

  public ByteArrayInputStream handleExcelExport(ConstructionCalculation calculation) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet(calculation.getName());
      Row constructionInfoHeader = sheet.createRow(0);
      constructionInfoHeader.createCell(0).setCellValue("Name");
      constructionInfoHeader.createCell(1).setCellValue("Room count");
      constructionInfoHeader.createCell(2).setCellValue("Total area");
      constructionInfoHeader.createCell(3).setCellValue("Total price");
      constructionInfoHeader.createCell(4).setCellValue("Date of calculation");

      Row constructionInfoRow = sheet.createRow(1);
      constructionInfoRow.createCell(0).setCellValue(calculation.getName());
      constructionInfoRow.createCell(1).setCellValue(calculation.getNumberOfRooms());
      constructionInfoRow.createCell(2).setCellValue(calculation.getSquareMeters());
      constructionInfoRow.createCell(3).setCellValue(calculation.getCalculationPrice());
      constructionInfoRow
          .createCell(4)
          .setCellValue(calculation.getDate().toString().replace("T", " "));

      Row emptyRow = sheet.createRow(2);
      Row roomInfoHeader = sheet.createRow(3);
      roomInfoHeader.createCell(0).setCellValue("Room");
      roomInfoHeader.createCell(1).setCellValue("Floor material");
      roomInfoHeader.createCell(2).setCellValue("Wall material");
      roomInfoHeader.createCell(3).setCellValue("Ceiling material");
      roomInfoHeader.createCell(4).setCellValue("Floor area");
      roomInfoHeader.createCell(5).setCellValue("Wall area");
      roomInfoHeader.createCell(6).setCellValue("Ceiling area");
      roomInfoHeader.createCell(7).setCellValue("Floor price");
      roomInfoHeader.createCell(8).setCellValue("Wall price");
      roomInfoHeader.createCell(9).setCellValue("Ceiling price");
      roomInfoHeader.createCell(10).setCellValue("Total area");
      roomInfoHeader.createCell(11).setCellValue("Total price");

      int rowNum = 4;
      for (RoomCalculation room : calculation.getRoomCalculations()) {
        Row roomInfoRow = sheet.createRow(rowNum);
        roomInfoRow.createCell(0).setCellValue(room.getRoomNumber());
        roomInfoRow.createCell(1).setCellValue(room.getFloorMaterial());
        roomInfoRow.createCell(2).setCellValue(room.getWallMaterial());
        roomInfoRow.createCell(3).setCellValue(room.getCeilingMaterial());
        roomInfoRow.createCell(4).setCellValue(room.getFloorSqM());
        roomInfoRow.createCell(5).setCellValue(room.getWallSqM());
        roomInfoRow.createCell(6).setCellValue(room.getCeilingSqM());
        roomInfoRow.createCell(7).setCellValue(room.getFloorMaterialPrice());
        roomInfoRow.createCell(8).setCellValue(room.getWallMaterialPrice());
        roomInfoRow.createCell(9).setCellValue(room.getCeilingMaterialPrice());
        roomInfoRow.createCell(10).setCellValue(room.getRoomArea());
        roomInfoRow.createCell(11).setCellValue(room.getRoomPrice());
        rowNum++;
      }

      workbook.write(outputStream);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (IOException ex) {
      return null;
    }
  }

  public void updateRoomsAndCalculationsOnMaterialUpdate(Material material, User user) {
    List<RoomCalculation> roomCalculations = new ArrayList<>();
    switch (material.getType()) {
      case WALL:
        {
          roomCalculations =
              roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndWallMaterial(
                  user.getUserId(), material.getName());
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
              roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndFloorMaterial(
                  user.getUserId(), material.getName());
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
              roomCalculationRepository.findRoomCalculationsByUserUUIDAndAndCeilingMaterial(
                  user.getUserId(), material.getName());
          if (!roomCalculations.isEmpty()) {
            roomCalculations.forEach(x -> x.setCeilingMaterialPrice(material.getPricePerSqMeter()));
          } else {
            return;
          }
          break;
        }
    }
    calculateRoomDetails(roomCalculations, user);
    Set<ConstructionCalculation> uniqueConstructionCalculations = new HashSet<>();
    roomCalculations.forEach(
        x -> uniqueConstructionCalculations.add(x.getConstructionCalculation()));
    uniqueConstructionCalculations.forEach(this::calculateConstructionDetails);
    constructionCalculationRepository.saveAll(uniqueConstructionCalculations);
  }

  public String setCalculationActivity(ConstructionActivityRequest request) {
    UUID calcId = request.getConstructionId();
    ConstructionCalculation calcToUpdate = getCalculation(calcId);
    if (calcToUpdate == null) {
      return String.format("No Calculation with the id '%s' found.", calcId);
    } else {
      calcToUpdate.setActive(request.isActive());
      saveConstructionCalculation(calcToUpdate);
      return "Successfully updated calc.";
    }
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
    calculation.setDate(LocalDateTime.now());
  }

  private void calculateRoomDetails(List<RoomCalculation> rooms, User user) {
    List<Material> materials = materialRepository.findAllByUserOrderByType(user);
    for (RoomCalculation room : rooms) {
      room.setUserUUID(user.getUserId());
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
