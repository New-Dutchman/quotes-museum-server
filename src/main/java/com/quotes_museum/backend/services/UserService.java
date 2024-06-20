package com.quotes_museum.backend.services;

import com.quotes_museum.backend.models.users.UserDTO;
import com.quotes_museum.backend.models.users.UsersRepository;
import com.quotes_museum.backend.security.Sha256PasswordEncoder;
import com.quotes_museum.backend.security.user.QuotatorUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder = new Sha256PasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDTO user;
        try {
            user = usersRepository.findByName(username);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new QuotatorUserDetails(user.getRole(), user.getName(), user.getHashedPassword());
    }

    public String registerRegularUser(String username, String password) {
        try {
            UserDTO user = new UserDTO(username, passwordEncoder.encode(password), "ROLE_USER");
            int res = usersRepository.addUser(user);
            return "Success";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "Cant create user";
        }
    }

    public boolean login(String username, String password) throws SQLException {
        String hashedPassword = passwordEncoder.encode(password);

        return usersRepository.auth(username, hashedPassword);
    }

//    public QuotatorUserDetails getByUsername(String username) {
//        try {
//
//            UserDTO user = usersRepository.findByName(username);
//            return new QuotatorUserDetails(user.getRole(), user.getName(), user.getHashedPassword());
//        } catch ( Exception e) {
//            throw new UsernameNotFoundException("user not found");
//        }
//
//    }
//
//    public UserDetailsService userDetailsService() {
//        return this::getByUsername;
//    }
//
//    public QuotatorUserDetails getCurrentUser() {
//        // Получение имени пользователя из контекста Spring Security
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        return getByUsername(username);
//    }
}
