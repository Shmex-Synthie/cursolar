package com.cursosrepository.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ForumResposta {

    private Long id;
    private Long topicoId;
    private Long autorId;
    private String autorTipo;
    private String autorNome;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String conteudo;

    private LocalDateTime criadoEm;

    public ForumResposta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTopicoId() { return topicoId; }
    public void setTopicoId(Long topicoId) { this.topicoId = topicoId; }

    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }

    public String getAutorTipo() { return autorTipo; }
    public void setAutorTipo(String autorTipo) { this.autorTipo = autorTipo; }

    public String getAutorNome() { return autorNome; }
    public void setAutorNome(String autorNome) { this.autorNome = autorNome; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
