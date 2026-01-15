package com.example.bettertalk.application.port.input

import com.example.bettertalk.domain.meeting.Meeting

/**
 * Input Port: 회의 생성 (Command)
 */
interface CreateMeetingUseCase {

    fun createMeeting(command: CreateMeetingCommand): Meeting
}

data class CreateMeetingCommand(
    val title: String,
    val audioFileUrl: String? = null,
    val participantUserIds: List<Long>
)
