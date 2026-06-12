package com.cursosrepository.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Usuários criados antes da introdução do login não possuem senha.
 * Este inicializador define a senha padrão "123456" para eles.
 */
@Component
public class SenhaPadraoInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SenhaPadraoInitializer.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public SenhaPadraoInitializer(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        String hash = encoder.encode("123456");
        int instrutores = jdbc.update(
            "UPDATE instrutor SET senha = ? WHERE senha IS NULL OR senha = ''", hash);
        int alunos = jdbc.update(
            "UPDATE aluno SET senha = ? WHERE senha IS NULL OR senha = ''", hash);
        if (instrutores + alunos > 0) {
            log.info("{} instrutor(es) e {} aluno(s) existentes receberam a senha padrão '123456'.",
                instrutores, alunos);
        }
    }
}
