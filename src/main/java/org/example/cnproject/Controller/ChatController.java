package org.example.cnproject.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.cnproject.DTO.ChatMessage;
import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.User;
import org.example.cnproject.Repository.ChatRoomRepository;
import org.example.cnproject.Repository.UserRepository;
import org.example.cnproject.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {
    @Autowired
    private ChatService chatService;
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatService.sendMessage(chatMessage);
        return chatMessage;
    }
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }
    @GetMapping("/chat")
    public String showChatPage(@RequestParam(required = false) String room, Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null || username.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("username", username);
        model.addAttribute("room", room != null ? room : "general");
        return "chat";
    }
}
