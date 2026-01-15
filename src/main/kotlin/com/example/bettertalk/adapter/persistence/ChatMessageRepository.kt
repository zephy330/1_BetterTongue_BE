package com.example.bettertalk.adapter.persistence

import com.example.bettertalk.domain.chat.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    fun findAllByUserIdOrderByCreatedAtAsc(userId: Long): List<ChatMessage>

    fun findAllByUserIdAndMeetingIdOrderByCreatedAtAsc(userId: Long, meetingId: Long): List<ChatMessage>

    fun findTop50ByUserIdOrderByCreatedAtDesc(userId: Long): List<ChatMessage>
}
