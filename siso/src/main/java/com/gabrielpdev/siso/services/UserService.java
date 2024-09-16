package com.gabrielpdev.siso.services;

import java.util.Objects;
import java.util.Optional;

import com.gabrielpdev.siso.models.CustomUserDetails;
import com.gabrielpdev.siso.models.exceptions.AuthorizationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.gabrielpdev.siso.models.User;
import com.gabrielpdev.siso.dtos.UserCreateDTO;
import com.gabrielpdev.siso.dtos.UserUpdateDTO;
import com.gabrielpdev.siso.repositories.UserRepository;
import com.gabrielpdev.siso.models.exceptions.DataBindingViolationException;
import com.gabrielpdev.siso.models.exceptions.ObjectNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public User findById(Long id) {
        if(!(isSelf(id) || isAdmin())) throw new AuthorizationException("Acesso negado.");
        Optional<User> obj = this.userRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Usuário {id:"+id+"} não encontrado"));
    }

    @Transactional
    public void createUser(User obj) {
        if(!isAdmin()) throw new AuthorizationException("Acesso negado.");
        obj.setId(null);
        obj.setPassword(bCryptPasswordEncoder.encode(obj.getPassword()));
        obj.setAtivo(true);
        this.userRepository.save(obj);
    }

    @Transactional
    public void updateUser(User obj) {
        if(hasRoot(obj) ||  !(isSelf(obj.getId()) || isAdmin())) throw new AuthorizationException("Acesso negado.");
        User newObj = this.findById(obj.getId());
        if(isSelf(obj.getId())) {
            newObj.setPassword(bCryptPasswordEncoder.encode(obj.getPassword()));
        }
        newObj.setEmail(obj.getEmail());
        if(isAdmin()) {
            newObj.setProfiles(obj.getProfiles());
            newObj.setAtivo(obj.getAtivo());
        }
        this.userRepository.save(newObj);
    }

    public void deleteUserById(Long id) {
        findById(id);
        if(!isAdmin()) throw new AuthorizationException("Acesso negado.");
        try {
            this.userRepository.deleteById(id);
        } catch (Exception e) {
            throw new DataBindingViolationException("O usuário não pode ser deletado pois existem objetos relacionados a ele");
        }
    }

    public User fromDTO(@Valid UserCreateDTO obj) {
        User user = new User();
        user.setUsername(obj.getUsername());
        user.setPassword(obj.getPassword());
        user.setEmail(obj.getEmail());
        return user;
    }

    public Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found")); // Lança exceção se não encontrado
    }

    public User fromDTO(@Valid UserUpdateDTO obj) {
        User user = new User();
        user.setPassword(obj.getPassword());
        user.setEmail(obj.getEmail());
        user.setProfiles(obj.getProfiles());
        user.setAtivo(obj.getAtivo());
        return user;
    }

    public CustomUserDetails authenticated() {
        try {
            return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAuthenticated() {
        if(Objects.isNull(authenticated())) {
            throw new AuthorizationException("Acesso negado.");
        }
        return true;
    }

    public boolean isSelf(Long id) {
        return id.equals(authenticated().getId()) && isAuthenticated();
    }

    public boolean isAdmin() {
        return authenticated().hasRole("ADMIN") && isAuthenticated();
    }

    public boolean isRoot() {
        return authenticated().hasRole("ROOT") && isAuthenticated();
    }

    public boolean hasRoot(User obj) {
        return obj.getProfiles().contains("ROOT");
    }

    @Component
    public class InitialDataLoader {

        @Autowired
        private UserService userService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private BCryptPasswordEncoder bCryptPasswordEncoder;

        @PostConstruct
        public void init() {
            // Verifica se o usuário ROOT já existe
            if (userRepository.findByUsername("rootuser").isEmpty()) {
                User rootUser = new User();
                rootUser.setUsername("rootuser");
                rootUser.setPassword(bCryptPasswordEncoder.encode("rootpassword"));
                rootUser.setEmail("rootuser@example.com");
                rootUser.setAtivo(true);
                rootUser.addProfile("ROOT");
                userRepository.save(rootUser);
            }
        }
    }

}
