package com.financeiro.backend.service;


import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.financeiro.backend.model.User;
import com.financeiro.backend.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository repository;


    public User insert(User user){
        return repository.save(user);
    }

    public List<User> listAll(){
        return repository.findAll();
    }

    public User searchById(UUID id){
        User user = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado!!"));
        return user;
    }

    public void remove(UUID id){
        repository.deleteById(id);
    }

    public User alter(User user){
        User userDB = searchById(user.getId());
        userDB.setName(user.getName());
        userDB.setEmail(user.getEmail());
        return repository.save(userDB);
    }
}

