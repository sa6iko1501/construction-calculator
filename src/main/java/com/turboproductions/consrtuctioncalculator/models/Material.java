/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
    name = "tb_material",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "material_name"}))
public class Material {
  @Id
  @Column(name = "material_id")
  private UUID materialId;

  @Column(name = "material_name", nullable = false)
  private String name;

  @Column(name = "material_type")
  private MaterialType type;

  @Column(name = "material_price_perSqM")
  private double pricePerSqMeter;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Material(String name, MaterialType type, double pricePerSqMeter) {
    this.materialId = UUID.randomUUID();
    this.name = name;
    this.pricePerSqMeter = pricePerSqMeter;
    this.type = type;
  }
}
