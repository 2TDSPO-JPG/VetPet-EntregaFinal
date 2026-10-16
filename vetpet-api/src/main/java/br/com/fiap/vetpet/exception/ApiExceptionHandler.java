package br.com.fiap.vetpet.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "br.com.fiap.vetpet.web.api")
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(DadoDuplicadoException.class)
    public ProblemDetail duplicado(DadoDuplicadoException ex) {
        return problema(HttpStatus.CONFLICT, "Dado já cadastrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail credenciais(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Falha na autenticação", ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail acessoNegado(AcessoNegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getObjectName(), erro.getDefaultMessage()));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos não passaram na validação.");
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail requisicaoInvalida(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Verifique o formato dos dados enviados (datas, números e valores de enum).");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        if (ex instanceof ErrorResponse erroPadrao) {
            return erroPadrao.getBody();
        }
        log.error("Erro inesperado na API", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
