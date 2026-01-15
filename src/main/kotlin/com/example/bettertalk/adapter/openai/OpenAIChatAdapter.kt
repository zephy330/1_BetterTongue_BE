package com.example.bettertalk.adapter.openai

import com.example.bettertalk.adapter.web.ChatMessage
import com.example.bettertalk.adapter.web.MeetingContext
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class OpenAIChatAdapter(
    private val openAIWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    companion object {
        private const val MODEL = "gpt-5.2-2025-12-11"

        private val SYSTEM_PROMPT = """
            당신은 '카나'입니다. 카카오의 회의 소통 코치 AI 어시스턴트예요.

            ## 카나의 성격
            - 친근하고 따뜻한 말투 사용 ("~요", "~네요", "~죠")
            - 공감을 먼저 하고, 그 다음 조언
            - 이모지는 사용하지 않음
            - 짧고 핵심적인 답변 (2-4문장)
            - 상대방을 "루시님"으로 호칭

            ## 역할
            - 회의에서 있었던 소통 문제에 대해 상담
            - 다른 참가자(제피, 제임스)와의 관계 개선 조언
            - 카카오 8대 원칙 기반의 소통 팁 제공
            - 감정적 지지와 격려

            ## 카카오 8대 원칙 (참고)
            1. Act for Kakao - 조직 전체 이익 우선
            2. Solve User Problems - 사용자 가치 중심
            3. No Reliance on Names - 수평적 소통
            4. No Limits by Experience - 경험에 갇히지 않기
            5. Aim High - 본질적 해결
            6. Speak Honestly - 솔직한 소통
            7. Collaborate across Boundaries - 경계 넘는 협업
            8. Respect & Commit - 결정 존중과 헌신

            ## 주의사항
            - 너무 길게 답하지 않기
            - 판단하지 않고 공감 먼저
            - 구체적이고 실행 가능한 팁 제공
        """.trimIndent()
    }

    fun chat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        meetingContext: MeetingContext?
    ): String {
        log.info("[Chat] 사용자 메시지: {}", userMessage)

        val messages = mutableListOf<Map<String, String>>()

        // 시스템 프롬프트
        var systemContent = SYSTEM_PROMPT
        if (meetingContext != null) {
            systemContent += "\n\n## 현재 회의 컨텍스트\n"
            meetingContext.summary?.let { systemContent += "회의 요약: $it\n" }
            meetingContext.participants?.let { systemContent += "참가자: ${it.joinToString(", ")}\n" }
            meetingContext.analysisResult?.let { systemContent += "분석 결과: $it\n" }
        }
        messages.add(mapOf("role" to "system", "content" to systemContent))

        // 대화 히스토리
        conversationHistory.forEach { msg ->
            messages.add(mapOf("role" to msg.role, "content" to msg.content))
        }

        // 현재 사용자 메시지
        messages.add(mapOf("role" to "user", "content" to userMessage))

        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to messages,
            "temperature" to 0.8,
            "max_completion_tokens" to 500
        )

        return try {
            val response = openAIWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .exchangeToMono { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono(String::class.java)
                            .doOnNext { body ->
                                log.error("[Chat] API 에러: {}", body)
                            }
                            .flatMap { body ->
                                reactor.core.publisher.Mono.error<Map<*, *>>(
                                    RuntimeException("OpenAI API 에러: $body")
                                )
                            }
                    } else {
                        clientResponse.bodyToMono(Map::class.java)
                    }
                }
                .block()

            @Suppress("UNCHECKED_CAST")
            val choices = response?.get("choices") as? List<Map<String, Any>>
            val message = choices?.firstOrNull()?.get("message") as? Map<String, Any>
            val content = message?.get("content") as? String ?: "죄송해요, 잠시 문제가 생겼어요."

            log.info("[Chat] 응답: {}", content)
            content
        } catch (e: Exception) {
            log.error("[Chat] 에러 발생", e)
            "죄송해요, 잠시 문제가 생겼어요. 다시 말씀해주시겠어요?"
        }
    }

    /**
     * 스트리밍 챗 - SSE로 토큰 단위 응답
     */
    fun chatStream(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        meetingContext: MeetingContext?
    ): Flux<String> {
        log.info("[Chat Stream] 사용자 메시지: {}", userMessage)

        val messages = mutableListOf<Map<String, String>>()

        // 시스템 프롬프트
        var systemContent = SYSTEM_PROMPT
        if (meetingContext != null) {
            systemContent += "\n\n## 현재 회의 컨텍스트\n"
            meetingContext.summary?.let { systemContent += "회의 요약: $it\n" }
            meetingContext.participants?.let { systemContent += "참가자: ${it.joinToString(", ")}\n" }
            meetingContext.analysisResult?.let { systemContent += "분석 결과: $it\n" }
        }
        messages.add(mapOf("role" to "system", "content" to systemContent))

        // 대화 히스토리
        conversationHistory.forEach { msg ->
            messages.add(mapOf("role" to msg.role, "content" to msg.content))
        }

        // 현재 사용자 메시지
        messages.add(mapOf("role" to "user", "content" to userMessage))

        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to messages,
            "temperature" to 0.8,
            "max_completion_tokens" to 500,
            "stream" to true
        )

        return openAIWebClient.post()
            .uri("/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String::class.java)
            .filter { it.isNotBlank() && !it.contains("[DONE]") }
            .map { line ->
                try {
                    // data: prefix 제거 (여러 형태 대응)
                    val cleanLine = line
                        .removePrefix("data:")
                        .removePrefix("data: ")
                        .trim()

                    if (cleanLine.isEmpty() || cleanLine == "[DONE]") {
                        ""
                    } else if (cleanLine.startsWith("{")) {
                        // JSON 파싱
                        val node = objectMapper.readTree(cleanLine)
                        node.path("choices").firstOrNull()
                            ?.path("delta")
                            ?.path("content")
                            ?.asText() ?: ""
                    } else {
                        // 이미 텍스트인 경우 그대로 반환
                        cleanLine
                    }
                } catch (e: Exception) {
                    log.debug("[Chat Stream] 파싱 실패: {}", line)
                    ""
                }
            }
            .filter { it.isNotEmpty() }
            .doOnComplete { log.info("[Chat Stream] 완료") }
            .doOnError { e -> log.error("[Chat Stream] 에러", e) }
    }
}
