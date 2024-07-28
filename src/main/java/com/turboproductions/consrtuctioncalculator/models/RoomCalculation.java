/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Entity
@Getter
@Table(name = "tb_room")
public class RoomCalculation {
  @Id
  @Column(name = "room_id")
  private UUID roomId;

  @Column(name = "floor_material")
  private String floorMaterial;

  @Column(name = "floor_area")
  private double floorSqM;

  @Column(name = "floor_price")
  private double floorMaterialPrice;

  @Column(name = "wall_material")
  private String wallMaterial;

  @Column(name = "wall_area")
  private double wallSqM;

  @Column(name = "wall_price")
  private double wallMaterialPrice;

  @Column(name = "ceiling_material")
  private String ceilingMaterial;

  @Column(name = "ceiling_area")
  private double ceilingSqM;

  @Column(name = "ceiling_price")
  private double ceilingMaterialPrice;

  @Column(name = "room_area")
  private double roomArea;

  @Column(name = "room_price")
  private double roomPrice;

  public RoomCalculation(
      String floorMaterial,
      double floorSqM,
      double floorMaterialPrice,
      String wallMaterial,
      double wallSqM,
      double wallMaterialPrice,
      String ceilingMaterial,
      double ceilingSqM,
      double ceilingMaterialPrice,
      double roomPrice) {
    this.roomId = UUID.randomUUID();
    this.floorMaterial = floorMaterial;
    this.floorSqM = floorSqM;
    this.floorMaterialPrice = floorMaterialPrice;
    this.wallMaterial = wallMaterial;
    this.wallSqM = wallSqM;
    this.wallMaterialPrice = wallMaterialPrice;
    this.ceilingMaterial = ceilingMaterial;
    this.ceilingSqM = ceilingSqM;
    this.ceilingMaterialPrice = ceilingMaterialPrice;
    this.roomArea = floorSqM + wallSqM + ceilingSqM;
    this.roomPrice = roomPrice;
  }

  public RoomCalculation() {
    this.roomId = UUID.randomUUID();
  }
}
