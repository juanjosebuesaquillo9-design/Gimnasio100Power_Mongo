package com.powergym.gimnasio.repository;

import com.powergym.gimnasio.model.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Optional<Cliente> findByCedula(String cedula);
    List<Cliente> findTop200ByOrderByIdClienteDesc();
}