package br.com.fiap.vetpet.notificacao.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    static final String CABECALHO = "X-API-KEY";

    private final byte[] chaveEsperada;

    public ApiKeyFilter(NotificacaoProperties properties) {
        this.chaveEsperada = properties.apiKey().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String chave = request.getHeader(CABECALHO);
        if (chave == null || !MessageDigest.isEqual(chaveEsperada, chave.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"erro\":\"Informe uma chave de API válida no cabeçalho X-API-KEY.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
