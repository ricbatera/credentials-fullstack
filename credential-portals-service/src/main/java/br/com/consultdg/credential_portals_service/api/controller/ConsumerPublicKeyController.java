package br.com.consultdg.credential_portals_service.api.controller;

import br.com.consultdg.credential_portals_service.api.model.BasicCredentialsResponseDTO;
import br.com.consultdg.credential_portals_service.api.model.ConsumerPublicKeyRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.ConsumerPublicKeyResponseDTO;
import br.com.consultdg.credential_portals_service.service.ConsumerPublicKeyService;
import br.com.consultdg.credential_portals_service.service.CredentialsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciamento das chaves públicas dos consumidores da API.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/consumer-keys")
@Tag(name = "Consumer Public Keys", description = "API para gerenciamento de chaves públicas dos consumidores")
public class ConsumerPublicKeyController {

    @Autowired
    private ConsumerPublicKeyService consumerPublicKeyService;

    @Autowired
    private CredentialsService credentialsService;

    @GetMapping
    @Operation(summary = "Lista todas as chaves públicas", description = "Retorna uma lista com todas as chaves públicas ativas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de chaves públicas retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class)))
    })
    public ResponseEntity<List<ConsumerPublicKeyResponseDTO>> getAllPublicKeys() {
        List<ConsumerPublicKeyResponseDTO> keys = consumerPublicKeyService.findAll();
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca chave pública por ID", description = "Retorna uma chave pública específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chave pública encontrada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Chave pública não encontrada", content = @Content)
    })
    public ResponseEntity<ConsumerPublicKeyResponseDTO> getPublicKeyById(
            @Parameter(description = "ID da chave pública", required = true) @PathVariable UUID id) {
        return consumerPublicKeyService.findById(id)
                .map(key -> ResponseEntity.ok(key))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/consumer/{consumerIdentifier}")
    @Operation(summary = "Busca chave pública por identificador do consumidor", 
               description = "Retorna a chave pública de um consumidor específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chave pública encontrada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Chave pública não encontrada para este consumidor", content = @Content)
    })
    public ResponseEntity<ConsumerPublicKeyResponseDTO> getPublicKeyByConsumerIdentifier(
            @Parameter(description = "Identificador único do consumidor", required = true) @PathVariable String consumerIdentifier) {
        return consumerPublicKeyService.findByConsumerIdentifier(consumerIdentifier)
                .map(key -> ResponseEntity.ok(key))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registra nova chave pública", description = "Registra uma nova chave pública para um consumidor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Chave pública registrada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou chave pública inválida", content = @Content),
        @ApiResponse(responseCode = "409", description = "Já existe uma chave ativa para este consumidor", content = @Content)
    })
    public ResponseEntity<ConsumerPublicKeyResponseDTO> registerPublicKey(
            @Parameter(description = "Dados da chave pública", required = true) @RequestBody ConsumerPublicKeyRequestDTO requestDTO) {
        try {
            ConsumerPublicKeyResponseDTO registeredKey = consumerPublicKeyService.registerPublicKey(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza chave pública", description = "Atualiza uma chave pública existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chave pública atualizada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Chave pública não encontrada", content = @Content),
        @ApiResponse(responseCode = "400", description = "Chave pública inválida", content = @Content)
    })
    public ResponseEntity<ConsumerPublicKeyResponseDTO> updatePublicKey(
            @Parameter(description = "ID da chave pública", required = true) @PathVariable UUID id,
            @Parameter(description = "Dados atualizados da chave pública", required = true) @RequestBody ConsumerPublicKeyRequestDTO requestDTO) {
        try {
            return consumerPublicKeyService.updatePublicKey(id, requestDTO)
                    .map(key -> ResponseEntity.ok(key))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove chave pública", description = "Remove (desativa) uma chave pública do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Chave pública removida com sucesso", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chave pública não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deletePublicKey(
            @Parameter(description = "ID da chave pública", required = true) @PathVariable UUID id) {
        boolean deleted = consumerPublicKeyService.deletePublicKey(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/consumer/{consumerIdentifier}")
    @Operation(summary = "Remove chave pública por consumidor", 
               description = "Remove (desativa) a chave pública de um consumidor específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Chave pública removida com sucesso", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chave pública não encontrada para este consumidor", content = @Content)
    })
    public ResponseEntity<Void> deletePublicKeyByConsumer(
            @Parameter(description = "Identificador único do consumidor", required = true) @PathVariable String consumerIdentifier) {
        boolean deleted = consumerPublicKeyService.deletePublicKeyByConsumerIdentifier(consumerIdentifier);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/valid")
    @Operation(summary = "Lista chaves públicas válidas", description = "Retorna apenas as chaves públicas que ainda não expiraram")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de chaves públicas válidas retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumerPublicKeyResponseDTO.class)))
    })
    public ResponseEntity<List<ConsumerPublicKeyResponseDTO>> getValidPublicKeys() {
        List<ConsumerPublicKeyResponseDTO> validKeys = consumerPublicKeyService.findValidKeys();
        return ResponseEntity.ok(validKeys);
    }

    @GetMapping("/generate-example")
    @Operation(summary = "Gera exemplo de par de chaves", 
               description = "Gera um par de chaves RSA de exemplo para demonstração")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Par de chaves gerado com sucesso",
                content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> generateKeyPairExample() {
        try {
            String keyPairExample = consumerPublicKeyService.generateKeyPairExample();
            return ResponseEntity.ok(keyPairExample);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar par de chaves: " + e.getMessage());
        }
    }

    @GetMapping("/consumer/{consumerIdentifier}/valid")
    @Operation(summary = "Verifica se consumidor possui chave válida", 
               description = "Verifica se um consumidor específico possui uma chave pública válida registrada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso",
                content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Boolean> hasValidPublicKey(
            @Parameter(description = "Identificador único do consumidor", required = true) @PathVariable String consumerIdentifier) {
        boolean hasValidKey = consumerPublicKeyService.hasValidPublicKey(consumerIdentifier);
        return ResponseEntity.ok(hasValidKey);
    }

    @GetMapping("/credentials")
    @Operation(summary = "Lista credenciais básicas para robôs", 
               description = "Retorna uma lista de credenciais básicas (urlPortal, username, password criptografada, nameMall) para consumo por robôs autenticados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de credenciais retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BasicCredentialsResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado - chave pública inválida ou não encontrada", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acesso negado - consumidor não possui permissão", content = @Content)
    })
    public ResponseEntity<List<BasicCredentialsResponseDTO>> getBasicCredentials(
            @Parameter(description = "Identificador único do consumidor", required = true) 
            @RequestHeader("X-Consumer-Identifier") String consumerIdentifier) {
        
        try {
            // Busca as credenciais com senhas criptografadas para o consumidor e converte para o DTO básico
            List<BasicCredentialsResponseDTO> basicCredentials = credentialsService.findAllWithEncryptedPassword(consumerIdentifier)
                    .stream()
                    .map(credential -> new BasicCredentialsResponseDTO(
                        credential.getUrlPortal(),
                        credential.getUsername(), 
                        credential.getEncryptedPassword(), // Senha criptografada com RSA
                        credential.getEncryptedPasswordOfInvoice(), // Senha da nota fiscal criptografada com RSA
                        credential.getNameMall()
                    ))
                    .toList();

            return ResponseEntity.ok(basicCredentials);
            
        } catch (IllegalArgumentException e) {
            // Consumidor não possui chave pública válida
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            // Erro interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
