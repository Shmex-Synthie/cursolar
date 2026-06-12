package com.cursosrepository.repository;

import com.cursosrepository.model.Curso;
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
public class CursoRepository {

    private final JdbcTemplate jdbc;

    public CursoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Curso> mapper = (rs, row) -> {
        Curso c = new Curso();
        c.setId(rs.getLong("id"));
        c.setTitulo(rs.getString("titulo"));
        c.setDescricao(rs.getString("descricao"));
        c.setCategoria(rs.getString("categoria"));
        c.setCargaHoraria(rs.getObject("carga_horaria", Integer.class));
        long instId = rs.getLong("instrutor_id");
        c.setInstrutorId(rs.wasNull() ? null : instId);
        c.setInstrutorNome(rs.getString("instrutor_nome"));
        c.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return c;
    };

    private static final String BASE_QUERY = """
        SELECT c.*, i.nome AS instrutor_nome
        FROM curso c
        LEFT JOIN instrutor i ON c.instrutor_id = i.id
        """;

    public List<Curso> findAll() {
        return jdbc.query(BASE_QUERY + "ORDER BY c.titulo", mapper);
    }

    public List<Curso> findByInstrutorId(Long instrutorId) {
        return jdbc.query(BASE_QUERY + "WHERE c.instrutor_id = ? ORDER BY c.titulo", mapper, instrutorId);
    }

    public Optional<Curso> findById(Long id) {
        List<Curso> list = jdbc.query(BASE_QUERY + "WHERE c.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Curso save(Curso curso) {
        if (curso.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO curso (titulo, descricao, categoria, carga_horaria, instrutor_id) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, curso.getTitulo());
                ps.setString(2, curso.getDescricao());
                ps.setString(3, curso.getCategoria());
                ps.setObject(4, curso.getCargaHoraria());
                ps.setObject(5, curso.getInstrutorId());
                return ps;
            }, kh);
            curso.setId(kh.getKey().longValue());
        } else {
            jdbc.update("UPDATE curso SET titulo=?, descricao=?, categoria=?, carga_horaria=?, instrutor_id=? WHERE id=?",
                curso.getTitulo(), curso.getDescricao(), curso.getCategoria(),
                curso.getCargaHoraria(), curso.getInstrutorId(), curso.getId());
        }
        return curso;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM curso WHERE id = ?", id);
    }
}
