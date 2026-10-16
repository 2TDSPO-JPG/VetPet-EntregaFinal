package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pessoa;
import br.com.fiap.vetpet.domain.enums.Perfil;
import br.com.fiap.vetpet.dto.request.LoginRequest;
import br.com.fiap.vetpet.dto.response.UsuarioResponse;
import br.com.fiap.vetpet.exception.CredenciaisInvalidasException;
import br.com.fiap.vetpet.repository.TutorRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AutenticacaoService {

    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse autenticarTutor(LoginRequest request) {
        return autenticar(tutorRepository.findByEmailIgnoreCase(DadosPessoais.normalizarEmail(request.email())),
                request.senha(), Perfil.TUTOR);
    }

    public UsuarioResponse autenticarVeterinario(LoginRequest request) {
        return autenticar(veterinarioRepository.findByEmailIgnoreCase(DadosPessoais.normalizarEmail(request.email())),
                request.senha(), Perfil.VETERINARIO);
    }

    private UsuarioResponse autenticar(Optional<? extends Pessoa> usuario, String senha, Perfil perfil) {
        Pessoa pessoa = usuario
                .filter(p -> p.getCredencial() != null)
                .filter(p -> passwordEncoder.matches(senha, p.getCredencial().getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        pessoa.registrarAcesso();
        return new UsuarioResponse(pessoa.getId(), pessoa.getNome(), pessoa.getEmail(), perfil);
    }
}
