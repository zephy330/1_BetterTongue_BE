package com.example.bettertalk.adapter.web.dto

import org.springframework.web.multipart.MultipartFile

/**
 * 회의 업로드 요청
 */
data class MeetingUploadRequest(
    val title: String,
    val file: MultipartFile
)
