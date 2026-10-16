package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.integration.NotificacaoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoViewController {

    private final NotificacaoClient notificacaoClient;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String email, Model model) {
        model.addAttribute("consulta", notificacaoClient.listar(email, 0, 30));
        model.addAttribute("email", email);
        return "notificacoes/lista";
    }
}
