package com.example.edutransporte.DTOs;

import com.example.edutransporte.model.enums.StatusAluno;
import com.example.edutransporte.model.enums.TipoUsuario;

/**
 * Resposta do login. Além do token, devolve os dados básicos do usuário
 * para que o app monte o perfil sem depender de dados fictícios.
 */
public record AuthResponse(
        String token,
        TipoUsuario tipo,
        String cpf,
        String nomeCompleto,
        StatusAluno status,
        String motivoRecusa
) {
}
