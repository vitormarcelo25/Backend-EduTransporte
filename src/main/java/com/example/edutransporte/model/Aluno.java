package com.example.edutransporte.model;

import com.example.edutransporte.model.enums.StatusAluno;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
public class Aluno {

    @Id
    @Column(columnDefinition = "CHAR(11)", length = 11)
    private String cpf;

    @Column(name = "nome_completo", nullable = false, length = 80)
    private String nomeCompleto;

    @Column(name = "numero_celular", nullable = false, length = 15)
    private String numeroCelular;

    @Column(nullable = false, length = 100)
    private String senha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidade_id", nullable = false)
    private Cidade cidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculdade_id", nullable = false)
    private Faculdade faculdade;

    /**
     * Situação do cadastro. O default PENDENTE garante que todo novo aluno
     * precise da aprovação do administrador antes de entrar no app.
     * A coluna fica anulável apenas para compatibilidade com registros antigos;
     * registros sem status são tratados como PENDENTE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, columnDefinition = "varchar(20) default 'PENDENTE'")
    private StatusAluno status = StatusAluno.PENDENTE;

    @Column(name = "motivo_recusa", length = 255)
    private String motivoRecusa;

    /** Nunca retorna null: cadastros antigos sem status contam como pendentes. */
    @Transient
    public StatusAluno getStatusEfetivo() {
        return status == null ? StatusAluno.PENDENTE : status;
    }
}
