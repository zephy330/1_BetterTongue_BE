package com.example.bettertalk.domain.chat

import com.example.bettertalk.domain.common.BaseTimeEntity
import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "chat_messages")
class ChatMessage(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    val meeting: Meeting? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: ChatRole,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseTimeEntity()

enum class ChatRole {
    USER,
    ASSISTANT
}
