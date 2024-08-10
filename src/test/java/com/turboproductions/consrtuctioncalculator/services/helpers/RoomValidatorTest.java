/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RoomValidatorTest {
  private final RoomValidator roomValidator = new RoomValidator();
  private List<RoomCalculation> roomCalculations;
  private static final String INVALID_PRICE_ERROR =
      "Invalid value for wall, ceiling or floor material price. Please check your material list.";
  private static final String INVALID_AREA_ERROR =
      "Invalid value for wall, ceiling or floor square meters. Values cannot be lower than 0.";

  @BeforeEach
  void setUp() {
    roomCalculations = new ArrayList<>();
    roomCalculations.add(new RoomCalculation());
    roomCalculations.getFirst().setCeilingMaterialPrice(12.3);
    roomCalculations.getFirst().setCeilingSqM(13.3);
    roomCalculations.getFirst().setWallMaterialPrice(13.3);
    roomCalculations.getFirst().setWallSqM(46.6);
    roomCalculations.getFirst().setFloorMaterialPrice(4.4);
    roomCalculations.getFirst().setFloorSqM(12.2);
    roomCalculations.getFirst().setRoomArea(100);
    roomCalculations.getFirst().setRoomPrice(532);
  }

  @Test
  void testValidData() {
    assertNull(roomValidator.validateRooms(roomCalculations));
  }

  @Test
  void testInvalidPrice() {
    // Invalid ceiling price
    roomCalculations.getFirst().setCeilingMaterialPrice(-2);
    assertEquals(INVALID_PRICE_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid floor price
    roomCalculations.getFirst().setCeilingMaterialPrice(13.3);
    roomCalculations.getFirst().setFloorMaterialPrice(-0.1);
    assertEquals(INVALID_PRICE_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid wall price
    roomCalculations.getFirst().setFloorMaterialPrice(4.4);
    roomCalculations.getFirst().setWallMaterialPrice(-0.01);
    assertEquals(INVALID_PRICE_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid total price
    roomCalculations.getFirst().setWallMaterialPrice(13.3);
    roomCalculations.getFirst().setRoomPrice(0);
    assertEquals(INVALID_PRICE_ERROR, roomValidator.validateRooms(roomCalculations));
  }

  @Test
  void testInvalidArea() {
    // Invalid ceiling area
    roomCalculations.getFirst().setCeilingSqM(-15);
    assertEquals(INVALID_AREA_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid wall area
    roomCalculations.getFirst().setCeilingSqM(13.3);
    roomCalculations.getFirst().setWallSqM(-1);
    assertEquals(INVALID_AREA_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid floor area
    roomCalculations.getFirst().setWallSqM(13.3);
    roomCalculations.getFirst().setFloorSqM(-0.1);
    assertEquals(INVALID_AREA_ERROR, roomValidator.validateRooms(roomCalculations));

    // Invalid total area
    roomCalculations.getFirst().setFloorSqM(13.3);
    roomCalculations.getFirst().setRoomArea(0);
    assertEquals(INVALID_AREA_ERROR, roomValidator.validateRooms(roomCalculations));
  }
}
