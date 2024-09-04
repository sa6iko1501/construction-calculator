/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_calculation")
@Getter
public class ConstructionCalculation {
  @Id
  @Column(name = "calculation_id")
  private UUID calculationId;

  @Setter
  @Column(name = "calculation_name")
  private String name;

  @Setter
  @Column(name = "room_numbers")
  private int numberOfRooms;

  @Setter
  @Column(name = "calculation_sq_m")
  private double squareMeters;

  @Setter
  @Column(name = "calculation_price")
  private double calculationPrice;

  @Setter
  @Column(name = "time_of_calculation")
  private LocalDateTime date;

  @Setter
  @OneToMany(mappedBy = "constructionCalculation", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RoomCalculation> roomCalculations;

  @Setter
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  public ConstructionCalculation() {
    this.date = LocalDateTime.now();
    this.calculationId = UUID.randomUUID();
  }
}
