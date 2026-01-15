package com.example.bettertalk.application.port.output

import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingParticipant
import com.example.bettertalk.domain.meeting.MeetingRelationship

/**
 * Output Port: 회의 조회
 */
interface LoadMeetingPort {

    fun findById(id: Long): Meeting?

    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Meeting>
}

/**
 * Output Port: 회의 참여자 조회
 */
interface LoadMeetingParticipantPort {

    fun findAllByMeetingId(meetingId: Long): List<MeetingParticipant>
}

/**
 * Output Port: 회의 관계 조회
 */
interface LoadMeetingRelationshipPort {

    fun findAllByMeetingId(meetingId: Long): List<MeetingRelationship>
}
