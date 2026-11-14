package com.powergym.gimnasio.repository;

import com.powergym.gimnasio.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreoAndPasswordHash(String correo, String passwordHash);
    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findByRolAndActivoTrue(String rol);
}