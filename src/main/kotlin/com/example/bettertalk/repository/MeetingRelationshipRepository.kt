package com.example.bettertalk.repository

import com.example.bettertalk.domain.meeting.MeetingRelationship
import org.springframework.data.jpa.repository.JpaRepository

interface MeetingRelationshipRepository : JpaRepository<MeetingRelationship, Long> {

    fun findAllByMeetingId(meetingId: Long): List<MeetingRelationship>

    fun findByMeetingIdAndFromUserIdAndToUserId(
        meetingId: Long,
        fromUserId: Long,
        toUserId: Long
    ): MeetingRelationship?
}
