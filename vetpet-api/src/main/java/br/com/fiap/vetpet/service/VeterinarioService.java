package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Veterinario;
import br.com.fiap.vetpet.dto.request.VeterinarioRequest;
import br.com.fiap.vetpet.dto.response.VeterinarioResponse;
import br.com.fiap.vetpet.exception.DadoDuplicadoException;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.repository.ExameRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeterinarioService {

    private static final String RECURSO = "Veterinário";

    private final VeterinarioRepository veterinarioRepository;
    private final ExameRepository exameRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<VeterinarioResponse> listar(String termo, Pageable pageable) {
        return veterinarioRepository.buscar(termo == null ? "" : termo.trim(), pageable)
                .map(VeterinarioResponse::de);
    }

    public List<VeterinarioResponse> listarParaSelecao() {
        return veterinarioRepository.findAll(Sort.by("nome")).stream().map(VeterinarioResponse::de).toList();
    }

    public VeterinarioResponse buscar(Long id) {
        return VeterinarioResponse.de(encontrar(id));
    }

    public VeterinarioResponse buscarPorCrmv(String crmv) {
        return veterinarioRepository.findByCrmvIgnoreCase(crmv.trim())
                .map(VeterinarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, crmv));
    }

    public VeterinarioRequest buscarParaEdicao(Long id) {
        return VeterinarioRequest.de(encontrar(id));
    }

    @Transactional
    public VeterinarioResponse cadastrar(VeterinarioRequest request) {
        validarUnicidade(request, null);
        Veterinario veterinario = new Veterinario();
        aplicar(request, veterinario);
        veterinario.definirSenha(passwordEncoder.encode(request.getSenha()));
        return VeterinarioResponse.de(veterinarioRepository.save(veterinario));
    }

    @Transactional
    public VeterinarioResponse atualizar(Long id, VeterinarioRequest request) {
        Veterinario veterinario = encontrar(id);
        validarUnicidade(request, id);
        aplicar(request, veterinario);
        return VeterinarioResponse.de(veterinario);
    }

    @Transactional
    public void excluir(Long id) {
        Veterinario veterinario = encontrar(id);
        if (exameRepository.existsByVeterinarioId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir " + veterinario.getNome() + ": existem atendimentos vinculados a este profissional.");
        }
        veterinarioRepository.delete(veterinario);
    }

    private void aplicar(VeterinarioRequest request, Veterinario veterinario) {
        DadosPessoais.aplicar(request, veterinario);
        veterinario.setCrmv(request.getCrmv().trim().toUpperCase());
        veterinario.setEspecialidade(DadosPessoais.vazioParaNulo(request.getEspecialidade()));
        veterinario.setAnosExperiencia(request.getAnosExperiencia());
        veterinario.setBiografia(DadosPessoais.vazioParaNulo(request.getBiografia()));
    }

    private Veterinario encontrar(Long id) {
        return veterinarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    private void validarUnicidade(VeterinarioRequest request, Long id) {
        String email = DadosPessoais.normalizarEmail(request.getEmail());
        String crmv = request.getCrmv().trim();
        boolean emailEmUso = id == null
                ? veterinarioRepository.existsByEmailIgnoreCase(email)
                : veterinarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (emailEmUso) {
            throw new DadoDuplicadoException("O e-mail " + email + " já está em uso por outro veterinário.");
        }
        boolean crmvEmUso = id == null
                ? veterinarioRepository.existsByCrmvIgnoreCase(crmv)
                : veterinarioRepository.existsByCrmvIgnoreCaseAndIdNot(crmv, id);
        if (crmvEmUso) {
            throw new DadoDuplicadoException("O CRMV " + crmv + " já está cadastrado.");
        }
        String cpf = DadosPessoais.formatarCpf(request.getCpf());
        boolean cpfEmUso = cpf != null && (id == null
                ? veterinarioRepository.existsByCpf(cpf)
                : veterinarioRepository.existsByCpfAndIdNot(cpf, id));
        if (cpfEmUso) {
            throw new DadoDuplicadoException("O CPF " + cpf + " já está cadastrado para outro veterinário.");
        }
    }
}
