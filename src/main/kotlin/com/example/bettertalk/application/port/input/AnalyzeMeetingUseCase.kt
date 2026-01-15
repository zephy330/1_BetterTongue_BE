package com.example.bettertalk.application.port.input

/**
 * Input Port: 회의 분석 (Command - Async)
 */
interface AnalyzeMeetingUseCase {

    fun analyzeMeeting(meetingId: Long)
}
