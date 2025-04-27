package org.example.cnproject.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean isPrivate;
    @ManyToMany(mappedBy = "chatRooms")
    private Set<User> users;
    @OneToMany(mappedBy = "chatRoom")
    private Set<Message> messages;
}
