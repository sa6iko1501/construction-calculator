/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.dao;

import com.turboproductions.consrtuctioncalculator.models.ConstructionCalculation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructionCalculationRepository
    extends JpaRepository<ConstructionCalculation, UUID> {}
