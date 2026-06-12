package com.cursosrepository.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Modulo {

    private Long id;

    @NotNull(message = "Curso é obrigatório")
    private Long cursoId;

    private String cursoTitulo;
    private Long cursoInstrutorId;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    private String descricao;
    private int ordem;

    public Modulo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getCursoTitulo() { return cursoTitulo; }
    public void setCursoTitulo(String cursoTitulo) { this.cursoTitulo = cursoTitulo; }

    public Long getCursoInstrutorId() { return cursoInstrutorId; }
    public void setCursoInstrutorId(Long cursoInstrutorId) { this.cursoInstrutorId = cursoInstrutorId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
