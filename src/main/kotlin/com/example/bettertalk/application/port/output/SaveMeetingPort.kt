package com.example.bettertalk.application.port.output

import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingParticipant
import com.example.bettertalk.domain.meeting.MeetingRelationship

/**
 * Output Port: 회의 저장
 */
interface SaveMeetingPort {

    fun save(meeting: Meeting): Meeting
}

/**
 * Output Port: 회의 참여자 저장
 */
interface SaveMeetingParticipantPort {

    fun save(participant: MeetingParticipant): MeetingParticipant
}

/**
 * Output Port: 회의 관계 저장
 */
interface SaveMeetingRelationshipPort {

    fun save(relationship: MeetingRelationship): MeetingRelationship
}
