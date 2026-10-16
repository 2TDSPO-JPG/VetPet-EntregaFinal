package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.AlterarSenhaRequest;
import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.response.AcompanhamentoPetResponse;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.dto.response.PerguntaResponse;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.AcompanhamentoService;
import br.com.fiap.vetpet.service.ExameService;
import br.com.fiap.vetpet.service.PerguntaService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Portal do tutor", description = "Rotas do app do tutor, restritas aos pets do usuário autenticado")
public class PortalTutorApiController {

    private final TutorService tutorService;
    private final PetService petService;
    private final ExameService exameService;
    private final PerguntaService perguntaService;
    private final AcompanhamentoService acompanhamentoService;

    @GetMapping
    @Operation(summary = "Dados do tutor autenticado")
    public TutorResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return tutorService.buscarPorEmail(usuario.getUsername());
    }

    @GetMapping("/pets")
    @Operation(summary = "Pets do tutor autenticado")
    public List<PetResponse> meusPets(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return petService.listarDoTutor(usuario.getUsername());
    }

    @GetMapping("/pets/{petId}/acompanhamento")
    @Operation(summary = "Acompanhamento completo de um pet do tutor")
    public AcompanhamentoPetResponse acompanhamento(@PathVariable Long petId,
                                                    @AuthenticationPrincipal UsuarioAutenticado usuario) {
        petService.garantirAcessoDoTutor(petId, usuario.getUsername());
        return acompanhamentoService.montar(petId);
    }

    @GetMapping("/exames")
    @Operation(summary = "Atendimentos de todos os pets do tutor")
    public List<ExameResponse> meusAtendimentos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return exameService.listarDoTutor(usuario.getUsername());
    }

    @PostMapping("/pets/{petId}/perguntas")
    @Operation(summary = "Envia uma dúvida para a clínica sobre um dos seus pets")
    public ResponseEntity<PerguntaResponse> perguntar(@PathVariable Long petId,
                                                      @Valid @RequestBody PerguntaRequest request,
                                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        petService.garantirAcessoDoTutor(petId, usuario.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perguntaService.criar(petId, request, usuario.getNome()));
    }

    @PatchMapping("/senha")
    @Operation(summary = "Altera a senha do tutor autenticado")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request,
                                             @AuthenticationPrincipal UsuarioAutenticado usuario) {
        tutorService.alterarSenha(usuario.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
