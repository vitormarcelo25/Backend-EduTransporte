package com.example.edutransporte.service;

import com.example.edutransporte.DTOs.AlunoRequest;
import com.example.edutransporte.DTOs.AlunoResponse;
import com.example.edutransporte.DTOs.LoginDTO;
import com.example.edutransporte.model.Aluno;
import com.example.edutransporte.model.Cidade;
import com.example.edutransporte.model.Faculdade;
import com.example.edutransporte.model.enums.StatusAluno;
import com.example.edutransporte.repository.AlunoRepository;
import com.example.edutransporte.repository.CidadeRepository;
import com.example.edutransporte.repository.FaculdadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final CidadeRepository cidadeRepository;
    private final FaculdadeRepository faculdadeRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository, CidadeRepository cidadeRepository,
                        FaculdadeRepository faculdadeRepository, PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.cidadeRepository = cidadeRepository;
        this.faculdadeRepository = faculdadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listarTodos() {
        return alunoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<AlunoResponse> buscarPorCpf(String cpf) {
        return alunoRepository.findById(normalizarCpf(cpf)).map(this::toResponse);
    }

    @Transactional
    public AlunoResponse criar(AlunoRequest r) {
        String cpf = normalizarCpf(r.cpf());
        if (cpf.length() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF deve conter 11 dígitos.");
        }
        if (r.nomeCompleto() == null || r.nomeCompleto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome completo.");
        }
        if (r.senha() == null || r.senha().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 4 caracteres.");
        }
        if (alunoRepository.existsById(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        Cidade cidade = cidadeRepository.findById(r.cidadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cidade não encontrada."));
        Faculdade faculdade = faculdadeRepository.findById(r.faculdadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculdade não encontrada."));

        Aluno a = new Aluno();
        a.setCpf(cpf);
        a.setNomeCompleto(r.nomeCompleto().trim());
        a.setNumeroCelular(r.numeroCelular() == null ? "" : r.numeroCelular().replaceAll("\\D", ""));
        a.setSenha(passwordEncoder.encode(r.senha()));
        a.setCidade(cidade);
        a.setFaculdade(faculdade);
        a.setStatus(StatusAluno.PENDENTE);
        a.setMotivoRecusa(null);
        return toResponse(alunoRepository.save(a));
    }

    @Transactional
    public void deletar(String cpf) {
        alunoRepository.deleteById(normalizarCpf(cpf));
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listarSolicitacoes() {
        return alunoRepository.findAll().stream()
                .filter(a -> a.getStatusEfetivo() == StatusAluno.PENDENTE)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlunoResponse aprovar(String cpf) {
        Aluno aluno = buscarEntidade(cpf);
        aluno.setStatus(StatusAluno.APROVADO);
        aluno.setMotivoRecusa(null);
        return toResponse(alunoRepository.save(aluno));
    }

    @Transactional
    public AlunoResponse recusar(String cpf, String motivo) {
        Aluno aluno = buscarEntidade(cpf);
        aluno.setStatus(StatusAluno.RECUSADO);
        aluno.setMotivoRecusa(motivo == null || motivo.isBlank() ? "Documentação não conferida." : motivo.trim());
        return toResponse(alunoRepository.save(aluno));
    }

    public boolean login(LoginDTO login) {
        Aluno aluno = alunoRepository.findByCpf(normalizarCpf(login.cpf()))
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));
        return passwordEncoder.matches(login.senha(), aluno.getSenha());
    }

    private Aluno buscarEntidade(String cpf) {
        return alunoRepository.findById(normalizarCpf(cpf))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    private AlunoResponse toResponse(Aluno a) {
        return new AlunoResponse(
                a.getCpf(),
                a.getNomeCompleto(),
                a.getNumeroCelular(),
                a.getCidade().getId(),
                a.getCidade().getNome(),
                a.getFaculdade().getId(),
                a.getFaculdade().getNome(),
                a.getStatusEfetivo(),
                a.getMotivoRecusa()
        );
    }
}
