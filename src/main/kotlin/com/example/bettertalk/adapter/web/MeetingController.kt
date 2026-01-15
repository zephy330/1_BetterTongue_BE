package com.example.bettertalk.adapter.web

import com.example.bettertalk.adapter.openai.OpenAIAdapter
import com.example.bettertalk.adapter.web.dto.*
import com.example.bettertalk.application.port.input.*
import com.example.bettertalk.application.port.output.ParticipantProfile
import com.example.bettertalk.domain.meeting.MeetingRelationship
import com.example.bettertalk.domain.meeting.MeetingStatus
import com.example.bettertalk.repository.MeetingRelationshipRepository
import com.example.bettertalk.repository.MeetingRepository
import com.example.bettertalk.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/meetings")
class MeetingController(
    private val getMeetingListUseCase: GetMeetingListUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val createMeetingUseCase: CreateMeetingUseCase,
    private val analyzeMeetingUseCase: AnalyzeMeetingUseCase,
    private val openAIAdapter: OpenAIAdapter,
    private val meetingRepository: MeetingRepository,
    private val userRepository: UserRepository,
    private val meetingRelationshipRepository: MeetingRelationshipRepository
) {

    companion object {
        private const val CURRENT_USER_ID = 1L
    }

    /**
     * GET /api/meetings - 회의 목록 조회 (최신순)
     */
    @GetMapping
    fun getMeetings(): ResponseEntity<List<MeetingListResponse>> {
        val meetings = getMeetingListUseCase.getMeetingsByUserId(CURRENT_USER_ID)
        val response = meetings.map { MeetingListResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * POST /api/meetings - 회의 녹음 파일 업로드
     */
    @PostMapping
    fun uploadMeeting(
        @RequestParam("title") title: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Void> {
        val audioFileUrl = "/audio/${file.originalFilename}"

        val command = CreateMeetingCommand(
            title = title,
            audioFileUrl = audioFileUrl,
            participantUserIds = listOf(CURRENT_USER_ID, 2L)
        )

        val meeting = createMeetingUseCase.createMeeting(command)
        analyzeMeetingUseCase.analyzeMeeting(meeting.id)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    /**
     * GET /api/meetings/{id} - 회의 상세 조회
     */
    @GetMapping("/{id}")
    fun getMeetingDetail(@PathVariable id: Long): ResponseEntity<MeetingDetailResponse> {
        val meeting = getMeetingDetailUseCase.getMeetingById(id)
        return ResponseEntity.ok(MeetingDetailResponse.from(meeting))
    }

    /**
     * GET /api/meetings/{id}/relationships - 케미 분석 결과 조회 (전체)
     */
    @GetMapping("/{id}/relationships")
    fun getMeetingRelationships(@PathVariable id: Long): ResponseEntity<List<RelationshipResponse>> {
        val meeting = getMeetingDetailUseCase.getMeetingById(id)

        val myRelationships = meeting.relationships
            .filter { it.fromUser.id == CURRENT_USER_ID }
            .map { RelationshipResponse.from(it) }

        return ResponseEntity.ok(myRelationships)
    }

    /**
     * GET /api/meetings/{id}/relationships/{targetUserId} - 특정 참가자와의 케미 조회
     */
    @GetMapping("/{id}/relationships/{targetUserId}")
    fun getRelationshipWithUser(
        @PathVariable id: Long,
        @PathVariable targetUserId: Long
    ): ResponseEntity<RelationshipResponse> {
        val meeting = getMeetingDetailUseCase.getMeetingById(id)

        val relationship = meeting.relationships
            .find { it.fromUser.id == CURRENT_USER_ID && it.toUser.id == targetUserId }
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(RelationshipResponse.from(relationship))
    }

    /**
     * GET /api/meetings/{id}/participants - 회의 참가자 목록 조회
     */
    @GetMapping("/{id}/participants")
    fun getMeetingParticipants(@PathVariable id: Long): ResponseEntity<List<ParticipantResponse>> {
        val meeting = getMeetingDetailUseCase.getMeetingById(id)

        val participants = meeting.participants
            .filter { it.user.id != CURRENT_USER_ID }  // 본인 제외
            .map { ParticipantResponse.from(it.user) }

        return ResponseEntity.ok(participants)
    }

    /**
     * POST /api/meetings/analyze-script - 스크립트 기반 회의 분석
     * 스크립트 텍스트를 받아서 GPT 분석 후 결과 반환 (DB 저장 포함)
     */
    @PostMapping("/analyze-script")
    fun analyzeScript(@RequestBody request: ScriptAnalysisRequest): ResponseEntity<ScriptAnalysisResponse> {
        // 참가자 프로필 구성
        val participants = request.participantIds.mapNotNull { userId ->
            userRepository.findById(userId).orElse(null)?.let { user ->
                ParticipantProfile(
                    userId = user.id,
                    nickname = user.nickname,
                    arrcType = user.arrcType,
                    characterBadgeLabel = user.characterBadgeLabel,
                    strengthTags = user.strengthTags,
                    weaknessTags = user.weaknessTags
                )
            }
        }

        if (participants.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ScriptAnalysisResponse(
                    success = false,
                    message = "참가자 정보를 찾을 수 없습니다.",
                    meetingId = null,
                    analysisResult = null
                )
            )
        }

        // 회의 생성 및 저장
        val command = CreateMeetingCommand(
            title = request.title ?: "새 회의",
            audioFileUrl = null,
            participantUserIds = request.participantIds
        )
        val meeting = createMeetingUseCase.createMeeting(command)

        // 각 참가자별로 개별 GPT 분석 실행 및 저장
        val currentUser = userRepository.findById(CURRENT_USER_ID).orElse(null)
        var firstAnalysisResult: com.example.bettertalk.application.port.output.AnalysisResult? = null

        request.participantIds.filter { it != CURRENT_USER_ID }.forEach { targetUserId ->
            val targetUser = userRepository.findById(targetUserId).orElse(null)
            if (currentUser != null && targetUser != null) {
                // 각 타겟 유저별로 개별 GPT 분석 실행
                val analysisResult = openAIAdapter.analyzeWithScriptForTarget(request.script, participants, targetUserId)

                // 첫 번째 분석 결과 저장 (응답용)
                if (firstAnalysisResult == null) {
                    firstAnalysisResult = analysisResult
                }

                // 올바른 JSON 구조로 저장 (프론트엔드 DTO 형식에 맞춤)
                val analysisJson = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(
                    mapOf(
                        "chemistryLabel" to analysisResult.chemistry.message,
                        "traitSliders" to analysisResult.traitSliders.map { slider ->
                            mapOf(
                                "label" to slider.label,
                                "fromValue" to slider.fromValue,
                                "toValue" to slider.toValue
                            )
                        },
                        "communicationGuide" to analysisResult.communicationTips,
                        "coachingCards" to analysisResult.coachingCards.map { card ->
                            mapOf(
                                "situation" to card.situation,
                                "originalScript" to card.originalScript,
                                "refinedScript" to card.refinedScript,
                                "advice" to card.advice
                            )
                        },
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
                meetingRelationshipRepository.save(relationship)
            }
        }

        // 전체 요약 저장 (첫 번째 분석 결과 사용)
        firstAnalysisResult?.let { result ->
            meeting.summary = "${result.summary.description}\n\n${result.summary.analysis}"
        }
        meeting.status = MeetingStatus.COMPLETED
        meetingRepository.save(meeting)

        val responseResult = firstAnalysisResult ?: return ResponseEntity.ok(ScriptAnalysisResponse(
            success = false,
            message = "분석 결과가 없습니다.",
            meetingId = meeting.id,
            analysisResult = null
        ))

        return ResponseEntity.ok(ScriptAnalysisResponse(
            success = true,
            message = "분석 완료",
            meetingId = meeting.id,
            analysisResult = AnalysisResultDto(
                chemistry = ChemistryDto(
                    score = responseResult.chemistry.score,
                    message = responseResult.chemistry.message
                ),
                communicationTips = responseResult.communicationTips,
                summary = SummaryDto(
                    description = responseResult.summary.description,
                    analysis = responseResult.summary.analysis
                ),
                aiAdvice = responseResult.aiAdvice
            )
        ))
    }
}

// 스크립트 분석 요청/응답 DTO
data class ScriptAnalysisRequest(
    val title: String? = null,
    val script: String,
    val participantIds: List<Long> = listOf(1L, 2L, 3L)  // 기본값: 루시, 제피, 제임스
)

data class ScriptAnalysisResponse(
    val success: Boolean,
    val message: String,
    val meetingId: Long?,
    val analysisResult: AnalysisResultDto?
)

data class AnalysisResultDto(
    val chemistry: ChemistryDto,
    val communicationTips: List<String>,
    val summary: SummaryDto,
    val aiAdvice: String
)

data class ChemistryDto(
    val score: Int,
    val message: String
)

data class SummaryDto(
    val description: String,
    val analysis: String
)
