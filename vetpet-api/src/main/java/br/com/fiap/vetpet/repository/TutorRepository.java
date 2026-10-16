package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByEmailIgnoreCase(String email);

    Optional<Tutor> findByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    @Query("""
            select t from Tutor t
            where lower(t.nome) like lower(concat('%', :termo, '%'))
               or lower(t.email) like lower(concat('%', :termo, '%'))
               or t.cpf like concat('%', :termo, '%')
            """)
    Page<Tutor> buscar(@Param("termo") String termo, Pageable pageable);
}
