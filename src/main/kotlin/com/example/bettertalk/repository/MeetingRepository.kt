package com.example.bettertalk.repository

import com.example.bettertalk.domain.meeting.Meeting
import com.example.bettertalk.domain.meeting.MeetingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MeetingRepository : JpaRepository<Meeting, Long> {

    /**
     * 사용자가 참여한 회의 목록을 최신순으로 조회
     */
    @Query("""
        SELECT DISTINCT m FROM Meeting m
        JOIN m.participants p
        WHERE p.user.id = :userId
        ORDER BY m.createdAt DESC
    """)
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Meeting>

    /**
     * 사용자가 참여한 회의 중 특정 상태의 회의 목록 조회
     */
    @Query("""
        SELECT DISTINCT m FROM Meeting m
        JOIN m.participants p
        WHERE p.user.id = :userId AND m.status = :status
        ORDER BY m.createdAt DESC
    """)
    fun findAllByUserIdAndStatusOrderByCreatedAtDesc(userId: Long, status: MeetingStatus): List<Meeting>
}
