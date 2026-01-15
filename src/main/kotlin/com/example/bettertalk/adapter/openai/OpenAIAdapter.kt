package com.example.bettertalk.adapter.openai

import com.example.bettertalk.application.port.output.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OpenAIAdapter(
    private val openAIWebClient: WebClient
) : AnalyzeMeetingPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    companion object {
        private const val MODEL = "gpt-5.2-2025-12-11"

        private val KAKAO_PRINCIPLES = """
            ## 카카오 8대 원칙 (Kakao Style)
            1. **Act for Kakao** - 조직 전체의 이익을 우선시
            2. **Solve User Problems** - 사용자 가치 중심 사고
            3. **No Reliance on Names** - 직급/경력 무관 수평적 소통 (신충헌)
            4. **No Limits by Experience** - 경험에 갇히지 않는 해결책
            5. **Aim High** - 본질적 해결을 목표
            6. **Speak Honestly** - 솔직하고 투명한 소통
            7. **Collaborate across Boundaries** - 경계를 넘는 협업
            8. **Respect & Commit** - 결정을 존중하고 헌신
        """.trimIndent()

        private val ARRC_MODEL = """
            ## ARRC 성향 모델
            - **TYPE_ANALYTIC (분석형)**: 논리적, 데이터 중심, 두괄식 선호, 감정보다 팩트
            - **TYPE_SUPPORTIVE (지지형)**: 경청, 공감, 배려 중심, 갈등 회피 경향
            - **TYPE_DRIVER (주도형)**: 결과 지향, 직설적, 빠른 의사결정 선호
            - **TYPE_EXPRESSIVE (표현형)**: 창의적, 열정적, 관계 중심, 인정욕구
        """.trimIndent()
    }

    override fun transcribe(audioFileUrl: String): TranscriptResult {
        log.info("[OpenAI] Transcribe 호출 - audioFileUrl: {}", audioFileUrl)
        // 실제로는 Whisper API 호출하지만, 해커톤에서는 스크립트 파일 직접 사용
        return TranscriptResult(
            speakers = emptyList(),
            fullScript = ""
        )
    }

    override fun analyze(script: TranscriptResult, participants: List<ParticipantProfile>): AnalysisResult {
        log.info("[OpenAI] 실제 GPT API 분석 시작 - 참여자 수: {}", participants.size)

        val participantInfo = participants.joinToString("\n") { p ->
            "- ${p.nickname} (ID: ${p.userId}): ${p.arrcType}, 특성: ${p.characterBadgeLabel}, 강점: ${p.strengthTags}, 약점: ${p.weaknessTags}"
        }

        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(script.fullScript, participantInfo, participants)

        val response = callGPT(systemPrompt, userPrompt)
        return parseAnalysisResponse(response, participants)
    }

    fun analyzeWithScript(scriptContent: String, participants: List<ParticipantProfile>): AnalysisResult {
        // 기본값: 첫 번째 타겟 유저에 대해 분석
        val targetUserId = participants.find { it.userId != 1L }?.userId ?: 2L
        return analyzeWithScriptForTarget(scriptContent, participants, targetUserId)
    }

    /**
     * 특정 타겟 유저에 대한 케미 분석 실행
     */
    fun analyzeWithScriptForTarget(scriptContent: String, participants: List<ParticipantProfile>, targetUserId: Long): AnalysisResult {
        val targetUser = participants.find { it.userId == targetUserId }
        log.info("[OpenAI] 스크립트 기반 GPT 분석 시작 - 타겟: ${targetUser?.nickname ?: targetUserId}")

        val participantInfo = participants.joinToString("\n") { p ->
            "- ${p.nickname} (ID: ${p.userId}): ${p.arrcType}, 특성: ${p.characterBadgeLabel}, 강점: ${p.strengthTags}, 약점: ${p.weaknessTags}"
        }

        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPromptForTarget(scriptContent, participantInfo, participants, targetUserId)

        val response = callGPT(systemPrompt, userPrompt)
        return parseAnalysisResponse(response, participants)
    }

    private fun buildSystemPrompt(): String {
        return """
            당신은 카카오의 회의 문화 코치 AI입니다.
            회의 스크립트를 분석하여 참가자들의 소통 방식을 평가하고,
            더 나은 협업을 위한 구체적인 코칭을 제공합니다.

            $KAKAO_PRINCIPLES

            $ARRC_MODEL

            ## 분석 원칙
            1. 각 참가자의 ARRC 성향을 고려하여 갈등 원인을 파악
            2. 카카오 8대 원칙 중 어떤 부분이 잘 지켜졌고, 어떤 부분이 개선 필요한지 분석
            3. 추상적 조언이 아닌 **구체적인 대안 스크립트** 제공
            4. 상대방 성향에 맞춘 **맞춤형 소통 가이드** 제시

            ## 응답 형식
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력합니다.
        """.trimIndent()
    }

    private fun buildUserPrompt(script: String, participantInfo: String, participants: List<ParticipantProfile>): String {
        val targetUserId = participants.find { it.userId != 1L }?.userId ?: 2L
        return buildUserPromptForTarget(script, participantInfo, participants, targetUserId)
    }

    private fun buildUserPromptForTarget(script: String, participantInfo: String, participants: List<ParticipantProfile>, targetUserId: Long): String {
        val currentUser = participants.find { it.userId == 1L } // 루시 (현재 사용자)
        val targetUser = participants.find { it.userId == targetUserId } // 분석 대상 (제피 또는 제임스)

        val currentName = currentUser?.nickname ?: "루시"
        val targetName = targetUser?.nickname ?: "상대방"

        return """
            ## 회의 참가자 정보
            $participantInfo

            ## 회의 스크립트
            $script

            ## 분석 요청
            위 회의를 분석하여 다음 JSON 형식으로 응답해주세요.
            분석 관점: ${currentName}와 ${targetName} 사이의 케미 분석

            ```json
            {
              "chemistry": {
                "score": 0-100 사이 점수,
                "message": "점수에 대한 한줄 설명 (예: '아직 쿵짝이 안 맞아요', '서로 다른 리듬으로 소통해요')"
              },
              "communicationTips": [
                "${targetName}와 소통할 때 유용한 팁 1 (한 문장)",
                "${targetName}와 소통할 때 유용한 팁 2 (한 문장)",
                "${targetName}와 소통할 때 유용한 팁 3 (한 문장)"
              ],
              "traitSliders": [
                {"label": "직설 vs 쿠션어", "fromValue": ${currentName}의 성향 0-100, "toValue": ${targetName}의 성향 0-100},
                {"label": "감정 vs 논리", "fromValue": ${currentName}의 성향 0-100, "toValue": ${targetName}의 성향 0-100},
                {"label": "경청 vs 주장", "fromValue": ${currentName}의 성향 0-100, "toValue": ${targetName}의 성향 0-100}
              ],
              "coachingCards": [
                {
                  "situation": "회의 중 ${currentName}와 ${targetName} 사이의 구체적인 갈등/어려움 상황 (스크립트에서 발췌)",
                  "originalScript": "${currentName}가 실제로 한 말 (원문 인용)",
                  "refinedScript": "더 효과적으로 말했다면 이렇게 (개선된 버전)",
                  "advice": "왜 이렇게 말하면 좋은지 설명 (1-2문장)"
                }
              ],
              "summary": {
                "description": "회의 상황 설명 (2-3문장, 무슨 주제로 어떤 논의가 있었는지)",
                "analysis": "카카오 8대 원칙 기준 ${currentName}와 ${targetName}의 소통 방식 분석 (3-4문장)"
              },
              "aiAdvice": "카나(AI 어시스턴트)의 조언 메시지 - ${currentName}님이 ${targetName}와 더 잘 소통하기 위한 따뜻하고 구체적인 한마디 (2-3문장)"
            }
            ```

            중요:
            - 점수가 낮을수록 소통이 어려움, 높을수록 잘 맞음
            - traitSliders의 fromValue는 ${currentName}의 성향, toValue는 ${targetName}의 성향 (0=왼쪽 극단, 100=오른쪽 극단)
            - communicationTips는 ${currentName}가 ${targetName}와 대화할 때 바로 활용할 수 있는 실용적인 팁
            - coachingCards는 실제 스크립트에서 ${currentName}와 ${targetName} 사이의 구체적인 상황을 발췌하여 개선 방안 제시
            - aiAdvice는 친근하고 공감하는 톤으로, "${currentName}님" 호칭 사용
        """.trimIndent()
    }

    private fun callGPT(systemPrompt: String, userPrompt: String): String {
        log.info("[OpenAI] GPT API 호출 시작")

        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            ),
            "temperature" to 0.7,
            "max_completion_tokens" to 4000
        )

        return try {
            val response = openAIWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .exchangeToMono { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono(String::class.java)
                            .doOnNext { body ->
                                log.error("[OpenAI] API 에러 응답 ({}): {}", clientResponse.statusCode(), body)
                            }
                            .flatMap { body ->
                                reactor.core.publisher.Mono.error<Map<*, *>>(RuntimeException("OpenAI API 에러: $body"))
                            }
                    } else {
                        clientResponse.bodyToMono(Map::class.java)
                    }
                }
                .block()

            @Suppress("UNCHECKED_CAST")
            val choices = response?.get("choices") as? List<Map<String, Any>>
            val message = choices?.firstOrNull()?.get("message") as? Map<String, Any>
            val content = message?.get("content") as? String ?: ""

            log.info("[OpenAI] GPT 응답 수신 완료")
            log.debug("[OpenAI] 응답 내용: {}", content)

            content
        } catch (e: Exception) {
            log.error("[OpenAI] GPT API 호출 실패", e)
            throw RuntimeException("OpenAI API 호출 실패: ${e.message}", e)
        }
    }

    private fun parseAnalysisResponse(response: String, participants: List<ParticipantProfile>): AnalysisResult {
        return try {
            // JSON 블록 추출 (```json ... ``` 형태일 수 있음)
            val jsonContent = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsed = objectMapper.readValue(jsonContent, FrontendAnalysisResponse::class.java)

            AnalysisResult(
                chemistry = ChemistryResult(
                    score = parsed.chemistry.score,
                    message = parsed.chemistry.message
                ),
                communicationTips = parsed.communicationTips,
                traitSliders = parsed.traitSliders?.map { slider ->
                    TraitSlider(
                        label = slider.label,
                        fromValue = slider.fromValue,
                        toValue = slider.toValue
                    )
                } ?: emptyList(),
                coachingCards = parsed.coachingCards?.map { card ->
                    CoachingCard(
                        situation = card.situation,
                        originalScript = card.originalScript,
                        refinedScript = card.refinedScript,
                        advice = card.advice
                    )
                } ?: emptyList(),
                summary = SummaryResult(
                    description = parsed.summary.description,
                    analysis = parsed.summary.analysis
                ),
                aiAdvice = parsed.aiAdvice
            )
        } catch (e: Exception) {
            log.error("[OpenAI] 응답 파싱 실패: {}", response, e)
            // 파싱 실패 시 기본값 반환
            createDefaultAnalysisResult()
        }
    }

    private fun createDefaultAnalysisResult(): AnalysisResult {
        return AnalysisResult(
            chemistry = ChemistryResult(
                score = 50,
                message = "분석 중 오류가 발생했습니다."
            ),
            communicationTips = listOf("분석 결과를 가져올 수 없습니다."),
            traitSliders = listOf(
                TraitSlider("직설 vs 쿠션어", 70, 30),
                TraitSlider("감정 vs 논리", 70, 30),
                TraitSlider("경청 vs 주장", 80, 30)
            ),
            coachingCards = emptyList(),
            summary = SummaryResult(
                description = "회의 분석 중 오류가 발생했습니다.",
                analysis = "다시 시도해주세요."
            ),
            aiAdvice = "죄송합니다. 분석 중 문제가 발생했어요. 다시 시도해주세요!"
        )
    }
}

// GPT 응답 파싱용 데이터 클래스 (프론트엔드 인터페이스 기반)
data class FrontendAnalysisResponse(
    val chemistry: ChemistryResponse,
    val communicationTips: List<String>,
    val traitSliders: List<TraitSliderResponse>? = null,
    val coachingCards: List<CoachingCardResponse>? = null,
    val summary: SummaryResponse,
    val aiAdvice: String
)

data class TraitSliderResponse(
    val label: String,
    val fromValue: Int,
    val toValue: Int
)

data class CoachingCardResponse(
    val situation: String,
    val originalScript: String,
    val refinedScript: String,
    val advice: String
)

data class ChemistryResponse(
    val score: Int,
    val message: String
)

data class SummaryResponse(
    val description: String,
    val analysis: String
)
