package org.example.cnproject.Service;

import org.example.cnproject.DTO.ChatMessage;
import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.Message;
import org.example.cnproject.Model.User;
import org.example.cnproject.Repository.ChatRoomRepository;
import org.example.cnproject.Repository.MessageRepository;
import org.example.cnproject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChatService{
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;
    public void sendMessage(ChatMessage chatMessage) {
        User sender = userRepository.findByUsername(chatMessage.getSender());
        ChatRoom chatRoom = chatRoomRepository.findByName(chatMessage.getChatRoom());

        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setTimestamp(new Date());
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setFilePath(chatMessage.getFileUrl());

        messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/" + chatMessage.getChatRoom(), chatMessage);
    }
}
