/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_material")
public class Material {
  @Id
  @Column(name = "material_id")
  private UUID materialId;

  @Column(name = "material_name", unique = true, nullable = false)
  private String name;

  @Column(name = "material_type")
  private MaterialType type;

  @Column(name = "material_price_perSqM")
  private double pricePerSqMeter;

  public Material(String name, MaterialType type, double pricePerSqMeter) {
    this.materialId = UUID.randomUUID();
    this.name = name;
    this.pricePerSqMeter = pricePerSqMeter;
    this.type = type;
  }
}
