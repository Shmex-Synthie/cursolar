package com.cursosrepository.repository;

import com.cursosrepository.model.Matricula;
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
public class MatriculaRepository {

    private final JdbcTemplate jdbc;

    public MatriculaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Matricula> mapper = (rs, row) -> {
        Matricula m = new Matricula();
        m.setId(rs.getLong("id"));
        m.setAlunoId(rs.getLong("aluno_id"));
        m.setAlunoNome(rs.getString("aluno_nome"));
        m.setCursoId(rs.getLong("curso_id"));
        m.setCursoTitulo(rs.getString("curso_titulo"));
        m.setMatriculadoEm(rs.getTimestamp("matriculado_em").toLocalDateTime());
        return m;
    };

    private static final String BASE_QUERY = """
        SELECT mt.*, a.nome AS aluno_nome, c.titulo AS curso_titulo
        FROM matricula mt
        JOIN aluno a ON mt.aluno_id = a.id
        JOIN curso c ON mt.curso_id = c.id
        """;

    public List<Matricula> findAll() {
        return jdbc.query(BASE_QUERY + "ORDER BY mt.matriculado_em DESC", mapper);
    }

    public List<Matricula> findByAlunoId(Long alunoId) {
        return jdbc.query(BASE_QUERY + "WHERE mt.aluno_id = ? ORDER BY c.titulo", mapper, alunoId);
    }

    public List<Matricula> findByCursoId(Long cursoId) {
        return jdbc.query(BASE_QUERY + "WHERE mt.curso_id = ? ORDER BY a.nome", mapper, cursoId);
    }

    public Optional<Matricula> findById(Long id) {
        List<Matricula> list = jdbc.query(BASE_QUERY + "WHERE mt.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public boolean existeMatricula(Long alunoId, Long cursoId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM matricula WHERE aluno_id = ? AND curso_id = ?",
            Integer.class, alunoId, cursoId);
        return count != null && count > 0;
    }

    public Matricula save(Matricula matricula) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO matricula (aluno_id, curso_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, matricula.getAlunoId());
            ps.setLong(2, matricula.getCursoId());
            return ps;
        }, kh);
        matricula.setId(kh.getKey().longValue());
        return matricula;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM matricula WHERE id = ?", id);
    }
}
