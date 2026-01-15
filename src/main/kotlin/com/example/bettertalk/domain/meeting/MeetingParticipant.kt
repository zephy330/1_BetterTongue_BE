package com.example.bettertalk.domain.meeting

import com.example.bettertalk.domain.common.BaseTimeEntity
import com.example.bettertalk.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "meeting_participants")
class MeetingParticipant(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 50)
    val speakerLabel: String,

    @Lob
    @Column(columnDefinition = "TEXT")
    var aiAnalysisSummary: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) : BaseTimeEntity()
