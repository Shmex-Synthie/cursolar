package com.cursosrepository.repository;

import com.cursosrepository.model.Submissao;
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
public class SubmissaoRepository {

    private final JdbcTemplate jdbc;

    public SubmissaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Submissao> mapper = (rs, row) -> {
        Submissao s = new Submissao();
        s.setId(rs.getLong("id"));
        s.setAtividadeId(rs.getLong("atividade_id"));
        s.setAlunoId(rs.getLong("aluno_id"));
        s.setAlunoNome(rs.getString("aluno_nome"));
        long arquivoId = rs.getLong("arquivo_id");
        s.setArquivoId(rs.wasNull() ? null : arquivoId);
        s.setArquivoNome(rs.getString("arquivo_nome"));
        long tamanho = rs.getLong("arquivo_tamanho");
        s.setArquivoTamanho(rs.wasNull() ? null : tamanho);
        s.setComentario(rs.getString("comentario"));
        s.setEnviadoEm(rs.getTimestamp("enviado_em").toLocalDateTime());
        return s;
    };

    private static final String BASE_QUERY = """
        SELECT s.*, a.nome AS aluno_nome, ar.nome AS arquivo_nome, ar.tamanho AS arquivo_tamanho
        FROM submissao s
        JOIN aluno a ON s.aluno_id = a.id
        LEFT JOIN arquivo ar ON s.arquivo_id = ar.id
        """;

    public List<Submissao> findByAtividadeId(Long atividadeId) {
        return jdbc.query(BASE_QUERY + "WHERE s.atividade_id = ? ORDER BY a.nome", mapper, atividadeId);
    }

    public List<Submissao> findByAlunoId(Long alunoId) {
        return jdbc.query(BASE_QUERY + "WHERE s.aluno_id = ?", mapper, alunoId);
    }

    public Optional<Submissao> findById(Long id) {
        List<Submissao> list = jdbc.query(BASE_QUERY + "WHERE s.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public Optional<Submissao> findByAtividadeIdAndAlunoId(Long atividadeId, Long alunoId) {
        List<Submissao> list = jdbc.query(
            BASE_QUERY + "WHERE s.atividade_id = ? AND s.aluno_id = ?", mapper, atividadeId, alunoId);
        return list.stream().findFirst();
    }

    public int countByAtividadeId(Long atividadeId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM submissao WHERE atividade_id = ?", Integer.class, atividadeId);
        return count != null ? count : 0;
    }

    public Submissao save(Submissao submissao) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO submissao (atividade_id, aluno_id, arquivo_id, comentario) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, submissao.getAtividadeId());
            ps.setLong(2, submissao.getAlunoId());
            ps.setObject(3, submissao.getArquivoId());
            ps.setString(4, submissao.getComentario());
            return ps;
        }, kh);
        submissao.setId(kh.getKey().longValue());
        return submissao;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM submissao WHERE id = ?", id);
    }
}
