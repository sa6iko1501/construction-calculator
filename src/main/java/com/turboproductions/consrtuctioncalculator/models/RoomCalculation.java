/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "tb_room")
public class RoomCalculation {
  @Id
  @Column(name = "room_id")
  private UUID roomId;

  @Column(name = "room_number")
  @Setter
  private String roomNumber;

  @Setter
  @Column(name = "floor_material")
  private String floorMaterial;

  @Setter
  @Column(name = "floor_area")
  private double floorSqM;

  @Setter
  @Column(name = "floor_price")
  private double floorMaterialPrice;

  @Setter
  @Column(name = "wall_material")
  private String wallMaterial;

  @Setter
  @Column(name = "wall_area")
  private double wallSqM;

  @Setter
  @Column(name = "wall_price")
  private double wallMaterialPrice;

  @Setter
  @Column(name = "ceiling_material")
  private String ceilingMaterial;

  @Setter
  @Column(name = "ceiling_area")
  private double ceilingSqM;

  @Setter
  @Column(name = "ceiling_price")
  private double ceilingMaterialPrice;

  @Setter
  @Column(name = "room_area")
  private double roomArea;

  @Setter
  @Column(name = "room_price")
  private double roomPrice;

  @Setter
  @ManyToOne
  @JoinColumn(name = "calculation_id", nullable = false)
  private ConstructionCalculation constructionCalculation;

  public RoomCalculation(
      ConstructionCalculation constructionCalculation,
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
    this.constructionCalculation = constructionCalculation;
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
