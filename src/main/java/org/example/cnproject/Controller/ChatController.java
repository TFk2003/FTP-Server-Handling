package org.example.cnproject.Controller;

import org.example.cnproject.DTO.ChatMessage;
import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.Message;
import org.example.cnproject.Model.User;
import org.example.cnproject.Service.ChatService;
import org.example.cnproject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sender = (String)  Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        chatMessage.setSender(sender);
        chatService.sendMessage(chatMessage);
        chatService.saveMessage(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
        return chatMessage;
    }

    @GetMapping("/chat")
    public String showChatPage(@RequestParam(required = false) String room, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("room", room != null ? room : "General");
        List<Message> messagesHistory = chatService.getMessagesForRoom(room);
        model.addAttribute("messagesHistory", messagesHistory);
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("users", allUsers);
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

    @MessageMapping("/chat.sendPrivate")
    public ChatMessage sendPrivateMessage(@Payload ChatMessage chatMessage){

        chatMessage.setChatRoom("Private");

        chatService.saveMessage(chatMessage);
        chatService.sendPrivateMessage(chatMessage);

        return chatMessage;
    }

    @GetMapping("/chat/private/history")
    @ResponseBody
    public List<ChatMessage> getPrivateChatHistory(@RequestParam String with, @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User receiver = userService.findByUsername(with);
        User sender = userService.findByUsername(userDetails.getUsername());
        List<Message> messages = chatService.findReceiver(sender, receiver);

        return messages.stream()
                .map(msg ->{
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSender(msg.getSender().getUsername());
                    chatMessage.setReceiver(msg.getReceiver().getUsername());
                    chatMessage.setContent(msg.getContent());
                    chatMessage.setTimestamp(msg.getTimestamp().toString());
                    chatMessage.setType(ChatMessage.MessageType.CHAT);
                    return chatMessage;
                })
                .toList();
    }
}
