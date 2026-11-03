package com.example.edutransporte.DTOs;

import com.example.edutransporte.model.enums.StatusAluno;

public record AlunoResponse(
        String cpf,
        String nomeCompleto,
        String numeroCelular,
        Integer cidadeId,
        String cidadeNome,
        Integer faculdadeId,
        String faculdadeNome,
        StatusAluno status,
        String motivoRecusa
) {}
