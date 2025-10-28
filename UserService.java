package com.library.service;

import com.library.entity.LibraryUser;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LibraryUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return user;
    }
    
    public Optional<LibraryUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<LibraryUser> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public LibraryUser save(LibraryUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public LibraryUser createDefaultUser() {
        if (!existsByUsername("student1")) {
            LibraryUser defaultUser = new LibraryUser();
            defaultUser.setUsername("student1");
            defaultUser.setPassword("pass123");
            defaultUser.setEmail("student1@library.com");
            defaultUser.setFullName("Default Student");
            defaultUser.setRole(LibraryUser.Role.STUDENT);
            return save(defaultUser);
        }
        return userRepository.findByUsername("student1").orElse(null);
    }
}













