package br.com.fiap.vetpet.web.view;

import br.com.fiap.vetpet.exception.RegraNegocioException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

final class Formularios {

    static final String SUCESSO = "sucesso";
    static final String ERRO = "erro";

    private Formularios() {
    }

    static String salvar(BindingResult resultado, RedirectAttributes atributos, String mensagemSucesso,
                         Supplier<String> exibirFormulario, Supplier<String> acao) {
        if (resultado.hasErrors()) {
            return exibirFormulario.get();
        }
        try {
            String destino = acao.get();
            atributos.addFlashAttribute(SUCESSO, mensagemSucesso);
            return "redirect:" + destino;
        } catch (RegraNegocioException e) {
            resultado.reject("negocio", e.getMessage());
            return exibirFormulario.get();
        }
    }

    static String executarValidado(BindingResult resultado, RedirectAttributes atributos, String destino,
                                   String mensagemSucesso, Runnable acao) {
        if (resultado.hasErrors()) {
            atributos.addFlashAttribute(ERRO, resultado.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:" + destino;
        }
        return executar(atributos, destino, mensagemSucesso, acao);
    }

    static String executar(RedirectAttributes atributos, String destino, String mensagemSucesso, Runnable acao) {
        try {
            acao.run();
            atributos.addFlashAttribute(SUCESSO, mensagemSucesso);
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute(ERRO, e.getMessage());
        }
        return "redirect:" + destino;
    }
}
