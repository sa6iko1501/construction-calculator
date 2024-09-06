/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.dao;

import com.turboproductions.consrtuctioncalculator.models.RoomCalculation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomCalculationRepository extends JpaRepository<RoomCalculation, UUID> {
  @Query(
      "SELECT r FROM RoomCalculation r WHERE r.userUUID = :userId AND r.constructionCalculation.active = true AND r.ceilingMaterial =:ceiling")
  List<RoomCalculation> findRoomCalculationsByUserUUIDAndAndCeilingMaterial(
      @Param("userId") UUID userId, @Param("ceiling") String ceilingMaterialName);

  @Query(
      "SELECT r FROM RoomCalculation r WHERE r.userUUID = :userId AND r.constructionCalculation.active = true AND r.wallMaterial =:wall")
  List<RoomCalculation> findRoomCalculationsByUserUUIDAndAndWallMaterial(
      @Param("userId") UUID userId, @Param("wall") String wallMaterialName);

  @Query(
      "SELECT r FROM RoomCalculation r WHERE r.userUUID = :userId AND r.constructionCalculation.active = true AND r.ceilingMaterial =:floor")
  List<RoomCalculation> findRoomCalculationsByUserUUIDAndAndFloorMaterial(
      @Param("userId") UUID userId, @Param("floor") String floorMaterialName);
}
