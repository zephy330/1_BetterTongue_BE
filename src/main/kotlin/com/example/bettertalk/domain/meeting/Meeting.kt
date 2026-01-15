package com.example.bettertalk.domain.meeting

import com.example.bettertalk.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "meetings")
class Meeting(

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(length = 500)
    var audioFileUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MeetingStatus = MeetingStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(length = 500)
    var overviewImageUrl: String? = null,

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participants: MutableList<MeetingParticipant> = mutableListOf(),

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    val relationships: MutableList<MeetingRelationship> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) : BaseTimeEntity() {

    fun addParticipant(participant: MeetingParticipant) {
        participants.add(participant)
    }

    fun addRelationship(relationship: MeetingRelationship) {
        relationships.add(relationship)
    }
}
