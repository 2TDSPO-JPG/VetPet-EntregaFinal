package br.com.fiap.vetpet.repository;

import br.com.fiap.vetpet.domain.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByPetCodeIgnoreCase(String petCode);

    @Query("""
            select p from Pet p
            where lower(p.nome) like lower(concat('%', :termo, '%'))
               or lower(p.petCode) like lower(concat('%', :termo, '%'))
               or lower(coalesce(p.raca, '')) like lower(concat('%', :termo, '%'))
            """)
    Page<Pet> buscar(@Param("termo") String termo, Pageable pageable);

    @Query("select p from Pet p join p.tutores t where lower(t.email) = lower(:email) order by p.nome")
    List<Pet> findByTutorEmail(@Param("email") String email);

    boolean existsByIdAndTutoresEmailIgnoreCase(Long id, String email);
}
