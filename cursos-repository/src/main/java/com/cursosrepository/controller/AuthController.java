package com.cursosrepository.controller;

import com.cursosrepository.model.Aluno;
import com.cursosrepository.model.Instrutor;
import com.cursosrepository.model.UsuarioSessao;
import com.cursosrepository.repository.AlunoRepository;
import com.cursosrepository.repository.InstrutorRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final InstrutorRepository instrutorRepo;
    private final AlunoRepository alunoRepo;
    private final PasswordEncoder encoder;

    public AuthController(InstrutorRepository instrutorRepo, AlunoRepository alunoRepo,
                          PasswordEncoder encoder) {
        this.instrutorRepo = instrutorRepo;
        this.alunoRepo = alunoRepo;
        this.encoder = encoder;
    }

    /** Retorna o redirect para o painel do usuário logado, ou null se não há sessão. */
    private String painelSeLogado(HttpSession session) {
        UsuarioSessao usuario = (UsuarioSessao) session.getAttribute("usuario");
        if (usuario == null) return null;
        return usuario.isInstrutor() ? "redirect:/instrutor" : "redirect:/aluno";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        String painel = painelSeLogado(session);
        return painel != null ? painel : "index";
    }

    // ---------- Instrutor ----------

    @GetMapping("/instrutor/login")
    public String loginInstrutorForm(Model model) {
        model.addAttribute("tipo", UsuarioSessao.INSTRUTOR);
        return "auth/login";
    }

    @PostMapping("/instrutor/login")
    public String loginInstrutor(@RequestParam String email, @RequestParam String senha,
                                 HttpSession session, RedirectAttributes redirect) {
        Optional<Instrutor> instrutor = instrutorRepo.findByEmail(email);
        if (instrutor.isPresent() && instrutor.get().getSenha() != null
                && encoder.matches(senha, instrutor.get().getSenha())) {
            session.setAttribute("usuario", new UsuarioSessao(
                instrutor.get().getId(), instrutor.get().getNome(), UsuarioSessao.INSTRUTOR));
            return "redirect:/instrutor";
        }
        redirect.addFlashAttribute("erro", "E-mail ou senha inválidos.");
        return "redirect:/instrutor/login";
    }

    @GetMapping("/instrutor/cadastro")
    public String cadastroInstrutorForm(Model model) {
        model.addAttribute("instrutor", new Instrutor());
        return "auth/cadastro-instrutor";
    }

    @PostMapping("/instrutor/cadastro")
    public String cadastroInstrutor(@Valid @ModelAttribute Instrutor instrutor,
                                    BindingResult result, HttpSession session) {
        if (result.hasErrors()) return "auth/cadastro-instrutor";
        try {
            instrutor.setSenha(encoder.encode(instrutor.getSenha()));
            instrutorRepo.save(instrutor);
        } catch (DuplicateKeyException e) {
            result.rejectValue("email", "duplicado", "Este e-mail já está cadastrado.");
            return "auth/cadastro-instrutor";
        }
        session.setAttribute("usuario", new UsuarioSessao(
            instrutor.getId(), instrutor.getNome(), UsuarioSessao.INSTRUTOR));
        return "redirect:/instrutor";
    }

    // ---------- Aluno ----------

    @GetMapping("/aluno/login")
    public String loginAlunoForm(Model model) {
        model.addAttribute("tipo", UsuarioSessao.ALUNO);
        return "auth/login";
    }

    @PostMapping("/aluno/login")
    public String loginAluno(@RequestParam String email, @RequestParam String senha,
                             HttpSession session, RedirectAttributes redirect) {
        Optional<Aluno> aluno = alunoRepo.findByEmail(email);
        if (aluno.isPresent() && aluno.get().getSenha() != null
                && encoder.matches(senha, aluno.get().getSenha())) {
            session.setAttribute("usuario", new UsuarioSessao(
                aluno.get().getId(), aluno.get().getNome(), UsuarioSessao.ALUNO));
            return "redirect:/aluno";
        }
        redirect.addFlashAttribute("erro", "E-mail ou senha inválidos.");
        return "redirect:/aluno/login";
    }

    @GetMapping("/aluno/cadastro")
    public String cadastroAlunoForm(Model model) {
        model.addAttribute("aluno", new Aluno());
        return "auth/cadastro-aluno";
    }

    @PostMapping("/aluno/cadastro")
    public String cadastroAluno(@Valid @ModelAttribute Aluno aluno,
                                BindingResult result, HttpSession session) {
        if (result.hasErrors()) return "auth/cadastro-aluno";
        try {
            aluno.setSenha(encoder.encode(aluno.getSenha()));
            alunoRepo.save(aluno);
        } catch (DuplicateKeyException e) {
            result.rejectValue("email", "duplicado", "Este e-mail já está cadastrado.");
            return "auth/cadastro-aluno";
        }
        session.setAttribute("usuario", new UsuarioSessao(
            aluno.getId(), aluno.getNome(), UsuarioSessao.ALUNO));
        return "redirect:/aluno";
    }

    // ---------- Logout ----------

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
