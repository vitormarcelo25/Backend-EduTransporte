package com.example.edutransporte.controller;

import com.example.edutransporte.DTOs.FaculdadeResponse;
import com.example.edutransporte.repository.FaculdadeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/faculdades")
public class FaculdadeController {

    private final FaculdadeRepository faculdadeRepository;

    public FaculdadeController(FaculdadeRepository faculdadeRepository) {
        this.faculdadeRepository = faculdadeRepository;
    }

    /** Lista as faculdades/instituições cadastradas para o cadastro do aluno. */
    @GetMapping
    public List<FaculdadeResponse> listar() {
        return faculdadeRepository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(f -> new FaculdadeResponse(f.getId(), f.getNome()))
                .toList();
    }
}
