package com.cursosrepository.repository;

import com.cursosrepository.model.Modulo;
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
public class ModuloRepository {

    private final JdbcTemplate jdbc;

    public ModuloRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Modulo> mapper = (rs, row) -> {
        Modulo m = new Modulo();
        m.setId(rs.getLong("id"));
        m.setCursoId(rs.getLong("curso_id"));
        m.setCursoTitulo(rs.getString("curso_titulo"));
        long instrutorId = rs.getLong("curso_instrutor_id");
        m.setCursoInstrutorId(rs.wasNull() ? null : instrutorId);
        m.setTitulo(rs.getString("titulo"));
        m.setDescricao(rs.getString("descricao"));
        m.setOrdem(rs.getInt("ordem"));
        return m;
    };

    private static final String BASE_QUERY = """
        SELECT m.*, c.titulo AS curso_titulo, c.instrutor_id AS curso_instrutor_id
        FROM modulo m
        JOIN curso c ON m.curso_id = c.id
        """;

    public List<Modulo> findAll() {
        return jdbc.query(BASE_QUERY + "ORDER BY c.titulo, m.ordem", mapper);
    }

    public List<Modulo> findByCursoId(Long cursoId) {
        return jdbc.query(BASE_QUERY + "WHERE m.curso_id = ? ORDER BY m.ordem", mapper, cursoId);
    }

    public Optional<Modulo> findById(Long id) {
        List<Modulo> list = jdbc.query(BASE_QUERY + "WHERE m.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Modulo save(Modulo modulo) {
        if (modulo.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO modulo (curso_id, titulo, descricao, ordem) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, modulo.getCursoId());
                ps.setString(2, modulo.getTitulo());
                ps.setString(3, modulo.getDescricao());
                ps.setInt(4, modulo.getOrdem());
                return ps;
            }, kh);
            modulo.setId(kh.getKey().longValue());
        } else {
            jdbc.update("UPDATE modulo SET curso_id=?, titulo=?, descricao=?, ordem=? WHERE id=?",
                modulo.getCursoId(), modulo.getTitulo(), modulo.getDescricao(),
                modulo.getOrdem(), modulo.getId());
        }
        return modulo;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM modulo WHERE id = ?", id);
    }
}
