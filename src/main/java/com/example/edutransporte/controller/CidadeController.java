package com.example.edutransporte.controller;

import com.example.edutransporte.DTOs.CidadeResponse;
import com.example.edutransporte.repository.CidadeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cidades")
public class CidadeController {

    private final CidadeRepository cidadeRepository;

    public CidadeController(CidadeRepository cidadeRepository) {
        this.cidadeRepository = cidadeRepository;
    }

    /** Lista as cidades cadastradas para preencher o select do cadastro. */
    @GetMapping
    public List<CidadeResponse> listar() {
        return cidadeRepository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(c -> new CidadeResponse(c.getId(), c.getNome()))
                .toList();
    }
}
