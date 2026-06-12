package com.cursosrepository.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Atividade {

    private Long id;

    @NotNull(message = "Módulo é obrigatório")
    private Long moduloId;

    private String moduloTitulo;
    private Long cursoId;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    private String descricao;
    private LocalDate prazo;
    private LocalDateTime criadoEm;

    public Atividade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModuloId() { return moduloId; }
    public void setModuloId(Long moduloId) { this.moduloId = moduloId; }

    public String getModuloTitulo() { return moduloTitulo; }
    public void setModuloTitulo(String moduloTitulo) { this.moduloTitulo = moduloTitulo; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
