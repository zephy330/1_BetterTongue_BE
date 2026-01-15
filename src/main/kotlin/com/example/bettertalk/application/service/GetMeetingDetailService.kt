package com.example.bettertalk.application.service

import com.example.bettertalk.application.port.input.GetMeetingDetailUseCase
import com.example.bettertalk.application.port.output.LoadMeetingPort
import com.example.bettertalk.domain.meeting.Meeting
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMeetingDetailService(
    private val loadMeetingPort: LoadMeetingPort
) : GetMeetingDetailUseCase {

    override fun getMeetingById(meetingId: Long): Meeting {
        return loadMeetingPort.findById(meetingId)
            ?: throw NoSuchElementException("Meeting not found: $meetingId")
    }
}
