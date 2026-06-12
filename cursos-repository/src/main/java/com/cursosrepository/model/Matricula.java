package com.cursosrepository.model;

import java.time.LocalDateTime;

public class Matricula {

    private Long id;
    private Long alunoId;
    private String alunoNome;
    private Long cursoId;
    private String cursoTitulo;
    private LocalDateTime matriculadoEm;

    public Matricula() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getCursoTitulo() { return cursoTitulo; }
    public void setCursoTitulo(String cursoTitulo) { this.cursoTitulo = cursoTitulo; }

    public LocalDateTime getMatriculadoEm() { return matriculadoEm; }
    public void setMatriculadoEm(LocalDateTime matriculadoEm) { this.matriculadoEm = matriculadoEm; }
}
