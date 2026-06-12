package com.cursosrepository.repository;

import com.cursosrepository.model.Instrutor;
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
public class InstrutorRepository {

    private final JdbcTemplate jdbc;

    public InstrutorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Instrutor> mapper = (rs, row) -> {
        Instrutor i = new Instrutor();
        i.setId(rs.getLong("id"));
        i.setNome(rs.getString("nome"));
        i.setEmail(rs.getString("email"));
        i.setSenha(rs.getString("senha"));
        i.setBio(rs.getString("bio"));
        i.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return i;
    };

    public List<Instrutor> findAll() {
        return jdbc.query("SELECT * FROM instrutor ORDER BY nome", mapper);
    }

    public Optional<Instrutor> findById(Long id) {
        List<Instrutor> list = jdbc.query("SELECT * FROM instrutor WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Optional<Instrutor> findByEmail(String email) {
        List<Instrutor> list = jdbc.query("SELECT * FROM instrutor WHERE email = ?", mapper, email);
        return list.stream().findFirst();
    }

    public Instrutor save(Instrutor instrutor) {
        if (instrutor.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO instrutor (nome, email, senha, bio) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, instrutor.getNome());
                ps.setString(2, instrutor.getEmail());
                ps.setString(3, instrutor.getSenha());
                ps.setString(4, instrutor.getBio());
                return ps;
            }, kh);
            instrutor.setId(kh.getKey().longValue());
        } else {
            jdbc.update("UPDATE instrutor SET nome=?, email=?, bio=? WHERE id=?",
                instrutor.getNome(), instrutor.getEmail(), instrutor.getBio(), instrutor.getId());
        }
        return instrutor;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM instrutor WHERE id = ?", id);
    }
}
