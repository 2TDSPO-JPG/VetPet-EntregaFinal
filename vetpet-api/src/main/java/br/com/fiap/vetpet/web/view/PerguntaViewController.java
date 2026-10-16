package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.dto.request.RespostaRequest;
import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.PerguntaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perguntas")
@RequiredArgsConstructor
public class PerguntaViewController {

    private static final String ABA_PERGUNTAS = "#aba-perguntas";

    private final PerguntaService perguntaService;

    @PostMapping("/{id}/responder")
    public String responder(@PathVariable Long id, @Valid @ModelAttribute RespostaRequest form,
                            BindingResult resultado, @AuthenticationPrincipal UsuarioAutenticado usuario,
                            RedirectAttributes atributos) {
        Long petId = perguntaService.buscar(id).getPetId();
        return Formularios.executarValidado(resultado, atributos, "/pets/" + petId + ABA_PERGUNTAS,
                "Resposta enviada ao tutor.",
                () -> perguntaService.responder(id, form, usuario.getNome()));
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        Long petId = perguntaService.excluir(id);
        atributos.addFlashAttribute(Formularios.SUCESSO, "Pergunta removida.");
        return "redirect:/pets/" + petId + ABA_PERGUNTAS;
    }
}
