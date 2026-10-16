package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.security.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "br.com.fiap.vetpet.web.view")
public class UsuarioLogadoAdvice {

    @ModelAttribute("usuario")
    public UsuarioAutenticado usuario(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }
}
