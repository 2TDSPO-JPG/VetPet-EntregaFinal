package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pessoa;
import br.com.fiap.vetpet.dto.request.PessoaRequest;

final class DadosPessoais {

    private DadosPessoais() {
    }

    static void aplicar(PessoaRequest request, Pessoa pessoa) {
        pessoa.setNome(request.getNome().trim());
        pessoa.setCpf(formatarCpf(request.getCpf()));
        pessoa.setEmail(normalizarEmail(request.getEmail()));
        pessoa.setTelefone(vazioParaNulo(request.getTelefone()));
        pessoa.setFotoUrl(vazioParaNulo(request.getFotoUrl()));
        pessoa.setEndereco(request.getEndereco() != null ? request.getEndereco().paraEntidade() : null);
    }

    static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    static String formatarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            return cpf.trim();
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
