package com.example.kurs.service;

import com.example.kurs.entity.Role;
import com.example.kurs.entity.User;
import com.example.kurs.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
         return userRepository.findAll();
    }
    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }
    public void updateUserRole(int id, Role role) throws Exception {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isPresent()){
            User user = optUser.get();
            user.setRole(role);
            userRepository.save(user);
        }
        else{
            throw new Exception("Пользователь не существует");
        }
    }
    public Optional<User> findByLogin(String login){
        return userRepository.findByLogin(login);
    }
    @Transactional
    public User registerNewUser(User user) throws Exception{
        if (userRepository.existsByLogin(user.getLogin())){
            throw new Exception("Пользователь с таким логином уже существует");
        }
        String password = user.getPassword();
        if (password.length() < 8){
            throw new Exception("Пароль должен быть больше 7 символов");
        }
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        if (user.getRole() == null){
            user.setRole(Role.REVIEWER);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User registerAdminUser(User user, Role role) throws Exception{
        if (userRepository.existsByLogin(user.getLogin())){
            throw new Exception("Пользователь с таким логином уже существует");
        }
        String password = user.getPassword();
        if (password.length() < 8){
            throw new Exception("Пароль должен быть больше 7 символов");
        }
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        user.setRole(role);
        return userRepository.save(user);
    }
    @Transactional
    public User createSystemUser(String login, String rawPassword, Role role, String fullname) {
        Optional<User> existingUser = userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setRole(role);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setFullname(fullname);
        return userRepository.save(newUser);
    }
}
