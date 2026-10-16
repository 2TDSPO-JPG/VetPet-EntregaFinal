package br.com.fiap.vetpet.security;

import br.com.fiap.vetpet.domain.enums.Perfil;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UsuarioAutenticado extends User {

    private final String nome;
    private final Perfil perfil;

    public UsuarioAutenticado(String email, String senhaHash, String nome, Perfil perfil) {
        super(email, senhaHash, List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));
        this.nome = nome;
        this.perfil = perfil;
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}
