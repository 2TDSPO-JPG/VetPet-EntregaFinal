package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.dto.request.ProntuarioRequest;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.service.ProntuarioService;
import br.com.fiap.vetpet.web.Paginacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prontuarios")
@RequiredArgsConstructor
public class ProntuarioViewController {

    private static final String FORMULARIO = "prontuarios/formulario";

    private final ProntuarioService prontuarioService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", prontuarioService.listar(null, null,
                Paginacao.de(pagina, Paginacao.TAMANHO_PADRAO, Sort.by(Sort.Direction.DESC, "registradoEm"))));
        model.addAttribute("link", "/prontuarios");
        return "prontuarios/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        model.addAttribute("prontuario", prontuarioService.buscar(id));
        return "prontuarios/detalhe";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam Long exameId, Model model, RedirectAttributes atributos) {
        ProntuarioRequest form = new ProntuarioRequest();
        form.setExameId(exameId);
        try {
            return formulario(model, form, null);
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute(Formularios.ERRO, e.getMessage());
            return "redirect:/atendimentos";
        }
    }

    @PostMapping("/novo")
    public String registrar(@Valid @ModelAttribute("form") ProntuarioRequest form, BindingResult resultado,
                            Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Prontuário registrado e atendimento concluído.",
                () -> formulario(model, form, null),
                () -> "/prontuarios/" + prontuarioService.registrar(form).getId());
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, prontuarioService.buscarParaEdicao(id), id);
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ProntuarioRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Prontuário atualizado.",
                () -> formulario(model, form, id),
                () -> "/prontuarios/" + prontuarioService.atualizar(id, form).getId());
    }

    private String formulario(Model model, ProntuarioRequest form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("prontuarioId", id);
        model.addAttribute("atendimento", id == null
                ? prontuarioService.buscarAtendimentoParaRegistro(form.getExameId())
                : null);
        if (id != null) {
            model.addAttribute("prontuario", prontuarioService.buscar(id));
        }
        return FORMULARIO;
    }
}
