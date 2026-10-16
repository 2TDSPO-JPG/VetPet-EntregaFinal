package br.com.fiap.vetpet.security;

import br.com.fiap.vetpet.config.AdminProperties;
import br.com.fiap.vetpet.domain.entity.Pessoa;
import br.com.fiap.vetpet.domain.enums.Perfil;
import br.com.fiap.vetpet.repository.TutorRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private static final String NOME_ADMIN = "Administrador VetPet";

    private final VeterinarioRepository veterinarioRepository;
    private final TutorRepository tutorRepository;
    private final String adminEmail;
    private final String adminSenhaHash;

    public UsuarioDetailsService(VeterinarioRepository veterinarioRepository, TutorRepository tutorRepository,
                                 AdminProperties adminProperties, PasswordEncoder passwordEncoder) {
        this.veterinarioRepository = veterinarioRepository;
        this.tutorRepository = tutorRepository;
        this.adminEmail = adminProperties.email().trim().toLowerCase();
        this.adminSenhaHash = passwordEncoder.encode(adminProperties.senha());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String email = username == null ? "" : username.trim().toLowerCase();
        if (email.equals(adminEmail)) {
            return new UsuarioAutenticado(adminEmail, adminSenhaHash, NOME_ADMIN, Perfil.ADMIN);
        }
        return converter(veterinarioRepository.findByEmailIgnoreCase(email), Perfil.VETERINARIO)
                .or(() -> converter(tutorRepository.findByEmailIgnoreCase(email), Perfil.TUTOR))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    private Optional<UserDetails> converter(Optional<? extends Pessoa> pessoa, Perfil perfil) {
        return pessoa
                .filter(p -> p.getCredencial() != null)
                .map(p -> new UsuarioAutenticado(p.getEmail(), p.getCredencial().getSenhaHash(), p.getNome(), perfil));
    }
}
