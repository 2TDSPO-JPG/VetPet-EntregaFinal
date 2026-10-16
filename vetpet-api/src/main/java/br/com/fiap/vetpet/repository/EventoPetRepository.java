package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.EventoPet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoPetRepository extends JpaRepository<EventoPet, Long> {

    boolean existsByEventoId(String eventoId);

    List<EventoPet> findByPetIdOrderByOcorridoEmDesc(Long petId);
}
