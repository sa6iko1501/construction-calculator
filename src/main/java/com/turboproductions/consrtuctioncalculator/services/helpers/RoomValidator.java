/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoomValidator {
  public String validateRooms(List<RoomCalculation> rooms) {
    String errMsg = null;
    for (RoomCalculation roomCalculation : rooms) {
      errMsg = validateArea(roomCalculation);
      if (errMsg != null) {
        break;
      }
      errMsg = validatePrice(roomCalculation);
      if (errMsg != null) {
        break;
      }
    }
    return errMsg;
  }

  private String validateArea(RoomCalculation roomCalculation) {
    if (roomCalculation.getRoomArea() <= 0
        || roomCalculation.getCeilingSqM() < 0
        || roomCalculation.getFloorSqM() < 0
        || roomCalculation.getWallSqM() < 0) {
      return "Invalid value for wall, ceiling or floor square meters. Values cannot be lower than 0.";
    }
    return null;
  }

  private String validatePrice(RoomCalculation roomCalculation) {
    if (roomCalculation.getRoomPrice() <= 0
        || roomCalculation.getCeilingMaterialPrice() < 0
        || roomCalculation.getFloorMaterialPrice() < 0
        || roomCalculation.getWallMaterialPrice() < 0) {
      return "Invalid value for wall, ceiling or floor material price. Please check your material list.";
    }
    return null;
  }
}
