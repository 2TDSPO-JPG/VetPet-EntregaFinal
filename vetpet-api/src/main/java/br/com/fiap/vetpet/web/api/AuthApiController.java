package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.request.LoginRequest;
import br.com.fiap.vetpet.dto.response.UsuarioResponse;
import br.com.fiap.vetpet.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Autenticação", description = "Validação de credenciais de tutores e veterinários")
public class AuthApiController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/tutores/login")
    @Operation(summary = "Valida e-mail e senha de um tutor")
    public UsuarioResponse loginTutor(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.autenticarTutor(request);
    }

    @PostMapping("/veterinarios/login")
    @Operation(summary = "Valida e-mail e senha de um veterinário")
    public UsuarioResponse loginVeterinario(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.autenticarVeterinario(request);
    }
}
