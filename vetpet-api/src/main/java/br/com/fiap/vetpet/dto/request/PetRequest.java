package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PetRequest {

    @NotBlank(message = "Informe o nome do pet")
    @Size(max = 80)
    private String nome;

    @NotNull(message = "Informe a data de nascimento")
    @PastOrPresent(message = "A data de nascimento não pode estar no futuro")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "Selecione a espécie")
    private Especie especie;

    @Size(max = 80)
    private String raca;

    private Sexo sexo;

    @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    @DecimalMax(value = "2000.00", message = "Peso inválido")
    private BigDecimal pesoKg;

    @Size(max = 500)
    private String alergias;

    @NotEmpty(message = "Selecione ao menos um tutor")
    private List<Long> tutoresIds = new ArrayList<>();

    public static PetRequest de(Pet pet) {
        PetRequest request = new PetRequest();
        request.setNome(pet.getNome());
        request.setDataNascimento(pet.getDataNascimento());
        request.setEspecie(pet.getEspecie());
        request.setRaca(pet.getRaca());
        request.setSexo(pet.getSexo());
        request.setPesoKg(pet.getPesoKg());
        request.setAlergias(pet.getAlergias());
        request.setTutoresIds(new ArrayList<>(pet.getTutores().stream().map(Tutor::getId).toList()));
        return request;
    }
}
