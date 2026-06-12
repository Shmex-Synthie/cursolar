package com.cursosrepository.controller;

import com.cursosrepository.model.Curso;
import com.cursosrepository.model.ForumResposta;
import com.cursosrepository.model.ForumTopico;
import com.cursosrepository.model.UsuarioSessao;
import com.cursosrepository.repository.CursoRepository;
import com.cursosrepository.repository.ForumRepository;
import com.cursosrepository.repository.MatriculaRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forum")
public class ForumController {

    private final ForumRepository forumRepo;
    private final CursoRepository cursoRepo;
    private final MatriculaRepository matriculaRepo;

    public ForumController(ForumRepository forumRepo, CursoRepository cursoRepo,
                           MatriculaRepository matriculaRepo) {
        this.forumRepo = forumRepo;
        this.cursoRepo = cursoRepo;
        this.matriculaRepo = matriculaRepo;
    }

    private UsuarioSessao usuario(HttpSession session) {
        return (UsuarioSessao) session.getAttribute("usuario");
    }

    private String painel(UsuarioSessao usuario) {
        return usuario.isInstrutor() ? "redirect:/instrutor" : "redirect:/aluno";
    }

    /** Instrutor acessa o fórum dos próprios cursos; aluno, dos cursos em que está matriculado. */
    private boolean podeAcessar(Long cursoId, UsuarioSessao usuario) {
        if (usuario.isInstrutor()) {
            Curso curso = cursoRepo.findById(cursoId).orElse(null);
            return curso != null && usuario.getId().equals(curso.getInstrutorId());
        }
        return matriculaRepo.existeMatricula(usuario.getId(), cursoId);
    }

    @GetMapping("/curso/{cursoId}")
    public String listarTopicos(@PathVariable Long cursoId, HttpSession session,
                                Model model, RedirectAttributes redirect) {
        UsuarioSessao usuario = usuario(session);
        if (!podeAcessar(cursoId, usuario)) {
            redirect.addFlashAttribute("erro", "Você não tem acesso ao fórum deste curso.");
            return painel(usuario);
        }
        model.addAttribute("curso", cursoRepo.findById(cursoId)
            .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado: " + cursoId)));
        model.addAttribute("topicos", forumRepo.findByCursoId(cursoId));
        model.addAttribute("novoTopico", new ForumTopico());
        return "forum/list";
    }

    @PostMapping("/curso/{cursoId}/topico/salvar")
    public String salvarTopico(@PathVariable Long cursoId,
                               @Valid @ModelAttribute("novoTopico") ForumTopico topico,
                               BindingResult result, HttpSession session,
                               RedirectAttributes redirect, Model model) {
        UsuarioSessao usuario = usuario(session);
        if (!podeAcessar(cursoId, usuario)) {
            redirect.addFlashAttribute("erro", "Você não tem acesso ao fórum deste curso.");
            return painel(usuario);
        }
        if (result.hasErrors()) {
            model.addAttribute("curso", cursoRepo.findById(cursoId).orElseThrow());
            model.addAttribute("topicos", forumRepo.findByCursoId(cursoId));
            return "forum/list";
        }
        topico.setCursoId(cursoId);
        topico.setAutorId(usuario.getId());
        topico.setAutorTipo(usuario.getTipo());
        topico.setAutorNome(usuario.getNome());
        forumRepo.saveTopico(topico);
        redirect.addFlashAttribute("sucesso", "Tópico criado!");
        return "redirect:/forum/curso/" + cursoId;
    }

    @GetMapping("/topico/{id}")
    public String verTopico(@PathVariable Long id, HttpSession session,
                            Model model, RedirectAttributes redirect) {
        UsuarioSessao usuario = usuario(session);
        ForumTopico topico = forumRepo.findTopicoById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado: " + id));
        if (!podeAcessar(topico.getCursoId(), usuario)) {
            redirect.addFlashAttribute("erro", "Você não tem acesso a este tópico.");
            return painel(usuario);
        }
        topico.setRespostas(forumRepo.findRespostasByTopicoId(id));
        model.addAttribute("topico", topico);
        model.addAttribute("novaResposta", new ForumResposta());
        return "forum/topico";
    }

    @PostMapping("/topico/{topicoId}/resposta/salvar")
    public String salvarResposta(@PathVariable Long topicoId,
                                 @Valid @ModelAttribute("novaResposta") ForumResposta resposta,
                                 BindingResult result, HttpSession session,
                                 RedirectAttributes redirect, Model model) {
        UsuarioSessao usuario = usuario(session);
        ForumTopico topico = forumRepo.findTopicoById(topicoId)
            .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado: " + topicoId));
        if (!podeAcessar(topico.getCursoId(), usuario)) {
            redirect.addFlashAttribute("erro", "Você não tem acesso a este tópico.");
            return painel(usuario);
        }
        if (result.hasErrors()) {
            topico.setRespostas(forumRepo.findRespostasByTopicoId(topicoId));
            model.addAttribute("topico", topico);
            return "forum/topico";
        }
        resposta.setTopicoId(topicoId);
        resposta.setAutorId(usuario.getId());
        resposta.setAutorTipo(usuario.getTipo());
        resposta.setAutorNome(usuario.getNome());
        forumRepo.saveResposta(resposta);
        redirect.addFlashAttribute("sucesso", "Resposta publicada!");
        return "redirect:/forum/topico/" + topicoId;
    }

    @PostMapping("/topico/{id}/excluir")
    public String excluirTopico(@PathVariable Long id, HttpSession session,
                                RedirectAttributes redirect) {
        UsuarioSessao usuario = usuario(session);
        ForumTopico topico = forumRepo.findTopicoById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado: " + id));

        boolean autor = usuario.getId().equals(topico.getAutorId())
            && usuario.getTipo().equals(topico.getAutorTipo());
        boolean instrutorDono = usuario.isInstrutor() && podeAcessar(topico.getCursoId(), usuario);

        if (autor || instrutorDono) {
            forumRepo.deleteTopicoById(id);
            redirect.addFlashAttribute("sucesso", "Tópico excluído.");
        } else {
            redirect.addFlashAttribute("erro", "Apenas o autor ou o instrutor do curso pode excluir o tópico.");
        }
        return "redirect:/forum/curso/" + topico.getCursoId();
    }
}
