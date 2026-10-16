package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.Cadastro;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.dto.response.PaginaResponse;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.service.TutorService;
import br.com.fiap.vetpet.web.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TutorApiController.BASE)
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Responsáveis pelos pets")
public class TutorApiController {

    static final String BASE = "/api/v1/tutores";

    private final TutorService tutorService;

    @GetMapping
    @Operation(summary = "Lista tutores com busca por nome, e-mail ou CPF")
    public PaginaResponse<TutorResponse> listar(@RequestParam(defaultValue = "") String busca,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "10") int tamanho) {
        return PaginaResponse.de(tutorService.listar(busca, Paginacao.de(pagina, tamanho, Sort.by("nome"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um tutor pelo ID")
    public TutorResponse buscar(@PathVariable Long id) {
        return tutorService.buscar(id);
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Busca um tutor pelo CPF")
    public TutorResponse buscarPorCpf(@PathVariable String cpf) {
        return tutorService.buscarPorCpf(cpf);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Busca um tutor pelo e-mail")
    public TutorResponse buscarPorEmail(@PathVariable String email) {
        return tutorService.buscarPorEmail(email);
    }

    @PostMapping
    @Operation(summary = "Cadastra um tutor (rota pública para o app do tutor)")
    public ResponseEntity<TutorResponse> cadastrar(@Validated(Cadastro.class) @RequestBody TutorRequest request) {
        TutorResponse tutor = tutorService.cadastrar(request);
        return ResponseEntity.created(Localizacao.recurso(BASE, tutor.getId())).body(tutor);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um tutor")
    public TutorResponse atualizar(@PathVariable Long id, @Valid @RequestBody TutorRequest request) {
        return tutorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um tutor e seus vínculos com pets")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tutorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
