package com.example.bettertalk.adapter.web.dto

import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingRelationship
import com.example.bettertalk.domain.user.User
import java.time.format.DateTimeFormatter

/**
 * 회의 목록 응답 (사이드바용)
 */
data class MeetingListResponse(
    val id: Long,
    val title: String,
    val date: String,
    val summary: String?
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        fun from(meeting: Meeting): MeetingListResponse {
            return MeetingListResponse(
                id = meeting.id,
                title = meeting.title,
                date = meeting.createdAt.format(DATE_FORMATTER),
                summary = meeting.summary
            )
        }
    }
}

/**
 * 회의 상세 응답
 */
data class MeetingDetailResponse(
    val id: Long,
    val title: String,
    val date: String,
    val status: String,
    val summary: String?,
    val overviewImageUrl: String?,
    val participants: List<ParticipantResponse>
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        fun from(meeting: Meeting): MeetingDetailResponse {
            return MeetingDetailResponse(
                id = meeting.id,
                title = meeting.title,
                date = meeting.createdAt.format(DATE_FORMATTER),
                status = meeting.status.name,
                summary = meeting.summary,
                overviewImageUrl = meeting.overviewImageUrl,
                participants = meeting.participants.map { ParticipantResponse.from(it.user) }
            )
        }
    }
}

/**
 * 참가자 정보 응답
 */
data class ParticipantResponse(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val arrcType: String,
    val characterBadgeLabel: String?
) {
    companion object {
        fun from(user: User): ParticipantResponse {
            return ParticipantResponse(
                id = user.id,
                nickname = user.nickname,
                profileImageUrl = user.profileImageUrl,
                arrcType = user.arrcType,
                characterBadgeLabel = user.characterBadgeLabel
            )
        }
    }
}

/**
 * 케미 분석 응답 (관계 분석)
 */
data class RelationshipResponse(
    val toUser: ParticipantResponse,
    val chemistryScore: Int,
    val analysisResult: AnalysisResultResponse
) {
    companion object {
        fun from(relationship: MeetingRelationship): RelationshipResponse {
            return RelationshipResponse(
                toUser = ParticipantResponse.from(relationship.toUser),
                chemistryScore = relationship.chemistryScore,
                analysisResult = AnalysisResultResponse.fromJson(relationship.analysisResultJson)
            )
        }
    }
}

/**
 * 분석 결과 응답 (JSON 파싱)
 */
data class AnalysisResultResponse(
    val chemistryLabel: String?,
    val traitSliders: List<TraitSliderResponse>,
    val communicationGuide: List<String>,
    val coachingCards: List<CoachingCardResponse>,
    val aiAdvice: String? = null
) {
    companion object {
        fun fromJson(json: String?): AnalysisResultResponse {
            if (json.isNullOrBlank()) {
                return AnalysisResultResponse(
                    chemistryLabel = null,
                    traitSliders = emptyList(),
                    communicationGuide = emptyList(),
                    coachingCards = emptyList(),
                    aiAdvice = null
                )
            }

            // JSON 파싱 (Jackson ObjectMapper 사용)
            return try {
                val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                mapper.readValue(json, AnalysisResultResponse::class.java)
            } catch (e: Exception) {
                AnalysisResultResponse(
                    chemistryLabel = null,
                    traitSliders = emptyList(),
                    communicationGuide = emptyList(),
                    coachingCards = emptyList(),
                    aiAdvice = null
                )
            }
        }
    }
}

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
