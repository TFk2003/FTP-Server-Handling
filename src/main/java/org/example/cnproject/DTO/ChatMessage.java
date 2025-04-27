package org.example.cnproject.DTO;

import lombok.Data;

@Data
public class ChatMessage {
    private String content;
    private String sender;
    private String chatRoom;
    private String timestamp;
    private String fileUrl;
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE, FILE
    }
}
