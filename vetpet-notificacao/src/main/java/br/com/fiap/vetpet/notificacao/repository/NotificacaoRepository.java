package br.com.fiap.vetpet.notificacao.repository;

import br.com.fiap.vetpet.notificacao.domain.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    boolean existsByEventoId(String eventoId);

    Page<Notificacao> findByDestinatariosContainingIgnoreCase(String email, Pageable pageable);
}
