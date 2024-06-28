/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "tb_material")
public class Material {
  @Id
  @Column(name = "material_id")
  @Getter
  private UUID material_id;

  @Column(name = "material_name", unique = true, nullable = false)
  @Getter
  private String name;

  @Column(name = "material_price_perSqM")
  @Getter
  private double pricePerSqMeter;

  public Material(String name, double pricePerSqMeter) {
    this.material_id = UUID.randomUUID();
    this.name = name;
    this.pricePerSqMeter = pricePerSqMeter;
  }
}
