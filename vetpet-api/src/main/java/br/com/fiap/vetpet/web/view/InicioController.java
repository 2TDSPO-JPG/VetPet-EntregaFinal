package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InicioController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null ? "redirect:/dashboard" : "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("resumo", dashboardService.resumo());
        return "dashboard";
    }
}
