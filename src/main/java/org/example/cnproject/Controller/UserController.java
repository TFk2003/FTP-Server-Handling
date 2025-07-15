package org.example.cnproject.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cnproject.DTO.LoginRequest;
import org.example.cnproject.Model.User;
import org.example.cnproject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @GetMapping("/login")
    public String showLoginPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/chat";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, Model model) {
        try{
            String jwtToken = userService.verify(loginRequest);
            System.out.println("Token generated: " + jwtToken);
            return "redirect:/chat";
        }
        catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            model.addAttribute("error", "Invalid username or password. Please try again.");
            return "login";
        }
    }
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           Model model) {
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setEmail(email);
        userService.registerUser(user);

        return "redirect:/login";
    }

//    @GetMapping("/logout")
//    public String handleLogout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal UserDetails userDetails) {
//        SecurityContextHolder.clearContext();
//        Cookie cookie = new Cookie("JWT", null);
//        cookie.setMaxAge(0);
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//        response.addCookie(cookie);
//        response.setHeader("Authorization", "");
//        userDetails = null;
//        return "redirect:/login";
//    }
}
