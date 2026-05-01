package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Motoboy;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.MotoboyRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private MotoboyRepository motoboyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            return ResponseEntity.ok(new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponseDTO("Email ou senha inválidos", false));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        try {
            if (usuarioRepository.existsByEmail(req.getEmail())) {
                return ResponseEntity.badRequest().body(new MensagemResponseDTO("Email já cadastrado", false));
            }

            // Criar novo usuário básico
            Usuario user = new Usuario();
            user.setNome(req.getNome());
            user.setEmail(req.getEmail());
            user.setSenha(passwordEncoder.encode(req.getSenha()));
            user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "CUSTOMER");
            user.setCpf(req.getCpf());
            user.setTelefone(req.getTelefone());
            user.setCep(req.getCep());
            user.setLogradouro(req.getLogradouro());
            user.setNumero(req.getNumero());
            user.setBairro(req.getBairro());
            user.setCidade(req.getCidade());
            user.setEstado(req.getEstado());
            user.setComplemento(req.getComplemento());
            user.setEndereco(req.getEndereco());

            Usuario savedUser = usuarioRepository.save(user);

            // Se for Farmácia, criar entidade Farmacia
            if ("PHARMACY".equalsIgnoreCase(req.getRole())) {
                if (req.getCnpj() == null || req.getCnpj().isEmpty()) {
                    return ResponseEntity.badRequest().body(new MensagemResponseDTO("CNPJ é obrigatório para farmácias", false));
                }
                if (req.getChavePix() == null || req.getChavePix().isEmpty()) {
                    return ResponseEntity.badRequest().body(new MensagemResponseDTO("Chave PIX é obrigatória para farmácias", false));
                }

                Farmacia farmacia = new Farmacia();
                farmacia.setNome(req.getNome());
                farmacia.setCnpj(req.getCnpj());
                farmacia.setUsuario(savedUser);
                farmacia.setCep(req.getCep());
                farmacia.setLogradouro(req.getLogradouro());
                farmacia.setNumero(req.getNumero());
                farmacia.setBairro(req.getBairro());
                farmacia.setCidade(req.getCidade());
                farmacia.setEstado(req.getEstado());
                farmacia.setComplemento(req.getComplemento());
                farmacia.setTelefone(req.getTelefone());
                farmacia.setEmail(req.getEmail());
                farmacia.setChavePix(req.getChavePix());
                farmaciaRepository.save(farmacia);
            } 
            // Se for Motoboy, criar entidade Motoboy
            else if ("COURIER".equalsIgnoreCase(req.getRole())) {
                Motoboy motoboy = new Motoboy();
                motoboy.setNome(req.getNome());
                motoboy.setCpf(req.getCpf());
                motoboy.setUsuario(savedUser);
                motoboy.setTelefone(req.getTelefone());
                motoboy.setCep(req.getCep());
                motoboy.setLogradouro(req.getLogradouro());
                motoboy.setNumero(req.getNumero());
                motoboy.setBairro(req.getBairro());
                motoboy.setCidade(req.getCidade());
                motoboy.setEstado(req.getEstado());
                motoboy.setComplemento(req.getComplemento());
                motoboy.setChavePix(req.getChavePix());
                motoboyRepository.save(motoboy);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO("Cadastro realizado com sucesso!", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO("Erro ao registrar: " + e.getMessage(), false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MensagemResponseDTO("Logout realizado com sucesso!", true));
    }
}