package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Sexo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PetResponse {

    private Long id;
    private String petCode;
    private String nome;
    private LocalDate dataNascimento;
    private int idade;
    private Especie especie;
    private String raca;
    private Sexo sexo;
    private BigDecimal pesoKg;
    private String alergias;
    private LocalDateTime criadoEm;
    private List<TutorResumoResponse> tutores;

    public static PetResponse de(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .petCode(pet.getPetCode())
                .nome(pet.getNome())
                .dataNascimento(pet.getDataNascimento())
                .idade(pet.calcularIdade())
                .especie(pet.getEspecie())
                .raca(pet.getRaca())
                .sexo(pet.getSexo())
                .pesoKg(pet.getPesoKg())
                .alergias(pet.getAlergias())
                .criadoEm(pet.getCriadoEm())
                .tutores(pet.getTutores().stream().map(TutorResumoResponse::de).toList())
                .build();
    }
}
