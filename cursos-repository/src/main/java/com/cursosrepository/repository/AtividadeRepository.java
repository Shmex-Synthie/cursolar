package com.cursosrepository.repository;

import com.cursosrepository.model.Atividade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AtividadeRepository {

    private final JdbcTemplate jdbc;

    public AtividadeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Atividade> mapper = (rs, row) -> {
        Atividade a = new Atividade();
        a.setId(rs.getLong("id"));
        a.setModuloId(rs.getLong("modulo_id"));
        a.setModuloTitulo(rs.getString("modulo_titulo"));
        a.setCursoId(rs.getLong("curso_id"));
        a.setTitulo(rs.getString("titulo"));
        a.setDescricao(rs.getString("descricao"));
        Date prazo = rs.getDate("prazo");
        if (prazo != null) a.setPrazo(prazo.toLocalDate());
        a.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return a;
    };

    private static final String BASE_QUERY = """
        SELECT at.*, m.titulo AS modulo_titulo, m.curso_id AS curso_id
        FROM atividade at
        JOIN modulo m ON at.modulo_id = m.id
        """;

    public List<Atividade> findByModuloId(Long moduloId) {
        return jdbc.query(BASE_QUERY + "WHERE at.modulo_id = ? ORDER BY at.prazo", mapper, moduloId);
    }

    public Optional<Atividade> findById(Long id) {
        List<Atividade> list = jdbc.query(BASE_QUERY + "WHERE at.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Atividade save(Atividade atividade) {
        if (atividade.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO atividade (modulo_id, titulo, descricao, prazo) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, atividade.getModuloId());
                ps.setString(2, atividade.getTitulo());
                ps.setString(3, atividade.getDescricao());
                ps.setObject(4, atividade.getPrazo() != null ? Date.valueOf(atividade.getPrazo()) : null);
                return ps;
            }, kh);
            atividade.setId(kh.getKey().longValue());
        } else {
            jdbc.update("UPDATE atividade SET modulo_id=?, titulo=?, descricao=?, prazo=? WHERE id=?",
                atividade.getModuloId(), atividade.getTitulo(), atividade.getDescricao(),
                atividade.getPrazo() != null ? Date.valueOf(atividade.getPrazo()) : null,
                atividade.getId());
        }
        return atividade;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM atividade WHERE id = ?", id);
    }
}
