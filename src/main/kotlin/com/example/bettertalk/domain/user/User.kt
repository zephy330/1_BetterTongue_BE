package com.example.bettertalk.domain.user

import com.example.bettertalk.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Column(nullable = false, length = 50)
    val nickname: String,

    @Column(length = 500)
    val profileImageUrl: String? = null,

    @Column(nullable = false, length = 50)
    val arrcType: String,

    @Column(length = 50)
    val characterBadgeLabel: String? = null,

    @Column(length = 200)
    val strengthTags: String? = null,

    @Column(length = 200)
    val weaknessTags: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) : BaseTimeEntity()
