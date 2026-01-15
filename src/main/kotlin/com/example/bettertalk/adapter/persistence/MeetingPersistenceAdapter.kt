package com.example.bettertalk.adapter.persistence

import com.example.bettertalk.application.port.output.*
import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingParticipant
import com.example.bettertalk.domain.meeting.MeetingRelationship
import com.example.bettertalk.repository.MeetingParticipantRepository
import com.example.bettertalk.repository.MeetingRelationshipRepository
import com.example.bettertalk.repository.MeetingRepository
import org.springframework.stereotype.Component

@Component
class MeetingPersistenceAdapter(
    private val meetingRepository: MeetingRepository
) : LoadMeetingPort, SaveMeetingPort {

    override fun findById(id: Long): Meeting? {
        return meetingRepository.findById(id).orElse(null)
    }

    override fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Meeting> {
        return meetingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
    }

    override fun save(meeting: Meeting): Meeting {
        return meetingRepository.save(meeting)
    }
}

@Component
class MeetingParticipantPersistenceAdapter(
    private val meetingParticipantRepository: MeetingParticipantRepository
) : LoadMeetingParticipantPort, SaveMeetingParticipantPort {

    override fun findAllByMeetingId(meetingId: Long): List<MeetingParticipant> {
        return meetingParticipantRepository.findAllByMeetingId(meetingId)
    }

    override fun save(participant: MeetingParticipant): MeetingParticipant {
        return meetingParticipantRepository.save(participant)
    }
}

@Component
class MeetingRelationshipPersistenceAdapter(
    private val meetingRelationshipRepository: MeetingRelationshipRepository
) : LoadMeetingRelationshipPort, SaveMeetingRelationshipPort {

    override fun findAllByMeetingId(meetingId: Long): List<MeetingRelationship> {
        return meetingRelationshipRepository.findAllByMeetingId(meetingId)
    }

    override fun save(relationship: MeetingRelationship): MeetingRelationship {
        return meetingRelationshipRepository.save(relationship)
    }
}
