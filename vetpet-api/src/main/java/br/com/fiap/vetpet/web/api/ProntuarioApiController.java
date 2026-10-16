package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.ProntuarioRequest;
import br.com.fiap.vetpet.dto.response.PaginaResponse;
import br.com.fiap.vetpet.dto.response.ProntuarioResponse;
import br.com.fiap.vetpet.service.ProntuarioService;
import br.com.fiap.vetpet.web.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ProntuarioApiController.BASE)
@RequiredArgsConstructor
@Tag(name = "Prontuários", description = "Registro clínico de cada atendimento")
public class ProntuarioApiController {

    static final String BASE = "/api/v1/prontuarios";

    private final ProntuarioService prontuarioService;

    @GetMapping
    @Operation(summary = "Lista prontuários, opcionalmente por pet ou veterinário")
    public PaginaResponse<ProntuarioResponse> listar(@RequestParam(required = false) Long petId,
                                                     @RequestParam(required = false) Long veterinarioId,
                                                     @RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "10") int tamanho) {
        return PaginaResponse.de(prontuarioService.listar(petId, veterinarioId,
                Paginacao.de(pagina, tamanho, Sort.by(Sort.Direction.DESC, "registradoEm"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um prontuário pelo ID")
    public ProntuarioResponse buscar(@PathVariable Long id) {
        return prontuarioService.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra o prontuário, conclui o atendimento e publica prontuario.registrado")
    public ResponseEntity<ProntuarioResponse> registrar(@Valid @RequestBody ProntuarioRequest request) {
        ProntuarioResponse prontuario = prontuarioService.registrar(request);
        return ResponseEntity.created(Localizacao.recurso(BASE, prontuario.getId())).body(prontuario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um prontuário")
    public ProntuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProntuarioRequest request) {
        return prontuarioService.atualizar(id, request);
    }
}
