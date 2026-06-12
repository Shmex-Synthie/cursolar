package com.cursosrepository.repository;

import com.cursosrepository.model.ForumResposta;
import com.cursosrepository.model.ForumTopico;
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
public class ForumRepository {

    private final JdbcTemplate jdbc;

    public ForumRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<ForumTopico> topicoMapper = (rs, row) -> {
        ForumTopico t = new ForumTopico();
        t.setId(rs.getLong("id"));
        t.setCursoId(rs.getLong("curso_id"));
        t.setCursoTitulo(rs.getString("curso_titulo"));
        long autorId = rs.getLong("autor_id");
        t.setAutorId(rs.wasNull() ? null : autorId);
        t.setAutorTipo(rs.getString("autor_tipo"));
        t.setAutorNome(rs.getString("autor_nome"));
        t.setTitulo(rs.getString("titulo"));
        t.setConteudo(rs.getString("conteudo"));
        t.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return t;
    };

    private final RowMapper<ForumResposta> respostaMapper = (rs, row) -> {
        ForumResposta r = new ForumResposta();
        r.setId(rs.getLong("id"));
        r.setTopicoId(rs.getLong("topico_id"));
        long autorId = rs.getLong("autor_id");
        r.setAutorId(rs.wasNull() ? null : autorId);
        r.setAutorTipo(rs.getString("autor_tipo"));
        r.setAutorNome(rs.getString("autor_nome"));
        r.setConteudo(rs.getString("conteudo"));
        r.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return r;
    };

    private static final String TOPICO_QUERY = """
        SELECT ft.*, c.titulo AS curso_titulo
        FROM forum_topico ft
        JOIN curso c ON ft.curso_id = c.id
        """;

    public List<ForumTopico> findByCursoId(Long cursoId) {
        return jdbc.query(TOPICO_QUERY + "WHERE ft.curso_id = ? ORDER BY ft.criado_em DESC", topicoMapper, cursoId);
    }

    public Optional<ForumTopico> findTopicoById(Long id) {
        List<ForumTopico> list = jdbc.query(TOPICO_QUERY + "WHERE ft.id = ?", topicoMapper, id);
        return list.stream().findFirst();
    }

    public ForumTopico saveTopico(ForumTopico topico) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO forum_topico (curso_id, autor_id, autor_tipo, autor_nome, titulo, conteudo) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, topico.getCursoId());
            ps.setObject(2, topico.getAutorId());
            ps.setString(3, topico.getAutorTipo());
            ps.setString(4, topico.getAutorNome());
            ps.setString(5, topico.getTitulo());
            ps.setString(6, topico.getConteudo());
            return ps;
        }, kh);
        topico.setId(kh.getKey().longValue());
        return topico;
    }

    public List<ForumResposta> findRespostasByTopicoId(Long topicoId) {
        return jdbc.query("SELECT * FROM forum_resposta WHERE topico_id = ? ORDER BY criado_em",
            respostaMapper, topicoId);
    }

    public ForumResposta saveResposta(ForumResposta resposta) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO forum_resposta (topico_id, autor_id, autor_tipo, autor_nome, conteudo) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, resposta.getTopicoId());
            ps.setObject(2, resposta.getAutorId());
            ps.setString(3, resposta.getAutorTipo());
            ps.setString(4, resposta.getAutorNome());
            ps.setString(5, resposta.getConteudo());
            return ps;
        }, kh);
        resposta.setId(kh.getKey().longValue());
        return resposta;
    }

    public void deleteTopicoById(Long id) {
        jdbc.update("DELETE FROM forum_topico WHERE id = ?", id);
    }
}
