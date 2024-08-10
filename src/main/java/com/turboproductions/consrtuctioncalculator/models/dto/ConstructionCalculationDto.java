/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models.dto;

import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ConstructionCalculationDto {
  private List<RoomCalculation> rooms;
  private String calculationName;

  public void addRoomCalculation(RoomCalculation roomCalc) {
    this.rooms.add(roomCalc);
  }
}
