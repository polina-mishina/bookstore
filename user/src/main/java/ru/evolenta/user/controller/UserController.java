package ru.evolenta.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.evolenta.user.dto.UpdateUserRequest;
import ru.evolenta.user.model.User;
import ru.evolenta.user.repository.UserRepository;
import ru.evolenta.user.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService service;

    @GetMapping("/me")
    public User getCurrentUser() {
        return service.getCurrentUser();
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody UpdateUserRequest updateUserRequest) {
        return service.updateCurrentUser(updateUserRequest);
    }

    @GetMapping
    public Iterable<User> getUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        return service.getUser(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody UpdateUserRequest updateUserRequest) {
        return service.updateUser(id, updateUserRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable long id) {
        return service.deleteUser(id);
    }
}