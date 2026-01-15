package com.example.bettertalk.application.port.output

/**
 * Output Port: AI 회의 분석
 */
interface AnalyzeMeetingPort {

    fun transcribe(audioFileUrl: String): TranscriptResult

    fun analyze(script: TranscriptResult, participants: List<ParticipantProfile>): AnalysisResult
}

data class TranscriptResult(
    val speakers: List<SpeakerScript>,
    val fullScript: String
)

data class SpeakerScript(
    val speakerLabel: String,
    val userId: Long?,
    val segments: List<ScriptSegment>
)

data class ScriptSegment(
    val startTime: String,
    val endTime: String,
    val text: String
)

data class ParticipantProfile(
    val userId: Long,
    val nickname: String,
    val arrcType: String,
    val characterBadgeLabel: String?,
    val strengthTags: String?,
    val weaknessTags: String?
)

/**
 * 프론트엔드 인터페이스 기반 분석 결과
 * @see fe_readme.md - AnalyzeMeetingResponse
 */
data class AnalysisResult(
    val chemistry: ChemistryResult,
    val communicationTips: List<String>,
    val traitSliders: List<TraitSlider>,
    val coachingCards: List<CoachingCard>,
    val summary: SummaryResult,
    val aiAdvice: String
)

data class ChemistryResult(
    val score: Int,      // 0-100
    val message: String  // 점수에 대한 한줄 설명
)

data class TraitSlider(
    val label: String,
    val fromValue: Int,  // 루시의 성향 (0-100)
    val toValue: Int     // 상대방의 성향 (0-100)
)

data class CoachingCard(
    val situation: String,
    val originalScript: String,
    val refinedScript: String,
    val advice: String
)

data class SummaryResult(
    val description: String,  // 상황 설명
    val analysis: String      // 분석 내용
)
