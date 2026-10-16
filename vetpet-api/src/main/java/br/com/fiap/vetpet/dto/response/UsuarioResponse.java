package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.enums.Perfil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private Perfil perfil;
}
