package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExameRepository extends JpaRepository<Exame, Long> {

    @Query("""
            select e from Exame e
            where (:status is null or e.status = :status)
              and (:petId is null or e.pet.id = :petId)
              and (:veterinarioId is null or e.veterinario.id = :veterinarioId)
              and (:inicio is null or e.dataHora >= :inicio)
              and (:fim is null or e.dataHora < :fim)
            """)
    Page<Exame> filtrar(@Param("status") StatusExame status,
                        @Param("petId") Long petId,
                        @Param("veterinarioId") Long veterinarioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim,
                        Pageable pageable);

    List<Exame> findByPetIdOrderByDataHoraDesc(Long petId);

    Optional<Exame> findFirstByPetIdAndStatusInAndDataHoraAfterOrderByDataHoraAsc(
            Long petId, Collection<StatusExame> status, LocalDateTime dataHora);

    List<Exame> findTop5ByStatusInAndDataHoraAfterOrderByDataHoraAsc(
            Collection<StatusExame> status, LocalDateTime dataHora);

    List<Exame> findByStatusInAndLembreteEnviadoFalseAndDataHoraBetween(
            Collection<StatusExame> status, LocalDateTime inicio, LocalDateTime fim);

    long countByStatusAndDataHoraBetween(StatusExame status, LocalDateTime inicio, LocalDateTime fim);

    long countByStatus(StatusExame status);

    boolean existsByVeterinarioId(Long veterinarioId);

    boolean existsByVeterinarioIdAndDataHoraAndStatusIn(Long veterinarioId, LocalDateTime dataHora,
                                                        Collection<StatusExame> status);

    @Query("select e from Exame e join e.pet p join p.tutores t where lower(t.email) = lower(:email) order by e.dataHora desc")
    List<Exame> findByTutorEmail(@Param("email") String email);
}
