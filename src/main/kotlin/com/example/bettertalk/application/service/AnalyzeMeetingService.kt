package com.example.bettertalk.application.service

import com.example.bettertalk.application.port.input.AnalyzeMeetingUseCase
import com.example.bettertalk.application.port.output.*
import com.example.bettertalk.domain.meeting.MeetingRelationship
import com.example.bettertalk.domain.meeting.MeetingStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnalyzeMeetingService(
    private val loadMeetingPort: LoadMeetingPort,
    private val saveMeetingPort: SaveMeetingPort,
    private val loadMeetingParticipantPort: LoadMeetingParticipantPort,
    private val saveMeetingParticipantPort: SaveMeetingParticipantPort,
    private val saveMeetingRelationshipPort: SaveMeetingRelationshipPort,
    private val loadUserPort: LoadUserPort,
    private val analyzeMeetingPort: AnalyzeMeetingPort
) : AnalyzeMeetingUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @Transactional
    override fun analyzeMeeting(meetingId: Long) {
        log.info("[AnalyzeMeetingService] 회의 분석 시작 - meetingId: {}", meetingId)

        try {
            val meeting = loadMeetingPort.findById(meetingId)
                ?: throw NoSuchElementException("Meeting not found: $meetingId")

            // Step 1: Context Loading
            val participants = loadMeetingParticipantPort.findAllByMeetingId(meetingId)
            val participantProfiles = participants.map { participant ->
                ParticipantProfile(
                    userId = participant.user.id,
                    nickname = participant.user.nickname,
                    arrcType = participant.user.arrcType,
                    characterBadgeLabel = participant.user.characterBadgeLabel,
                    strengthTags = participant.user.strengthTags,
                    weaknessTags = participant.user.weaknessTags
                )
            }

            // Step 2: Transcribe (STT)
            val audioFileUrl = meeting.audioFileUrl
                ?: throw IllegalStateException("Audio file URL is missing")

            log.info("[AnalyzeMeetingService] Transcribe 시작")
            val transcript = analyzeMeetingPort.transcribe(audioFileUrl)

            // Step 3: Analyze (AI 분석)
            log.info("[AnalyzeMeetingService] Analyze 시작")
            val analysisResult = analyzeMeetingPort.analyze(transcript, participantProfiles)

            // Step 4: Save Results (프론트엔드 인터페이스 기반)
            meeting.summary = "${analysisResult.summary.description}\n\n${analysisResult.summary.analysis}"
            meeting.status = MeetingStatus.COMPLETED

            // 케미 분석 결과를 관계로 저장 (루시-제피 기준)
            val currentUser = loadUserPort.findById(1L) // 루시 (현재 사용자)
            val targetUser = loadUserPort.findById(2L)  // 제피 (분석 대상)

            if (currentUser != null && targetUser != null) {
                // analysisResultJson에 프론트엔드 형식 저장
                val analysisJson = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(
                    mapOf(
                        "chemistry" to mapOf(
                            "score" to analysisResult.chemistry.score,
                            "message" to analysisResult.chemistry.message
                        ),
                        "communicationTips" to analysisResult.communicationTips,
                        "aiAdvice" to analysisResult.aiAdvice
                    )
                )

                val relationship = MeetingRelationship(
                    meeting = meeting,
                    fromUser = currentUser,
                    toUser = targetUser,
                    chemistryScore = analysisResult.chemistry.score,
                    analysisResultJson = analysisJson
                )
                saveMeetingRelationshipPort.save(relationship)
                meeting.addRelationship(relationship)
            }

            saveMeetingPort.save(meeting)
            log.info("[AnalyzeMeetingService] 회의 분석 완료 - meetingId: {}", meetingId)

        } catch (e: Exception) {
            log.error("[AnalyzeMeetingService] 회의 분석 실패 - meetingId: {}", meetingId, e)

            loadMeetingPort.findById(meetingId)?.let { meeting ->
                meeting.status = MeetingStatus.FAILED
                saveMeetingPort.save(meeting)
            }
        }
    }
}
