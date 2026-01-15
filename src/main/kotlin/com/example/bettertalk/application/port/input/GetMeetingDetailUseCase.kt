package com.example.bettertalk.application.port.input

import com.example.bettertalk.domain.meeting.Meeting

/**
 * Input Port: 회의 상세 조회 (Query)
 */
interface GetMeetingDetailUseCase {

    fun getMeetingById(meetingId: Long): Meeting
}
