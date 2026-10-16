package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Veterinario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    Optional<Veterinario> findByEmailIgnoreCase(String email);

    Optional<Veterinario> findByCrmvIgnoreCase(String crmv);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCrmvIgnoreCase(String crmv);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCrmvIgnoreCaseAndIdNot(String crmv, Long id);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    @Query("""
            select v from Veterinario v
            where lower(v.nome) like lower(concat('%', :termo, '%'))
               or lower(v.crmv) like lower(concat('%', :termo, '%'))
               or lower(coalesce(v.especialidade, '')) like lower(concat('%', :termo, '%'))
            """)
    Page<Veterinario> buscar(@Param("termo") String termo, Pageable pageable);
}
