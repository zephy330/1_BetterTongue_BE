package com.example.bettertalk.adapter.web

import com.example.bettertalk.adapter.openai.OpenAIAdapter
import com.example.bettertalk.application.port.output.ParticipantProfile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
@RequestMapping("/api/test")
class TestController(
    private val openAIAdapter: OpenAIAdapter
) {

    /**
     * 실제 GPT API를 호출하여 스크립트 분석 테스트
     * GET /api/test/analyze
     */
    @GetMapping("/analyze")
    fun analyzeScript(): ResponseEntity<Any> {
        // docs/script.txt 파일 읽기
        val scriptFile = File("docs/script.txt")
        val scriptContent = if (scriptFile.exists()) {
            scriptFile.readText()
        } else {
            return ResponseEntity.badRequest().body(mapOf("error" to "script.txt not found"))
        }

        // 참가자 정보 (스크립트 기반)
        val participants = listOf(
            ParticipantProfile(
                userId = 1L,
                nickname = "루시",
                arrcType = "TYPE_SUPPORTIVE",
                characterBadgeLabel = "소심형",
                strengthTags = "경청,공감,배려",
                weaknessTags = "주장,결단"
            ),
            ParticipantProfile(
                userId = 2L,
                nickname = "제피",
                arrcType = "TYPE_DRIVER",
                characterBadgeLabel = "자기중심적",
                strengthTags = "추진력,명확함,효율",
                weaknessTags = "공감,배려,경청"
            ),
            ParticipantProfile(
                userId = 3L,
                nickname = "제임스",
                arrcType = "TYPE_EXPRESSIVE",
                characterBadgeLabel = "절제된 리더",
                strengthTags = "리더십,유머,조율",
                weaknessTags = "디테일,구체성"
            )
        )

        // 실제 GPT 분석 호출
        val result = openAIAdapter.analyzeWithScript(scriptContent, participants)

        // 프론트엔드 인터페이스에 맞는 응답 구조
        return ResponseEntity.ok(mapOf(
            "chemistry" to mapOf(
                "score" to result.chemistry.score,
                "message" to result.chemistry.message
            ),
            "communicationTips" to result.communicationTips,
            "summary" to mapOf(
                "description" to result.summary.description,
                "analysis" to result.summary.analysis
            ),
            "aiAdvice" to result.aiAdvice
        ))
    }

    /**
     * 스크립트 내용 확인
     */
    @GetMapping("/script")
    fun getScript(): ResponseEntity<Any> {
        val scriptFile = File("docs/script.txt")
        return if (scriptFile.exists()) {
            ResponseEntity.ok(mapOf("script" to scriptFile.readText()))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
