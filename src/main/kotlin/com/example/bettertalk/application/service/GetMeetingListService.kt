package com.example.bettertalk.application.service

import com.example.bettertalk.application.port.input.GetMeetingListUseCase
import com.example.bettertalk.application.port.output.LoadMeetingPort
import com.example.bettertalk.domain.meeting.Meeting
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMeetingListService(
    private val loadMeetingPort: LoadMeetingPort
) : GetMeetingListUseCase {

    override fun getMeetingsByUserId(userId: Long): List<Meeting> {
        return loadMeetingPort.findAllByUserIdOrderByCreatedAtDesc(userId)
    }
}
