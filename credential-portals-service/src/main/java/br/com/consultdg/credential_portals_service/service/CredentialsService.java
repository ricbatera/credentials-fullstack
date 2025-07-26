package br.com.consultdg.credential_portals_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.consultdg.credential_portals_service.api.model.CredentialsRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.CredentialsResponseDTO;
import br.com.consultdg.credential_portals_service.model.Credentials;
import br.com.consultdg.credential_portals_service.repository.CredentialsRepository;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    public List<CredentialsResponseDTO> findAll() {
        return credentialsRepository.findAllActive()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<CredentialsResponseDTO> findById(UUID id) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(this::toResponseDTO);
    }

    public CredentialsResponseDTO create(CredentialsRequestDTO requestDTO) {
        Credentials credentials = toEntity(requestDTO);
        credentials = credentialsRepository.save(credentials);
        return toResponseDTO(credentials);
    }

    public Optional<CredentialsResponseDTO> update(UUID id, CredentialsRequestDTO requestDTO) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(existingCredentials -> {
                    updateEntityFromDTO(existingCredentials, requestDTO);
                    return credentialsRepository.save(existingCredentials);
                })
                .map(this::toResponseDTO);
    }

    public boolean delete(UUID id) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(credentials -> {
                    credentials.setActive(false);
                    credentials.setDeletedAt(LocalDateTime.now());
                    credentialsRepository.save(credentials);
                    return true;
                })
                .orElse(false);
    }

    public Optional<CredentialsResponseDTO> findByCnpj(String cnpj) {
        return credentialsRepository.findByCnpjAndActive(cnpj)
                .map(this::toResponseDTO);
    }

    public List<CredentialsResponseDTO> findByNameMall(String nameMall) {
        return credentialsRepository.findByNameMallAndActive(nameMall)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Credentials toEntity(CredentialsRequestDTO requestDTO) {
        Credentials credentials = new Credentials();
        credentials.setNameMall(requestDTO.getNameMall());
        credentials.setCnpj(requestDTO.getCnpj());
        credentials.setUrlPortal(requestDTO.getUrlPortal());
        credentials.setUsername(requestDTO.getUsername());
        credentials.setPassword(requestDTO.getPassword());
        credentials.setActive(requestDTO.getActive() != null ? requestDTO.getActive() : true);
        return credentials;
    }

    private void updateEntityFromDTO(Credentials credentials, CredentialsRequestDTO requestDTO) {
        credentials.setNameMall(requestDTO.getNameMall());
        credentials.setCnpj(requestDTO.getCnpj());
        credentials.setUrlPortal(requestDTO.getUrlPortal());
        credentials.setUsername(requestDTO.getUsername());
        credentials.setPassword(requestDTO.getPassword());
        credentials.setActive(requestDTO.getActive() != null ? requestDTO.getActive() : credentials.getActive());
    }

    private CredentialsResponseDTO toResponseDTO(Credentials credentials) {
        return new CredentialsResponseDTO(
                credentials.getId(),
                credentials.getNameMall(),
                credentials.getCnpj(),
                credentials.getUrlPortal(),
                credentials.getUsername(),
                credentials.getCreatedAt(),
                credentials.getUpdatedAt(),
                credentials.getDeletedAt(),
                credentials.getActive()
        );
    }
}
