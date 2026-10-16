package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.request.RespostaRequest;
import br.com.fiap.vetpet.dto.response.PerguntaResponse;
import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.PerguntaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Perguntas", description = "Dúvidas dos tutores e respostas da equipe")
public class PerguntaApiController {

    private final PerguntaService perguntaService;

    @GetMapping("/pets/{petId}/perguntas")
    @Operation(summary = "Lista as perguntas de um pet")
    public List<PerguntaResponse> listar(@PathVariable Long petId) {
        return perguntaService.listarDoPet(petId);
    }

    @PostMapping("/pets/{petId}/perguntas")
    @Operation(summary = "Registra uma pergunta sobre o pet")
    public ResponseEntity<PerguntaResponse> criar(@PathVariable Long petId,
                                                  @Valid @RequestBody PerguntaRequest request,
                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perguntaService.criar(petId, request, usuario.getNome()));
    }

    @PatchMapping("/perguntas/{id}/resposta")
    @Operation(summary = "Responde uma pergunta e publica pergunta.respondida")
    public PerguntaResponse responder(@PathVariable Long id, @Valid @RequestBody RespostaRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perguntaService.responder(id, request, usuario.getNome());
    }

    @DeleteMapping("/perguntas/{id}")
    @Operation(summary = "Remove uma pergunta")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        perguntaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
