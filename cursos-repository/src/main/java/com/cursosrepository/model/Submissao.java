package com.cursosrepository.model;

import java.time.LocalDateTime;

public class Submissao {

    private Long id;
    private Long atividadeId;
    private Long alunoId;
    private String alunoNome;
    private Long arquivoId;
    private String arquivoNome;
    private Long arquivoTamanho;
    private String comentario;
    private LocalDateTime enviadoEm;

    public Submissao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAtividadeId() { return atividadeId; }
    public void setAtividadeId(Long atividadeId) { this.atividadeId = atividadeId; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public Long getArquivoId() { return arquivoId; }
    public void setArquivoId(Long arquivoId) { this.arquivoId = arquivoId; }

    public String getArquivoNome() { return arquivoNome; }
    public void setArquivoNome(String arquivoNome) { this.arquivoNome = arquivoNome; }

    public Long getArquivoTamanho() { return arquivoTamanho; }
    public void setArquivoTamanho(Long arquivoTamanho) { this.arquivoTamanho = arquivoTamanho; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }
}
