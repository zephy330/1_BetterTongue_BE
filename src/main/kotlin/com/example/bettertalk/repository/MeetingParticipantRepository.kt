package com.example.bettertalk.repository

import com.example.bettertalk.domain.meeting.MeetingParticipant
import org.springframework.data.jpa.repository.JpaRepository

interface MeetingParticipantRepository : JpaRepository<MeetingParticipant, Long> {

    fun findAllByMeetingId(meetingId: Long): List<MeetingParticipant>
}
