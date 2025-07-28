package br.com.consultdg.credential_portals_service.repository;

import br.com.consultdg.credential_portals_service.model.ConsumerPublicKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para gerenciamento das chaves públicas dos consumidores.
 */
@Repository
public interface ConsumerPublicKeyRepository extends JpaRepository<ConsumerPublicKey, UUID> {

    /**
     * Busca uma chave pública por identificador do consumidor.
     * 
     * @param consumerIdentifier O identificador único do consumidor
     * @return Optional contendo a chave pública se encontrada
     */
    @Query("SELECT cpk FROM ConsumerPublicKey cpk WHERE cpk.consumerIdentifier = :consumerIdentifier AND cpk.active = true")
    Optional<ConsumerPublicKey> findByConsumerIdentifierAndActive(@Param("consumerIdentifier") String consumerIdentifier);

    /**
     * Busca todas as chaves públicas ativas.
     * 
     * @return Lista de chaves públicas ativas
     */
    @Query("SELECT cpk FROM ConsumerPublicKey cpk WHERE cpk.active = true")
    List<ConsumerPublicKey> findAllActive();

    /**
     * Busca chaves públicas por nome do consumidor.
     * 
     * @param consumerName O nome do consumidor
     * @return Lista de chaves públicas do consumidor
     */
    @Query("SELECT cpk FROM ConsumerPublicKey cpk WHERE cpk.consumerName = :consumerName AND cpk.active = true")
    List<ConsumerPublicKey> findByConsumerNameAndActive(@Param("consumerName") String consumerName);

    /**
     * Verifica se existe uma chave pública para o identificador do consumidor.
     * 
     * @param consumerIdentifier O identificador único do consumidor
     * @return true se existe, false caso contrário
     */
    boolean existsByConsumerIdentifierAndActive(String consumerIdentifier, Boolean active);

    /**
     * Busca chaves públicas que ainda são válidas (não expiraram).
     * 
     * @return Lista de chaves públicas válidas
     */
    @Query("SELECT cpk FROM ConsumerPublicKey cpk WHERE cpk.active = true AND (cpk.expiresAt IS NULL OR cpk.expiresAt > CURRENT_TIMESTAMP)")
    List<ConsumerPublicKey> findValidKeys();

    /**
     * Busca chave pública válida por identificador do consumidor.
     * 
     * @param consumerIdentifier O identificador único do consumidor
     * @return Optional contendo a chave pública válida se encontrada
     */
    @Query("SELECT cpk FROM ConsumerPublicKey cpk WHERE cpk.consumerIdentifier = :consumerIdentifier AND cpk.active = true AND (cpk.expiresAt IS NULL OR cpk.expiresAt > CURRENT_TIMESTAMP)")
    Optional<ConsumerPublicKey> findValidKeyByConsumerIdentifier(@Param("consumerIdentifier") String consumerIdentifier);
}
