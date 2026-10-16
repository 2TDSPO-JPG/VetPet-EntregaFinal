package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.dto.request.Cadastro;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.service.TutorService;
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
@RequestMapping("/tutores")
@RequiredArgsConstructor
public class TutorViewController {

    private static final String FORMULARIO = "tutores/formulario";

    private final TutorService tutorService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String busca,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", tutorService.listar(busca,
                Paginacao.de(pagina, Paginacao.TAMANHO_PADRAO, Sort.by("nome"))));
        model.addAttribute("busca", busca);
        model.addAttribute("link", Links.comFiltros("/tutores", Map.of("busca", busca)));
        return "tutores/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        model.addAttribute("tutor", tutorService.buscar(id));
        return "tutores/detalhe";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return formulario(model, new TutorRequest(), null);
    }

    @PostMapping("/novo")
    public String cadastrar(@Validated(Cadastro.class) @ModelAttribute("form") TutorRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Tutor cadastrado com sucesso.",
                () -> formulario(model, form, null),
                () -> "/tutores/" + tutorService.cadastrar(form).getId());
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, tutorService.buscarParaEdicao(id), id);
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") TutorRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Dados do tutor atualizados.",
                () -> formulario(model, form, id),
                () -> "/tutores/" + tutorService.atualizar(id, form).getId());
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        return Formularios.executar(atributos, "/tutores", "Tutor removido.", () -> tutorService.excluir(id));
    }

    private String formulario(Model model, TutorRequest form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("tutorId", id);
        return FORMULARIO;
    }
}
