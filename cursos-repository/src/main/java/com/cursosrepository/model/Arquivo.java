package com.cursosrepository.model;

import java.time.LocalDateTime;

public class Arquivo {

    public enum TipoArquivo { PDF, VIDEO, IMAGEM, OUTRO }

    private Long id;
    private Long moduloId;
    private String moduloTitulo;
    private Long cursoId;
    private String nome;
    private String caminho;
    private TipoArquivo tipo;
    private Long tamanho;
    private LocalDateTime enviadoEm;

    public Arquivo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModuloId() { return moduloId; }
    public void setModuloId(Long moduloId) { this.moduloId = moduloId; }

    public String getModuloTitulo() { return moduloTitulo; }
    public void setModuloTitulo(String moduloTitulo) { this.moduloTitulo = moduloTitulo; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCaminho() { return caminho; }
    public void setCaminho(String caminho) { this.caminho = caminho; }

    public TipoArquivo getTipo() { return tipo; }
    public void setTipo(TipoArquivo tipo) { this.tipo = tipo; }

    public Long getTamanho() { return tamanho; }
    public void setTamanho(Long tamanho) { this.tamanho = tamanho; }

    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }
}
