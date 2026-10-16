package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.enums.Especie;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PetResumoResponse {

    private Long id;
    private String petCode;
    private String nome;
    private Especie especie;
    private String raca;
    private int idade;

    public static PetResumoResponse de(Pet pet) {
        return new PetResumoResponse(pet.getId(), pet.getPetCode(), pet.getNome(), pet.getEspecie(),
                pet.getRaca(), pet.calcularIdade());
    }
}
