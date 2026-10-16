package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Prontuario;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.ProntuarioRequest;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.dto.response.ProntuarioResponse;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.ProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProntuarioService {

    private static final String RECURSO = "Prontuário";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProntuarioRepository prontuarioRepository;
    private final ExameService exameService;
    private final EventoPublisher eventoPublisher;

    public Page<ProntuarioResponse> listar(Long petId, Long veterinarioId, Pageable pageable) {
        return prontuarioRepository.filtrar(petId, veterinarioId, pageable).map(ProntuarioResponse::de);
    }

    public List<ProntuarioResponse> listarDoPet(Long petId) {
        return prontuarioRepository.findByPetId(petId).stream().map(ProntuarioResponse::de).toList();
    }

    public ProntuarioResponse buscar(Long id) {
        return ProntuarioResponse.de(encontrar(id));
    }

    public ProntuarioRequest buscarParaEdicao(Long id) {
        return ProntuarioRequest.de(encontrar(id));
    }

    public ExameResponse buscarAtendimentoParaRegistro(Long exameId) {
        Exame exame = exameService.encontrar(exameId);
        validarAtendimentoAberto(exame);
        return ExameResponse.de(exame);
    }

    @Transactional
    public ProntuarioResponse registrar(ProntuarioRequest request) {
        Exame exame = exameService.encontrar(request.getExameId());
        validarAtendimentoAberto(exame);
        Prontuario prontuario = new Prontuario();
        prontuario.setExame(exame);
        aplicar(request, prontuario);
        exame.setProntuario(prontuario);
        exame.setStatus(StatusExame.REALIZADO);
        if (request.getPesoAferidoKg() != null) {
            exame.getPet().setPesoKg(request.getPesoAferidoKg());
        }
        Prontuario salvo = prontuarioRepository.save(prontuario);
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.PRONTUARIO_REGISTRADO, exame.getPet(), descreverAtendimento(salvo)));
        return ProntuarioResponse.de(salvo);
    }

    @Transactional
    public ProntuarioResponse atualizar(Long id, ProntuarioRequest request) {
        Prontuario prontuario = encontrar(id);
        aplicar(request, prontuario);
        if (request.getPesoAferidoKg() != null) {
            prontuario.getExame().getPet().setPesoKg(request.getPesoAferidoKg());
        }
        return ProntuarioResponse.de(prontuario);
    }

    private void validarAtendimentoAberto(Exame exame) {
        if (prontuarioRepository.existsByExameId(exame.getId())) {
            throw new RegraNegocioException("Este atendimento já possui prontuário registrado.");
        }
        if (exame.getStatus() == StatusExame.CANCELADO) {
            throw new RegraNegocioException("Não é possível registrar prontuário de um atendimento cancelado.");
        }
    }

    private void aplicar(ProntuarioRequest request, Prontuario prontuario) {
        prontuario.setDiagnostico(request.getDiagnostico().trim());
        prontuario.setPrescricao(DadosPessoais.vazioParaNulo(request.getPrescricao()));
        prontuario.setExamesSolicitados(DadosPessoais.vazioParaNulo(request.getExamesSolicitados()));
        prontuario.setObservacoes(DadosPessoais.vazioParaNulo(request.getObservacoes()));
        prontuario.setPesoAferidoKg(request.getPesoAferidoKg());
        prontuario.setProximoRetorno(request.getProximoRetorno());
    }

    private String descreverAtendimento(Prontuario prontuario) {
        Exame exame = prontuario.getExame();
        StringBuilder descricao = new StringBuilder()
                .append(exame.getTipo().getDescricao())
                .append(" realizado(a) por ")
                .append(exame.getVeterinario().getNome())
                .append(". Diagnóstico: ")
                .append(prontuario.getDiagnostico());
        if (prontuario.getProximoRetorno() != null) {
            descricao.append(". Retorno recomendado em ").append(prontuario.getProximoRetorno().format(FORMATO_DATA));
        }
        return descricao.append('.').toString();
    }

    private Prontuario encontrar(Long id) {
        return prontuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }
}
