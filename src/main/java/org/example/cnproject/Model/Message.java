package org.example.cnproject.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;
    private String filePath;

    private String content;
    private Date timestamp;

}
