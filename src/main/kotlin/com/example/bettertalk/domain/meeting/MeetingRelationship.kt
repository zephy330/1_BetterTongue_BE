package com.example.bettertalk.domain.meeting

import com.example.bettertalk.domain.common.BaseTimeEntity
import com.example.bettertalk.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "meeting_relationships")
class MeetingRelationship(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    val toUser: User,

    @Column(nullable = false)
    var chemistryScore: Int = 0,

    @Column(columnDefinition = "TEXT")
    var analysisResultJson: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) : BaseTimeEntity()
