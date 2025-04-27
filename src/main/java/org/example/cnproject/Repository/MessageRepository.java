package org.example.cnproject.Repository;

import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoom(ChatRoom chatRoom);
}
