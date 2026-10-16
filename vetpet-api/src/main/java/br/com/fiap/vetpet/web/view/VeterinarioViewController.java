package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.dto.request.Cadastro;
import br.com.fiap.vetpet.dto.request.VeterinarioRequest;
import br.com.fiap.vetpet.service.VeterinarioService;
import br.com.fiap.vetpet.web.Paginacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/veterinarios")
@RequiredArgsConstructor
public class VeterinarioViewController {

    private static final String FORMULARIO = "veterinarios/formulario";

    private final VeterinarioService veterinarioService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String busca,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", veterinarioService.listar(busca,
                Paginacao.de(pagina, Paginacao.TAMANHO_PADRAO, Sort.by("nome"))));
        model.addAttribute("busca", busca);
        model.addAttribute("link", Links.comFiltros("/veterinarios", Map.of("busca", busca)));
        return "veterinarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return formulario(model, new VeterinarioRequest(), null);
    }

    @PostMapping("/novo")
    public String cadastrar(@Validated(Cadastro.class) @ModelAttribute("form") VeterinarioRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Veterinário cadastrado com sucesso.",
                () -> formulario(model, form, null),
                () -> {
                    veterinarioService.cadastrar(form);
                    return "/veterinarios";
                });
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, veterinarioService.buscarParaEdicao(id), id);
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") VeterinarioRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Dados do veterinário atualizados.",
                () -> formulario(model, form, id),
                () -> {
                    veterinarioService.atualizar(id, form);
                    return "/veterinarios";
                });
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        return Formularios.executar(atributos, "/veterinarios", "Veterinário removido.",
                () -> veterinarioService.excluir(id));
    }

    private String formulario(Model model, VeterinarioRequest form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("veterinarioId", id);
        return FORMULARIO;
    }
}
