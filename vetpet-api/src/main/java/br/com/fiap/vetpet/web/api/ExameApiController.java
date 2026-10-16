package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.dto.request.StatusExameRequest;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.dto.response.PaginaResponse;
import br.com.fiap.vetpet.service.ExameService;
import br.com.fiap.vetpet.web.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(ExameApiController.BASE)
@RequiredArgsConstructor
@Tag(name = "Atendimentos", description = "Agendamento e acompanhamento de consultas, exames e vacinas")
public class ExameApiController {

    static final String BASE = "/api/v1/exames";

    private final ExameService exameService;

    @GetMapping
    @Operation(summary = "Lista atendimentos com filtros opcionais de status, pet, veterinário e data")
    public PaginaResponse<ExameResponse> listar(
            @RequestParam(required = false) StatusExame status,
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return PaginaResponse.de(exameService.listar(status, petId, veterinarioId, data,
                Paginacao.de(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataHora"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um atendimento pelo ID")
    public ExameResponse buscar(@PathVariable Long id) {
        return exameService.buscar(id);
    }

    @GetMapping("/proximo")
    @Operation(summary = "Próximo atendimento em aberto de um pet")
    public ResponseEntity<ExameResponse> proximo(@RequestParam Long petId) {
        return ResponseEntity.of(exameService.buscarProximoDoPet(petId));
    }

    @PostMapping
    @Operation(summary = "Agenda um atendimento e publica o evento exame.agendado")
    public ResponseEntity<ExameResponse> agendar(@Valid @RequestBody ExameRequest request) {
        ExameResponse exame = exameService.agendar(request);
        return ResponseEntity.created(Localizacao.recurso(BASE, exame.getId())).body(exame);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status (AGENDADO, ADIADO ou CANCELADO)")
    public ExameResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusExameRequest request) {
        return exameService.alterarStatus(id, request.status());
    }

    @PatchMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela um atendimento em aberto")
    public ExameResponse cancelar(@PathVariable Long id) {
        return exameService.cancelar(id);
    }
}
