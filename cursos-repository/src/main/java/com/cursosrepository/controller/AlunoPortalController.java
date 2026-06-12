package com.cursosrepository.controller;

import com.cursosrepository.model.Arquivo;
import com.cursosrepository.model.Atividade;
import com.cursosrepository.model.Curso;
import com.cursosrepository.model.Modulo;
import com.cursosrepository.model.Submissao;
import com.cursosrepository.model.UsuarioSessao;
import com.cursosrepository.repository.AtividadeRepository;
import com.cursosrepository.repository.CursoRepository;
import com.cursosrepository.repository.MatriculaRepository;
import com.cursosrepository.repository.ModuloRepository;
import com.cursosrepository.repository.SubmissaoRepository;
import com.cursosrepository.service.ArquivoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/aluno")
public class AlunoPortalController {

    private final CursoRepository cursoRepo;
    private final MatriculaRepository matriculaRepo;
    private final ModuloRepository moduloRepo;
    private final ArquivoService arquivoService;
    private final AtividadeRepository atividadeRepo;
    private final SubmissaoRepository submissaoRepo;

    public AlunoPortalController(CursoRepository cursoRepo, MatriculaRepository matriculaRepo,
                                 ModuloRepository moduloRepo, ArquivoService arquivoService,
                                 AtividadeRepository atividadeRepo, SubmissaoRepository submissaoRepo) {
        this.cursoRepo = cursoRepo;
        this.matriculaRepo = matriculaRepo;
        this.moduloRepo = moduloRepo;
        this.arquivoService = arquivoService;
        this.atividadeRepo = atividadeRepo;
        this.submissaoRepo = submissaoRepo;
    }

    private UsuarioSessao usuario(HttpSession session) {
        return (UsuarioSessao) session.getAttribute("usuario");
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        UsuarioSessao usuario = usuario(session);
        model.addAttribute("matriculas", matriculaRepo.findByAlunoId(usuario.getId()));
        return "aluno/dashboard";
    }

    @GetMapping("/cursos/{id}")
    public String verCurso(@PathVariable Long id, HttpSession session, Model model,
                           RedirectAttributes redirect) {
        UsuarioSessao usuario = usuario(session);
        if (!matriculaRepo.existeMatricula(usuario.getId(), id)) {
            redirect.addFlashAttribute("erro", "Você precisa estar matriculado para acessar este curso.");
            return "redirect:/aluno";
        }
        Curso curso = cursoRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado: " + id));
        List<Modulo> modulos = moduloRepo.findByCursoId(id);

        Map<Long, List<Arquivo>> arquivosPorModulo = new HashMap<>();
        Map<Long, List<Atividade>> atividadesPorModulo = new HashMap<>();
        for (Modulo m : modulos) {
            arquivosPorModulo.put(m.getId(), arquivoService.listarPorModulo(m.getId()));
            atividadesPorModulo.put(m.getId(), atividadeRepo.findByModuloId(m.getId()));
        }

        Map<Long, Submissao> submissoesPorAtividade = new HashMap<>();
        for (Submissao s : submissaoRepo.findByAlunoId(usuario.getId())) {
            submissoesPorAtividade.put(s.getAtividadeId(), s);
        }

        model.addAttribute("curso", curso);
        model.addAttribute("modulos", modulos);
        model.addAttribute("arquivosCurso", arquivoService.listarPorCurso(id));
        model.addAttribute("arquivosPorModulo", arquivosPorModulo);
        model.addAttribute("atividadesPorModulo", atividadesPorModulo);
        model.addAttribute("submissoesPorAtividade", submissoesPorAtividade);
        return "aluno/curso-view";
    }

    @PostMapping("/atividades/{atividadeId}/submeter")
    public String submeterAtividade(@PathVariable Long atividadeId,
                                    @RequestParam("arquivo") MultipartFile file,
                                    @RequestParam(required = false) String comentario,
                                    HttpSession session, RedirectAttributes redirect) throws IOException {
        UsuarioSessao usuario = usuario(session);
        Atividade atividade = atividadeRepo.findById(atividadeId)
            .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada: " + atividadeId));

        if (!matriculaRepo.existeMatricula(usuario.getId(), atividade.getCursoId())) {
            redirect.addFlashAttribute("erro", "Você precisa estar matriculado para enviar esta atividade.");
            return "redirect:/aluno";
        }
        if (file.isEmpty()) {
            redirect.addFlashAttribute("erro", "Selecione um arquivo para enviar.");
            return "redirect:/aluno/cursos/" + atividade.getCursoId();
        }

        // Reenvio substitui a submissão anterior
        submissaoRepo.findByAtividadeIdAndAlunoId(atividadeId, usuario.getId()).ifPresent(anterior -> {
            try {
                if (anterior.getArquivoId() != null) arquivoService.excluir(anterior.getArquivoId());
            } catch (IOException ignored) {}
            submissaoRepo.deleteById(anterior.getId());
        });

        Arquivo arquivo = arquivoService.salvarAvulso(file);
        Submissao submissao = new Submissao();
        submissao.setAtividadeId(atividadeId);
        submissao.setAlunoId(usuario.getId());
        submissao.setArquivoId(arquivo.getId());
        submissao.setComentario(comentario);
        submissaoRepo.save(submissao);

        redirect.addFlashAttribute("sucesso", "Atividade enviada com sucesso!");
        return "redirect:/aluno/cursos/" + atividade.getCursoId();
    }

    @PostMapping("/submissoes/{id}/excluir")
    public String excluirSubmissao(@PathVariable Long id, HttpSession session,
                                   RedirectAttributes redirect) throws IOException {
        UsuarioSessao usuario = usuario(session);
        Submissao submissao = submissaoRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Submissão não encontrada: " + id));
        if (!usuario.getId().equals(submissao.getAlunoId())) {
            redirect.addFlashAttribute("erro", "Você só pode excluir as próprias submissões.");
            return "redirect:/aluno";
        }
        Long cursoId = atividadeRepo.findById(submissao.getAtividadeId())
            .map(Atividade::getCursoId).orElse(null);

        if (submissao.getArquivoId() != null) {
            arquivoService.excluir(submissao.getArquivoId());
        }
        submissaoRepo.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Submissão removida. Você pode enviar novamente.");
        return cursoId != null ? "redirect:/aluno/cursos/" + cursoId : "redirect:/aluno";
    }
}
