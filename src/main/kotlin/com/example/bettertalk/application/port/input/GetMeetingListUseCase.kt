package com.example.bettertalk.application.port.input

import com.example.bettertalk.domain.meeting.Meeting

/**
 * Input Port: 회의 목록 조회 (Query)
 */
interface GetMeetingListUseCase {

    fun getMeetingsByUserId(userId: Long): List<Meeting>
}
