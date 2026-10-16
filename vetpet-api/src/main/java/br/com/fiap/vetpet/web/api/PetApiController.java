package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.response.AcompanhamentoPetResponse;
import br.com.fiap.vetpet.dto.response.EventoPetResponse;
import br.com.fiap.vetpet.dto.response.PaginaResponse;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.service.AcompanhamentoService;
import br.com.fiap.vetpet.service.HistoricoService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.web.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(PetApiController.BASE)
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Cadastro, vínculos e acompanhamento dos pets")
public class PetApiController {

    static final String BASE = "/api/v1/pets";

    private final PetService petService;
    private final AcompanhamentoService acompanhamentoService;
    private final HistoricoService historicoService;

    @GetMapping
    @Operation(summary = "Lista pets com busca por nome, código ou raça")
    public PaginaResponse<PetResponse> listar(@RequestParam(defaultValue = "") String busca,
                                              @RequestParam(defaultValue = "0") int pagina,
                                              @RequestParam(defaultValue = "10") int tamanho) {
        return PaginaResponse.de(petService.listar(busca, Paginacao.de(pagina, tamanho, Sort.by("nome"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pet pelo ID")
    public PetResponse buscar(@PathVariable Long id) {
        return petService.buscar(id);
    }

    @GetMapping("/codigo/{petCode}")
    @Operation(summary = "Busca um pet pelo código PET-XXXXXXXX")
    public PetResponse buscarPorCodigo(@PathVariable String petCode) {
        return petService.buscarPorCodigo(petCode);
    }

    @GetMapping("/{id}/acompanhamento")
    @Operation(summary = "Visão completa da saúde do pet: situação, atendimentos, prontuários, perguntas e linha do tempo")
    public AcompanhamentoPetResponse acompanhamento(@PathVariable Long id) {
        return acompanhamentoService.montar(id);
    }

    @GetMapping("/{id}/linha-do-tempo")
    @Operation(summary = "Eventos do pet registrados a partir das mensagens do RabbitMQ")
    public List<EventoPetResponse> linhaDoTempo(@PathVariable Long id) {
        petService.buscar(id);
        return historicoService.linhaDoTempo(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um pet vinculado a um ou mais tutores")
    public ResponseEntity<PetResponse> cadastrar(@Valid @RequestBody PetRequest request) {
        PetResponse pet = petService.cadastrar(request);
        return ResponseEntity.created(Localizacao.recurso(BASE, pet.getId())).body(pet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados e os tutores de um pet")
    public PetResponse atualizar(@PathVariable Long id, @Valid @RequestBody PetRequest request) {
        return petService.atualizar(id, request);
    }

    @PostMapping("/{id}/tutores/{tutorId}")
    @Operation(summary = "Vincula mais um tutor ao pet")
    public PetResponse vincularTutor(@PathVariable Long id, @PathVariable Long tutorId) {
        return petService.vincularTutor(id, tutorId);
    }

    @DeleteMapping("/{id}/tutores/{tutorId}")
    @Operation(summary = "Remove o vínculo de um tutor com o pet")
    public PetResponse desvincularTutor(@PathVariable Long id, @PathVariable Long tutorId) {
        return petService.desvincularTutor(id, tutorId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um pet e todo o seu histórico")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        petService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
