/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.dao;

import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import com.turboproductions.consrtuctioncalculator.models.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructionCalculationRepository
    extends JpaRepository<ConstructionCalculation, UUID> {
  List<ConstructionCalculation> findConstructionCalculationsByUserOrderByDate(User user);

  List<ConstructionCalculation> findConstructionCalculationsByUserAndActive(
      User user, boolean active);
}
