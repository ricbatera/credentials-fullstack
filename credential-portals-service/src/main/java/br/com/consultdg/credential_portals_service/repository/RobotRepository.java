package br.com.consultdg.credential_portals_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.consultdg.credential_portals_service.model.Robot;

@Repository
public interface RobotRepository extends JpaRepository<Robot, UUID> {

    @Query("SELECT r FROM Robot r WHERE r.robotName = :robotName")
    Optional<Robot> findByRobotName(@Param("robotName") String robotName);

    @Query("SELECT r FROM Robot r ORDER BY r.robotName")
    List<Robot> findAllOrderByRobotName();

    @Query("SELECT r FROM Robot r JOIN FETCH r.credentials WHERE r.id = :id")
    Optional<Robot> findByIdWithCredentials(@Param("id") UUID id);

    @Query("SELECT DISTINCT r FROM Robot r JOIN FETCH r.credentials")
    List<Robot> findAllWithCredentials();
}
