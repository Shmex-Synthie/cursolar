package com.cursosrepository.repository;

import com.cursosrepository.model.Aluno;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AlunoRepository {

    private final JdbcTemplate jdbc;

    public AlunoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Aluno> mapper = (rs, row) -> {
        Aluno a = new Aluno();
        a.setId(rs.getLong("id"));
        a.setNome(rs.getString("nome"));
        a.setEmail(rs.getString("email"));
        a.setSenha(rs.getString("senha"));
        a.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return a;
    };

    public List<Aluno> findAll() {
        return jdbc.query("SELECT * FROM aluno ORDER BY nome", mapper);
    }

    public Optional<Aluno> findById(Long id) {
        List<Aluno> list = jdbc.query("SELECT * FROM aluno WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Optional<Aluno> findByEmail(String email) {
        List<Aluno> list = jdbc.query("SELECT * FROM aluno WHERE email = ?", mapper, email);
        return list.stream().findFirst();
    }

    public Aluno save(Aluno aluno) {
        if (aluno.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO aluno (nome, email, senha) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, aluno.getNome());
                ps.setString(2, aluno.getEmail());
                ps.setString(3, aluno.getSenha());
                return ps;
            }, kh);
            aluno.setId(kh.getKey().longValue());
        } else {
            jdbc.update("UPDATE aluno SET nome=?, email=? WHERE id=?",
                aluno.getNome(), aluno.getEmail(), aluno.getId());
        }
        return aluno;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM aluno WHERE id = ?", id);
    }
}
