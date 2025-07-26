package br.com.consultdg.credential_portals_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.consultdg.credential_portals_service.model.Credentials;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {

    @Query("SELECT c FROM Credentials c WHERE c.active = true")
    List<Credentials> findAllActive();

    @Query("SELECT c FROM Credentials c WHERE c.cnpj = :cnpj AND c.active = true")
    Optional<Credentials> findByCnpjAndActive(@Param("cnpj") String cnpj);

    @Query("SELECT c FROM Credentials c WHERE c.nameMall = :nameMall AND c.active = true")
    List<Credentials> findByNameMallAndActive(@Param("nameMall") String nameMall);

    @Query("SELECT c FROM Credentials c WHERE c.username = :username AND c.active = true")
    Optional<Credentials> findByUsernameAndActive(@Param("username") String username);
}
