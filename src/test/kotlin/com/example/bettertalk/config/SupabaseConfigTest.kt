package com.example.bettertalk.config

import io.github.jan.supabase.SupabaseClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SupabaseConfigTest {

    @Autowired
    lateinit var supabaseClient: SupabaseClient

    @Test
    fun `supabase client should be initialized`() {
        assertNotNull(supabaseClient)
    }
}
