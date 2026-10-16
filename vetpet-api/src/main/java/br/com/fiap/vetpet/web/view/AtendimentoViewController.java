package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.service.ExameService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.service.VeterinarioService;
import br.com.fiap.vetpet.web.Paginacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/atendimentos")
@RequiredArgsConstructor
public class AtendimentoViewController {

    private final ExameService exameService;
    private final PetService petService;
    private final VeterinarioService veterinarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) StatusExame status,
                         @RequestParam(required = false) Long veterinarioId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", exameService.listar(status, null, veterinarioId, data,
                Paginacao.de(pagina, Paginacao.TAMANHO_PADRAO, Sort.by(Sort.Direction.DESC, "dataHora"))));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("veterinarioSelecionado", veterinarioId);
        model.addAttribute("dataSelecionada", data);
        model.addAttribute("statusDisponiveis", StatusExame.values());
        model.addAttribute("veterinarios", veterinarioService.listarParaSelecao());
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("status", status);
        filtros.put("veterinarioId", veterinarioId);
        filtros.put("data", data);
        model.addAttribute("link", Links.comFiltros("/atendimentos", filtros));
        return "atendimentos/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long petId, Model model) {
        ExameRequest form = new ExameRequest();
        form.setPetId(petId);
        return formulario(model, form);
    }

    @PostMapping("/novo")
    public String agendar(@Valid @ModelAttribute("form") ExameRequest form, BindingResult resultado, Model model,
                          RedirectAttributes atributos) {
        return Formularios.salvar(resultado, atributos, "Atendimento agendado. Os tutores serão avisados.",
                () -> formulario(model, form),
                () -> "/pets/" + exameService.agendar(form).getPetId() + "#aba-atendimentos");
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusExame status,
                                @RequestParam(defaultValue = "/atendimentos") String voltarPara,
                                RedirectAttributes atributos) {
        return Formularios.executar(atributos, destinoSeguro(voltarPara),
                "Atendimento marcado como " + status.getDescricao().toLowerCase() + ".",
                () -> exameService.alterarStatus(id, status));
    }

    private String formulario(Model model, ExameRequest form) {
        model.addAttribute("form", form);
        model.addAttribute("pets", petService.listarParaSelecao());
        model.addAttribute("veterinarios", veterinarioService.listarParaSelecao());
        model.addAttribute("tipos", TipoAtendimento.values());
        return "atendimentos/formulario";
    }

    private String destinoSeguro(String destino) {
        return destino.startsWith("/") && !destino.startsWith("//") ? destino : "/atendimentos";
    }
}
