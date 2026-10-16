package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Sexo;
import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.response.AcompanhamentoPetResponse;
import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.AcompanhamentoService;
import br.com.fiap.vetpet.service.PerguntaService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.service.TutorService;
import br.com.fiap.vetpet.web.Paginacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.util.Map;

@Controller
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetViewController {

    private static final String FORMULARIO = "pets/formulario";

    private final PetService petService;
    private final TutorService tutorService;
    private final AcompanhamentoService acompanhamentoService;
    private final PerguntaService perguntaService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String busca,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", petService.listar(busca,
                Paginacao.de(pagina, Paginacao.TAMANHO_PADRAO, Sort.by("nome"))));
        model.addAttribute("busca", busca);
        model.addAttribute("link", Links.comFiltros("/pets", Map.of("busca", busca)));
        return "pets/lista";
    }

    @GetMapping("/{id}")
    public String acompanhar(@PathVariable Long id, Model model) {
        AcompanhamentoPetResponse acompanhamento = acompanhamentoService.montar(id);
        model.addAttribute("acompanhamento", acompanhamento);
        model.addAttribute("pet", acompanhamento.getPet());
        model.addAttribute("tutores", tutorService.listarParaSelecao());
        model.addAttribute("novaPergunta", new PerguntaRequest());
        return "pets/detalhe";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long tutorId, Model model) {
        PetRequest form = new PetRequest();
        if (tutorId != null) {
            form.getTutoresIds().add(tutorId);
        }
        return formulario(model, form, null);
    }

    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("form") PetRequest form, BindingResult resultado, Model model,
                            RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Pet cadastrado. O acompanhamento começou!",
                () -> formulario(model, form, null),
                () -> "/pets/" + petService.cadastrar(form).getId());
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, petService.buscarParaEdicao(id), id);
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") PetRequest form,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Dados do pet atualizados.",
                () -> formulario(model, form, id),
                () -> "/pets/" + petService.atualizar(id, form).getId());
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        return Formularios.executar(atributos, "/pets", "Pet removido.", () -> petService.excluir(id));
    }

    @PostMapping("/{id}/tutores")
    public String vincularTutor(@PathVariable Long id, @RequestParam Long tutorId, RedirectAttributes atributos) {
        return Formularios.executar(atributos, "/pets/" + id, "Tutor vinculado ao pet.",
                () -> petService.vincularTutor(id, tutorId));
    }

    @PostMapping("/{id}/tutores/{tutorId}/remover")
    public String desvincularTutor(@PathVariable Long id, @PathVariable Long tutorId, RedirectAttributes atributos) {
        return Formularios.executar(atributos, "/pets/" + id, "Vínculo com o tutor removido.",
                () -> petService.desvincularTutor(id, tutorId));
    }

    @PostMapping("/{id}/perguntas")
    public String perguntar(@PathVariable Long id, @Valid @ModelAttribute("novaPergunta") PerguntaRequest form,
                            BindingResult resultado, @AuthenticationPrincipal UsuarioAutenticado usuario,
                            RedirectAttributes atributos) {
        return Formularios.executarValidado(resultado, atributos, "/pets/" + id + "#aba-perguntas",
                "Pergunta registrada.",
                () -> perguntaService.criar(id, form, usuario.getNome()));
    }

    private String formulario(Model model, PetRequest form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("petId", id);
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tutores", tutorService.listarParaSelecao());
        return FORMULARIO;
    }
}
