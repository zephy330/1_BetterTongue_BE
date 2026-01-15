package com.example.bettertalk.application.service

import com.example.bettertalk.application.port.input.CreateMeetingCommand
import com.example.bettertalk.application.port.input.CreateMeetingUseCase
import com.example.bettertalk.application.port.output.LoadUserPort
import com.example.bettertalk.application.port.output.SaveMeetingParticipantPort
import com.example.bettertalk.application.port.output.SaveMeetingPort
import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingParticipant
import com.example.bettertalk.domain.meeting.MeetingStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateMeetingService(
    private val saveMeetingPort: SaveMeetingPort,
    private val saveMeetingParticipantPort: SaveMeetingParticipantPort,
    private val loadUserPort: LoadUserPort
) : CreateMeetingUseCase {

    override fun createMeeting(command: CreateMeetingCommand): Meeting {
        val meeting = Meeting(
            title = command.title,
            audioFileUrl = command.audioFileUrl,
            status = MeetingStatus.PENDING
        )

        val savedMeeting = saveMeetingPort.save(meeting)

        val users = loadUserPort.findAllByIds(command.participantUserIds)
        users.forEachIndexed { index, user ->
            val participant = MeetingParticipant(
                meeting = savedMeeting,
                user = user,
                speakerLabel = "Speaker ${('A' + index)}"
            )
            saveMeetingParticipantPort.save(participant)
            savedMeeting.addParticipant(participant)
        }

        return savedMeeting
    }
}
