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
import java.util.List;

import static org.example.cnproject.DTO.ChatMessage.MessageType.JOIN;
import static org.example.cnproject.DTO.ChatMessage.MessageType.LEAVE;

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
        System.out.println("Sending message to room: " + chatMessage.getChatRoom());

        ChatRoom chatRoom = chatRoomRepository.findByName(chatMessage.getChatRoom());
        String topic = "/topic/" + chatRoom.getName();
        System.out.println("Full topic path: " + topic);
        messagingTemplate.convertAndSend("/topic/" + chatRoom.getName(), chatMessage);
    }

    public void saveMessage(ChatMessage chatMessage) {
        if (JOIN.equals(chatMessage.getType()) || LEAVE.equals(chatMessage.getType())) {
            return;
        }


        ChatRoom chatRoom = chatRoomRepository.findByName(chatMessage.getChatRoom());
        User sender = userRepository.findByUsername(chatMessage.getSender());
        if(sender == null) {
            throw new IllegalArgumentException("Invalid sender");
        }
        if(chatRoom == null) {
            throw new IllegalArgumentException("Invalid chat room");
        }

        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        if(chatMessage.getReceiver() != null){
            User receiver = userRepository.findByUsername(chatMessage.getReceiver());
            message.setReceiver(receiver);
        }
        message.setTimestamp(new Date());

        messageRepository.save(message);
    }

    public List<Message> getMessagesForRoom(String RoomName) {
        ChatRoom chatRoom = chatRoomRepository.findByName(RoomName);
        if(chatRoom == null) {
            return List.of();
        }
        return messageRepository.findByChatRoom(chatRoom);
    }

    public ChatRoom findByName(String room) {
        return chatRoomRepository.findByName(room);
    }

    public List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom) {
        return messageRepository.findByChatRoomOrderByTimestampAsc(chatRoom)
                .stream()
                .filter(msg -> msg.getContent() != null && !msg.getContent().contains("joined!"))
                .map(msg -> {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSender(msg.getSender().getUsername());
                    chatMessage.setContent(msg.getContent());
                    chatMessage.setChatRoom(chatRoom.getName());
                    chatMessage.setTimestamp(msg.getTimestamp().toString());
                    chatMessage.setType(ChatMessage.MessageType.CHAT);
                    chatMessage.setFileUrl(msg.getFilePath()); // Optional
                    return chatMessage;
                })
                .toList();
    }

    public List<Message> findReceiver(User sender, User receiver) {
        return messageRepository.findConversationBetween(sender, receiver);
    }

    public void sendPrivateMessage(ChatMessage chatMessage) {
        String receiver = chatMessage.getReceiver();
        String sender = chatMessage.getSender();
        messagingTemplate.convertAndSendToUser(receiver,"/queue/private", chatMessage);
        messagingTemplate.convertAndSendToUser(sender,"/queue/private", chatMessage);
    }
}
