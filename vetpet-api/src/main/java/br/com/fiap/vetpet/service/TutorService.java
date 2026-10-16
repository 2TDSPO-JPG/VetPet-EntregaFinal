package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.AlterarSenhaRequest;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.dto.response.TutorResumoResponse;
import br.com.fiap.vetpet.exception.CredenciaisInvalidasException;
import br.com.fiap.vetpet.exception.DadoDuplicadoException;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.TutorRepository;
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
public class TutorService {

    private static final String RECURSO = "Tutor";

    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventoPublisher eventoPublisher;

    public Page<TutorResponse> listar(String termo, Pageable pageable) {
        return tutorRepository.buscar(termo == null ? "" : termo.trim(), pageable).map(TutorResponse::de);
    }

    public List<TutorResumoResponse> listarParaSelecao() {
        return tutorRepository.findAll(Sort.by("nome")).stream().map(TutorResumoResponse::de).toList();
    }

    public TutorResponse buscar(Long id) {
        return TutorResponse.de(encontrar(id));
    }

    public TutorResponse buscarPorEmail(String email) {
        return tutorRepository.findByEmailIgnoreCase(DadosPessoais.normalizarEmail(email))
                .map(TutorResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, email));
    }

    public TutorResponse buscarPorCpf(String cpf) {
        String cpfFormatado = DadosPessoais.formatarCpf(cpf);
        return tutorRepository.findByCpf(cpfFormatado)
                .map(TutorResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, cpf));
    }

    public TutorRequest buscarParaEdicao(Long id) {
        return TutorRequest.de(encontrar(id));
    }

    @Transactional
    public TutorResponse cadastrar(TutorRequest request) {
        validarUnicidade(request, null);
        Tutor tutor = new Tutor();
        DadosPessoais.aplicar(request, tutor);
        tutor.definirSenha(passwordEncoder.encode(request.getSenha()));
        Tutor salvo = tutorRepository.save(tutor);
        eventoPublisher.publicar(EventoVetPet.semPet(TipoEvento.TUTOR_CADASTRADO, List.of(salvo.getEmail()),
                "Bem-vindo(a) ao VetPet, " + salvo.getNome() + "! Seu cadastro foi concluído."));
        return TutorResponse.de(salvo);
    }

    @Transactional
    public TutorResponse atualizar(Long id, TutorRequest request) {
        Tutor tutor = encontrar(id);
        validarUnicidade(request, id);
        DadosPessoais.aplicar(request, tutor);
        return TutorResponse.de(tutor);
    }

    @Transactional
    public void alterarSenha(String email, AlterarSenhaRequest request) {
        Tutor tutor = tutorRepository.findByEmailIgnoreCase(DadosPessoais.normalizarEmail(email))
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, email));
        if (!passwordEncoder.matches(request.senhaAtual(), tutor.getCredencial().getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        tutor.definirSenha(passwordEncoder.encode(request.novaSenha()));
    }

    @Transactional
    public void excluir(Long id) {
        Tutor tutor = encontrar(id);
        List.copyOf(tutor.getPets()).forEach((Pet pet) -> pet.removerTutor(tutor));
        tutorRepository.delete(tutor);
    }

    private Tutor encontrar(Long id) {
        return tutorRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    private void validarUnicidade(TutorRequest request, Long id) {
        String email = DadosPessoais.normalizarEmail(request.getEmail());
        String cpf = DadosPessoais.formatarCpf(request.getCpf());
        boolean emailEmUso = id == null
                ? tutorRepository.existsByEmailIgnoreCase(email)
                : tutorRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (emailEmUso) {
            throw new DadoDuplicadoException("O e-mail " + email + " já está em uso por outro tutor.");
        }
        boolean cpfEmUso = cpf != null && (id == null
                ? tutorRepository.existsByCpf(cpf)
                : tutorRepository.existsByCpfAndIdNot(cpf, id));
        if (cpfEmUso) {
            throw new DadoDuplicadoException("O CPF " + cpf + " já está cadastrado.");
        }
    }
}
