package com.cursosrepository.controller;

import com.cursosrepository.model.Curso;
import com.cursosrepository.model.Matricula;
import com.cursosrepository.model.UsuarioSessao;
import com.cursosrepository.repository.AlunoRepository;
import com.cursosrepository.repository.CursoRepository;
import com.cursosrepository.repository.MatriculaRepository;
import com.cursosrepository.repository.ModuloRepository;
import com.cursosrepository.service.ArquivoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/instrutor")
public class InstrutorPortalController {

    private final CursoRepository cursoRepo;
    private final ModuloRepository moduloRepo;
    private final MatriculaRepository matriculaRepo;
    private final ArquivoService arquivoService;
    private final AlunoRepository alunoRepo;

    public InstrutorPortalController(CursoRepository cursoRepo, ModuloRepository moduloRepo,
                                     MatriculaRepository matriculaRepo, ArquivoService arquivoService,
                                     AlunoRepository alunoRepo) {
        this.cursoRepo = cursoRepo;
        this.moduloRepo = moduloRepo;
        this.matriculaRepo = matriculaRepo;
        this.arquivoService = arquivoService;
        this.alunoRepo = alunoRepo;
    }

    private UsuarioSessao usuario(HttpSession session) {
        return (UsuarioSessao) session.getAttribute("usuario");
    }

    private Curso cursoDoInstrutor(Long cursoId, UsuarioSessao usuario) {
        Curso curso = cursoRepo.findById(cursoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado"));
        if (!usuario.getId().equals(curso.getInstrutorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este curso pertence a outro instrutor");
        }
        return curso;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("cursos", cursoRepo.findByInstrutorId(usuario(session).getId()));
        return "instrutor/dashboard";
    }

    @GetMapping("/cursos/novo")
    public String novoCurso(Model model) {
        model.addAttribute("curso", new Curso());
        return "instrutor/curso-form";
    }

    @GetMapping("/cursos/{id}")
    public String verCurso(@PathVariable Long id, HttpSession session, Model model) {
        Curso curso = cursoDoInstrutor(id, usuario(session));
        var matriculados = matriculaRepo.findByCursoId(id);
        Set<Long> idsMatriculados = matriculados.stream()
            .map(Matricula::getAlunoId)
            .collect(Collectors.toSet());
        var alunosDisponiveis = alunoRepo.findAll().stream()
            .filter(a -> !idsMatriculados.contains(a.getId()))
            .toList();

        model.addAttribute("curso", curso);
        model.addAttribute("modulos", moduloRepo.findByCursoId(id));
        model.addAttribute("matriculados", matriculados);
        model.addAttribute("alunosDisponiveis", alunosDisponiveis);
        model.addAttribute("arquivos", arquivoService.listarPorCurso(id));
        return "instrutor/curso-view";
    }

    @PostMapping("/cursos/{id}/matricular")
    public String matricularAluno(@PathVariable Long id, @RequestParam Long alunoId,
                                  HttpSession session, RedirectAttributes redirect) {
        cursoDoInstrutor(id, usuario(session));
        if (matriculaRepo.existeMatricula(alunoId, id)) {
            redirect.addFlashAttribute("erro", "Este aluno já está matriculado no curso.");
        } else {
            Matricula matricula = new Matricula();
            matricula.setAlunoId(alunoId);
            matricula.setCursoId(id);
            matriculaRepo.save(matricula);
            redirect.addFlashAttribute("sucesso", "Aluno matriculado com sucesso!");
        }
        return "redirect:/instrutor/cursos/" + id;
    }

    @PostMapping("/cursos/{cursoId}/matriculas/{matriculaId}/cancelar")
    public String cancelarMatricula(@PathVariable Long cursoId, @PathVariable Long matriculaId,
                                    HttpSession session, RedirectAttributes redirect) {
        cursoDoInstrutor(cursoId, usuario(session));
        matriculaRepo.findById(matriculaId).ifPresent(m -> {
            if (cursoId.equals(m.getCursoId())) {
                matriculaRepo.deleteById(matriculaId);
                redirect.addFlashAttribute("sucesso", "Matrícula cancelada.");
            }
        });
        return "redirect:/instrutor/cursos/" + cursoId;
    }

    @PostMapping("/cursos/{id}/upload")
    public String uploadMaterial(@PathVariable Long id, @RequestParam("arquivo") MultipartFile file,
                                 HttpSession session, RedirectAttributes redirect) throws IOException {
        cursoDoInstrutor(id, usuario(session));
        if (!file.isEmpty()) {
            arquivoService.salvarParaCurso(id, file);
            redirect.addFlashAttribute("sucesso", "Material enviado com sucesso!");
        }
        return "redirect:/instrutor/cursos/" + id;
    }

    @PostMapping("/cursos/{cursoId}/arquivos/{arquivoId}/excluir")
    public String excluirMaterial(@PathVariable Long cursoId, @PathVariable Long arquivoId,
                                  HttpSession session, RedirectAttributes redirect) throws IOException {
        cursoDoInstrutor(cursoId, usuario(session));
        arquivoService.excluir(arquivoId);
        redirect.addFlashAttribute("sucesso", "Material excluído.");
        return "redirect:/instrutor/cursos/" + cursoId;
    }

    @GetMapping("/cursos/{id}/editar")
    public String editarCurso(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("curso", cursoDoInstrutor(id, usuario(session)));
        return "instrutor/curso-form";
    }

    @PostMapping("/cursos/salvar")
    public String salvarCurso(@Valid @ModelAttribute Curso curso, BindingResult result,
                              HttpSession session, RedirectAttributes redirect) {
        if (result.hasErrors()) return "instrutor/curso-form";
        UsuarioSessao usuario = usuario(session);
        if (curso.getId() != null) {
            cursoDoInstrutor(curso.getId(), usuario);
        }
        curso.setInstrutorId(usuario.getId());
        cursoRepo.save(curso);
        redirect.addFlashAttribute("sucesso", "Curso salvo com sucesso!");
        return "redirect:/instrutor";
    }

    @PostMapping("/cursos/{id}/excluir")
    public String excluirCurso(@PathVariable Long id, HttpSession session,
                               RedirectAttributes redirect) {
        cursoDoInstrutor(id, usuario(session));
        cursoRepo.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Curso excluído.");
        return "redirect:/instrutor";
    }
}
