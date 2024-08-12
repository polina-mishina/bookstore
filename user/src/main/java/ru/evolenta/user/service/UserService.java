package ru.evolenta.user.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.evolenta.user.dto.UpdateUserRequest;
import ru.evolenta.user.model.User;
import ru.evolenta.user.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public ResponseEntity<User> createUser(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(user));
    }

    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public ResponseEntity<User> updateCurrentUser(UpdateUserRequest userRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = getByUsername(username);

        if(!user.getUsername().equals(userRequest.getUsername()) &&
                repository.existsByUsername(userRequest.getUsername())
        ) {
            return ResponseEntity.badRequest().build();
        }

        BeanUtils.copyProperties(userRequest, user);
        return ResponseEntity.ok(repository.save(user));
    }

    public ResponseEntity<User> getUser(long id) {
        Optional<User> userOptional = repository.findById(id);
        return userOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<User> updateUser(long id, UpdateUserRequest userRequest) {
        Optional<User> userOptional = repository.findById(id);
        if(userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if(!userOptional.get().getUsername().equals(userRequest.getUsername()) &&
                repository.existsByUsername(userRequest.getUsername())
        ) {
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = userOptional.get();
        BeanUtils.copyProperties(userRequest, updatedUser);
        return ResponseEntity.ok(repository.save(updatedUser));
    }

    public ResponseEntity<User> deleteUser(long id) {
        Optional<User> userOptional = repository.findById(id);
        if(userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User deletedUser = userOptional.get();
        repository.deleteById(id);
        return ResponseEntity.ok(deletedUser);
    }
}