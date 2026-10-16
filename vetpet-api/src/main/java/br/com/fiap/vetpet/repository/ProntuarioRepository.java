package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Prontuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    boolean existsByExameId(Long exameId);

    Optional<Prontuario> findByExameId(Long exameId);

    @Query("select p from Prontuario p where p.exame.pet.id = :petId order by p.registradoEm desc")
    List<Prontuario> findByPetId(@Param("petId") Long petId);

    @Query("""
            select p from Prontuario p
            where (:petId is null or p.exame.pet.id = :petId)
              and (:veterinarioId is null or p.exame.veterinario.id = :veterinarioId)
            """)
    Page<Prontuario> filtrar(@Param("petId") Long petId,
                             @Param("veterinarioId") Long veterinarioId,
                             Pageable pageable);

    @Query("select max(e.dataHora) from Prontuario p join p.exame e where e.pet.id = :petId")
    Optional<LocalDateTime> ultimoAtendimento(@Param("petId") Long petId);
}
