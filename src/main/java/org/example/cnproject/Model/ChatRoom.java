package org.example.cnproject.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Data
@ToString(exclude = {"users", "messages"})
@EqualsAndHashCode(exclude = {"users", "messages"})
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean isPrivate;

    @ManyToMany(mappedBy = "chatRooms")
    private Set<User> users;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private Set<Message> messages;
}
