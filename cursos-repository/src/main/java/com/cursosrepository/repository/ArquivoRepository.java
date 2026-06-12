package com.cursosrepository.repository;

import com.cursosrepository.model.Arquivo;
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
public class ArquivoRepository {

    private final JdbcTemplate jdbc;

    public ArquivoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Arquivo> mapper = (rs, row) -> {
        Arquivo a = new Arquivo();
        a.setId(rs.getLong("id"));
        long moduloId = rs.getLong("modulo_id");
        a.setModuloId(rs.wasNull() ? null : moduloId);
        a.setModuloTitulo(rs.getString("modulo_titulo"));
        long cursoId = rs.getLong("curso_id");
        a.setCursoId(rs.wasNull() ? null : cursoId);
        a.setNome(rs.getString("nome"));
        a.setCaminho(rs.getString("caminho"));
        a.setTipo(Arquivo.TipoArquivo.valueOf(rs.getString("tipo")));
        a.setTamanho(rs.getLong("tamanho"));
        a.setEnviadoEm(rs.getTimestamp("enviado_em").toLocalDateTime());
        return a;
    };

    private static final String BASE_QUERY = """
        SELECT a.*, m.titulo AS modulo_titulo
        FROM arquivo a
        LEFT JOIN modulo m ON a.modulo_id = m.id
        """;

    public List<Arquivo> findByModuloId(Long moduloId) {
        return jdbc.query(BASE_QUERY + "WHERE a.modulo_id = ? ORDER BY a.enviado_em", mapper, moduloId);
    }

    public List<Arquivo> findByCursoId(Long cursoId) {
        return jdbc.query(BASE_QUERY + "WHERE a.curso_id = ? ORDER BY a.enviado_em", mapper, cursoId);
    }

    public Optional<Arquivo> findById(Long id) {
        List<Arquivo> list = jdbc.query(BASE_QUERY + "WHERE a.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Arquivo save(Arquivo arquivo) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO arquivo (modulo_id, curso_id, nome, caminho, tipo, tamanho) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, arquivo.getModuloId());
            ps.setObject(2, arquivo.getCursoId());
            ps.setString(3, arquivo.getNome());
            ps.setString(4, arquivo.getCaminho());
            ps.setString(5, arquivo.getTipo().name());
            ps.setObject(6, arquivo.getTamanho());
            return ps;
        }, kh);
        arquivo.setId(kh.getKey().longValue());
        return arquivo;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM arquivo WHERE id = ?", id);
    }
}
