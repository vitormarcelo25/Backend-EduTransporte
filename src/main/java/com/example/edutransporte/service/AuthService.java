package com.example.edutransporte.service;

import com.example.edutransporte.DTOs.AuthRequest;
import com.example.edutransporte.DTOs.AuthResponse;
import com.example.edutransporte.model.Aluno;
import com.example.edutransporte.model.Motorista;
import com.example.edutransporte.model.enums.StatusAluno;
import com.example.edutransporte.model.enums.TipoUsuario;
import com.example.edutransporte.repository.AlunoRepository;
import com.example.edutransporte.repository.MotoristaRepository;
import com.example.edutransporte.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AlunoRepository alunoRepository;
    private final MotoristaRepository motoristaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String adminCode;

    public AuthService(AlunoRepository alunoRepository,
                       MotoristaRepository motoristaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${admin.code:admin26}") String adminCode) {
        this.alunoRepository = alunoRepository;
        this.motoristaRepository = motoristaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.adminCode = adminCode;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest r) {
        String cpf = normalizarCpf(r.cpf());
        String senha = r.senha();

        if (cpf.isEmpty() || senha == null || senha.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Informe o CPF e a senha.");
        }

        var alunoOpt = alunoRepository.findByCpf(cpf);
        if (alunoOpt.isPresent()) {
            Aluno aluno = alunoOpt.get();
            if (!senhaConfere(senha, aluno.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CPF ou senha inválidos.");
            }

            StatusAluno status = aluno.getStatusEfetivo();
            if (status == StatusAluno.PENDENTE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Seu cadastro ainda está em análise pela Coordenação de Transportes. Aguarde a aprovação do administrador.");
            }
            if (status == StatusAluno.RECUSADO) {
                String motivo = aluno.getMotivoRecusa() != null && !aluno.getMotivoRecusa().isBlank()
                        ? " Motivo: " + aluno.getMotivoRecusa()
                        : "";
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Seu cadastro foi recusado pela Coordenação de Transportes." + motivo);
            }

            return new AuthResponse(
                    jwtService.gerar(aluno.getCpf(), TipoUsuario.ALUNO),
                    TipoUsuario.ALUNO,
                    aluno.getCpf(),
                    aluno.getNomeCompleto(),
                    status,
                    null
            );
        }

        var motoristaOpt = motoristaRepository.findByCpf(cpf);
        if (motoristaOpt.isPresent()) {
            Motorista motorista = motoristaOpt.get();
            if (!senhaConfere(senha, motorista.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CPF ou senha inválidos.");
            }
            return new AuthResponse(
                    jwtService.gerar(motorista.getCpf(), TipoUsuario.MOTORISTA),
                    TipoUsuario.MOTORISTA,
                    motorista.getCpf(),
                    motorista.getNomeCompleto(),
                    null,
                    null
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CPF não cadastrado ou senha inválida.");
    }

    /**
     * Login do administrador. O código é validado no servidor (nunca no app)
     * e a variável de ambiente ADMIN_CODE tem prioridade sobre o valor padrão.
     */
    public AuthResponse loginAdmin(String codigo) {
        if (codigo == null || codigo.isBlank() || !codigo.equals(adminCode)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código de acesso do administrador inválido.");
        }
        return new AuthResponse(
                jwtService.gerar("admin", TipoUsuario.ADMIN),
                TipoUsuario.ADMIN,
                "admin",
                "Administrador",
                null,
                null
        );
    }

    /**
     * Aceita senhas novas em BCrypt e senhas antigas gravadas em texto puro.
     */
    private boolean senhaConfere(String senhaDigitada, String senhaArmazenada) {
        if (senhaDigitada == null || senhaArmazenada == null) {
            return false;
        }
        if (senhaArmazenada.startsWith("$2a$")
                || senhaArmazenada.startsWith("$2b$")
                || senhaArmazenada.startsWith("$2y$")) {
            try {
                return passwordEncoder.matches(senhaDigitada, senhaArmazenada);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return senhaDigitada.equals(senhaArmazenada);
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }
}
