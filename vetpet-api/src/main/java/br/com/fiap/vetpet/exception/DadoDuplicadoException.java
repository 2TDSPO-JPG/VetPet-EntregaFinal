package br.com.fiap.vetpet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DadoDuplicadoException extends RegraNegocioException {

    public DadoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
