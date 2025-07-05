package org.example.cnproject.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.cnproject.DTO.ChatMessage;
import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.Message;
import org.example.cnproject.Model.User;
import org.example.cnproject.Repository.ChatRoomRepository;
import org.example.cnproject.Repository.UserRepository;
import org.example.cnproject.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sender = (String)  headerAccessor.getSessionAttributes().get("username");
        chatMessage.setSender(sender);
        chatService.sendMessage(chatMessage);
        chatService.saveMessage(chatMessage);
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
    public String showChatPage(@RequestParam(required = false) String room, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("room", room != null ? room : "general");
        List<Message> messagesHistory = chatService.getMessagesForRoom(room);
        model.addAttribute("messagesHistory", messagesHistory);
        return "chat";
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@RequestParam String room,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        ChatRoom chatRoom = chatService.findByName(room);
        if (chatRoom == null) {
            return List.of();
        }

        // Convert Message to DTO
        return chatService.findByChatRoomOrderByTimestampAsc(chatRoom);
    }
}
