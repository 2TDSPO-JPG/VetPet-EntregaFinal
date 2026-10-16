package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    List<Pergunta> findByPetIdOrderByCriadaEmDesc(Long petId);

    List<Pergunta> findTop5ByRespostaIsNullOrderByCriadaEmAsc();

    long countByRespostaIsNull();
}
