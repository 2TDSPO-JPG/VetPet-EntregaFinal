package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.Cadastro;
import br.com.fiap.vetpet.dto.request.VeterinarioRequest;
import br.com.fiap.vetpet.dto.response.PaginaResponse;
import br.com.fiap.vetpet.dto.response.VeterinarioResponse;
import br.com.fiap.vetpet.service.VeterinarioService;
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
@RequestMapping(VeterinarioApiController.BASE)
@RequiredArgsConstructor
@Tag(name = "Veterinários", description = "Profissionais da clínica (escrita restrita ao ADMIN)")
public class VeterinarioApiController {

    static final String BASE = "/api/v1/veterinarios";

    private final VeterinarioService veterinarioService;

    @GetMapping
    @Operation(summary = "Lista veterinários com busca por nome, CRMV ou especialidade")
    public PaginaResponse<VeterinarioResponse> listar(@RequestParam(defaultValue = "") String busca,
                                                      @RequestParam(defaultValue = "0") int pagina,
                                                      @RequestParam(defaultValue = "10") int tamanho) {
        return PaginaResponse.de(veterinarioService.listar(busca, Paginacao.de(pagina, tamanho, Sort.by("nome"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veterinário pelo ID")
    public VeterinarioResponse buscar(@PathVariable Long id) {
        return veterinarioService.buscar(id);
    }

    @GetMapping("/crmv/{crmv}")
    @Operation(summary = "Busca um veterinário pelo CRMV")
    public VeterinarioResponse buscarPorCrmv(@PathVariable String crmv) {
        return veterinarioService.buscarPorCrmv(crmv);
    }

    @PostMapping
    @Operation(summary = "Cadastra um veterinário")
    public ResponseEntity<VeterinarioResponse> cadastrar(
            @Validated(Cadastro.class) @RequestBody VeterinarioRequest request) {
        VeterinarioResponse veterinario = veterinarioService.cadastrar(request);
        return ResponseEntity.created(Localizacao.recurso(BASE, veterinario.getId())).body(veterinario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um veterinário")
    public VeterinarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeterinarioRequest request) {
        return veterinarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um veterinário sem atendimentos vinculados")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        veterinarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
