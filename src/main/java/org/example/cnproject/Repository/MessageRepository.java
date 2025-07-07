package org.example.cnproject.Repository;

import org.example.cnproject.Model.ChatRoom;
import org.example.cnproject.Model.Message;
import org.example.cnproject.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoom(ChatRoom chatRoom);

    List<Message> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversationBetween(@Param("user1") User user1, @Param("user2") User user2);
}
