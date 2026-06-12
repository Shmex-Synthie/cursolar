package com.cursosrepository.model;

import java.io.Serializable;

/** Dados do usuário logado guardados na HttpSession. */
public class UsuarioSessao implements Serializable {

    public static final String INSTRUTOR = "INSTRUTOR";
    public static final String ALUNO = "ALUNO";

    private final Long id;
    private final String nome;
    private final String tipo;

    public UsuarioSessao(Long id, String nome, String tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }

    public boolean isInstrutor() { return INSTRUTOR.equals(tipo); }
    public boolean isAluno() { return ALUNO.equals(tipo); }
}
