/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.dao;

import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomCalculationRepository extends JpaRepository<RoomCalculation, UUID> {}
