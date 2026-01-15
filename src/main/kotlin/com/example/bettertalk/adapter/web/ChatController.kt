package com.example.bettertalk.adapter.web

import com.example.bettertalk.adapter.openai.OpenAIChatAdapter
import com.example.bettertalk.adapter.persistence.ChatMessageRepository
import com.example.bettertalk.repository.UserRepository
import com.example.bettertalk.domain.chat.ChatMessage as ChatMessageEntity
import com.example.bettertalk.domain.chat.ChatRole
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatAdapter: OpenAIChatAdapter,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository
) {

    /**
     * 챗봇 대화 API (DB 저장 포함)
     * POST /api/chat
     */
    @PostMapping
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val userId = request.userId ?: 1L  // 기본값: 루시
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ChatResponse("사용자를 찾을 수 없습니다.", "시스템"))

        // DB에서 최근 대화 히스토리 로드
        val dbHistory = chatMessageRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
            .reversed()
            .map { ChatMessage(
                role = if (it.role == ChatRole.USER) "user" else "assistant",
                content = it.content
            )}

        // GPT 호출
        val response = chatAdapter.chat(
            userMessage = request.message,
            conversationHistory = dbHistory,
            meetingContext = request.meetingContext
        )

        // 사용자 메시지 저장
        chatMessageRepository.save(ChatMessageEntity(
            user = user,
            meeting = null,
            role = ChatRole.USER,
            content = request.message
        ))

        // AI 응답 저장
        chatMessageRepository.save(ChatMessageEntity(
            user = user,
            meeting = null,
            role = ChatRole.ASSISTANT,
            content = response
        ))

        return ResponseEntity.ok(ChatResponse(
            message = response,
            sender = "카나"
        ))
    }

    /**
     * 스트리밍 챗봇 대화 API (SSE)
     * POST /api/chat/stream
     */
    @PostMapping("/stream", produces = ["text/plain;charset=UTF-8"])
    fun chatStream(@RequestBody request: ChatRequest): Flux<String> {
        val userId = request.userId ?: 1L
        val user = userRepository.findById(userId).orElse(null)
            ?: return Flux.just("data: 사용자를 찾을 수 없습니다.\n\n")

        // DB에서 최근 대화 히스토리 로드
        val dbHistory = chatMessageRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
            .reversed()
            .map { ChatMessage(
                role = if (it.role == ChatRole.USER) "user" else "assistant",
                content = it.content
            )}

        // 사용자 메시지 먼저 저장
        chatMessageRepository.save(ChatMessageEntity(
            user = user,
            meeting = null,
            role = ChatRole.USER,
            content = request.message
        ))

        // 응답을 모아서 저장하기 위한 StringBuilder
        val fullResponse = StringBuilder()

        return chatAdapter.chatStream(
            userMessage = request.message,
            conversationHistory = dbHistory,
            meetingContext = request.meetingContext
        )
        .doOnNext { chunk -> fullResponse.append(chunk) }
        .concatWith(Flux.just("[DONE]"))
        .doOnComplete {
            // AI 응답 저장
            if (fullResponse.isNotEmpty()) {
                chatMessageRepository.save(ChatMessageEntity(
                    user = user,
                    meeting = null,
                    role = ChatRole.ASSISTANT,
                    content = fullResponse.toString()
                ))
            }
        }
    }

    /**
     * 대화 히스토리 조회
     * GET /api/chat/history?userId=1
     */
    @GetMapping("/history")
    fun getHistory(@RequestParam userId: Long?): ResponseEntity<List<ChatHistoryItem>> {
        val uid = userId ?: 1L
        val messages = chatMessageRepository.findAllByUserIdOrderByCreatedAtAsc(uid)

        val history = messages.map { msg ->
            ChatHistoryItem(
                id = msg.id,
                role = if (msg.role == ChatRole.USER) "user" else "assistant",
                content = msg.content,
                timestamp = msg.createdAt.toString()
            )
        }

        return ResponseEntity.ok(history)
    }

    /**
     * 대화 히스토리 삭제 (새 대화 시작)
     * DELETE /api/chat/history?userId=1
     */
    @DeleteMapping("/history")
    fun clearHistory(@RequestParam userId: Long?): ResponseEntity<Map<String, String>> {
        val uid = userId ?: 1L
        val messages = chatMessageRepository.findAllByUserIdOrderByCreatedAtAsc(uid)
        chatMessageRepository.deleteAll(messages)

        return ResponseEntity.ok(mapOf("status" to "cleared"))
    }

    /**
     * 초기 인사말 API
     * GET /api/chat/greeting
     */
    @GetMapping("/greeting")
    fun getGreeting(): ResponseEntity<ChatResponse> {
        return ResponseEntity.ok(ChatResponse(
            message = "루시님, 오늘 회의에서 말을 많이 안했네요. 고민이 되는 점이 있었나요?",
            sender = "카나"
        ))
    }
}

data class ChatRequest(
    val message: String,
    val userId: Long? = null,
    val history: List<ChatMessage>? = null,
    val meetingContext: MeetingContext? = null
)

data class ChatMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)

data class MeetingContext(
    val summary: String? = null,
    val participants: List<String>? = null,
    val analysisResult: String? = null
)

data class ChatResponse(
    val message: String,
    val sender: String
)

data class ChatHistoryItem(
    val id: Long,
    val role: String,
    val content: String,
    val timestamp: String
)
