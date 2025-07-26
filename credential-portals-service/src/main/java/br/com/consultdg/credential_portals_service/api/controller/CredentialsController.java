package br.com.consultdg.credential_portals_service.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.consultdg.credential_portals_service.api.model.CredentialsRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.CredentialsResponseDTO;
import br.com.consultdg.credential_portals_service.service.CredentialsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin
@RequestMapping("/api/credentials")
@Tag(name = "Credentials", description = "API para gerenciamento de credenciais de portais")
public class CredentialsController {

    @Autowired
    private CredentialsService credentialsService;

    @GetMapping
    @Operation(summary = "Lista todas as credenciais", description = "Retorna uma lista com todas as credenciais ativas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de credenciais retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class)))
    })
    public ResponseEntity<List<CredentialsResponseDTO>> getAllCredentials() {
        List<CredentialsResponseDTO> credentials = credentialsService.findAll();
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca credencial por ID", description = "Retorna uma credencial específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credencial encontrada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Credencial não encontrada", content = @Content)
    })
    public ResponseEntity<CredentialsResponseDTO> getCredentialById(
            @Parameter(description = "ID da credencial", required = true) @PathVariable UUID id) {
        return credentialsService.findById(id)
                .map(credential -> ResponseEntity.ok(credential))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Busca credencial por CNPJ", description = "Retorna uma credencial específica pelo CNPJ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credencial encontrada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Credencial não encontrada", content = @Content)
    })
    public ResponseEntity<CredentialsResponseDTO> getCredentialByCnpj(
            @Parameter(description = "CNPJ da empresa", required = true) @PathVariable String cnpj) {
        return credentialsService.findByCnpj(cnpj)
                .map(credential -> ResponseEntity.ok(credential))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Busca credenciais por nome do shopping", description = "Retorna credenciais filtradas pelo nome do shopping")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credenciais encontradas",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhuma credencial encontrada", content = @Content)
    })
    public ResponseEntity<List<CredentialsResponseDTO>> getCredentialsByNameMall(
            @Parameter(description = "Nome do shopping", required = true) @RequestParam String nameMall) {
        List<CredentialsResponseDTO> credentials = credentialsService.findByNameMall(nameMall);
        if (credentials.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(credentials);
    }

    @PostMapping
    @Operation(summary = "Cria nova credencial", description = "Cria uma nova credencial no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Credencial criada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<CredentialsResponseDTO> createCredential(
            @Parameter(description = "Dados da credencial", required = true) @RequestBody CredentialsRequestDTO requestDTO) {
        try {
            CredentialsResponseDTO createdCredential = credentialsService.create(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCredential);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza credencial", description = "Atualiza uma credencial existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credencial atualizada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Credencial não encontrada", content = @Content)
    })
    public ResponseEntity<CredentialsResponseDTO> updateCredential(
            @Parameter(description = "ID da credencial", required = true) @PathVariable UUID id, 
            @Parameter(description = "Dados atualizados da credencial", required = true) @RequestBody CredentialsRequestDTO requestDTO) {
        return credentialsService.update(id, requestDTO)
                .map(credential -> ResponseEntity.ok(credential))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove credencial", description = "Remove uma credencial do sistema (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Credencial removida com sucesso", content = @Content),
        @ApiResponse(responseCode = "404", description = "Credencial não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deleteCredential(
            @Parameter(description = "ID da credencial", required = true) @PathVariable UUID id) {
        boolean deleted = credentialsService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
