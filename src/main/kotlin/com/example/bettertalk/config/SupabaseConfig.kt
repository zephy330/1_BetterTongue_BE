package com.example.bettertalk.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SupabaseConfig {

    @Value("\${supabase.url}")
    lateinit var supabaseUrl: String

    @Value("\${supabase.key}")
    lateinit var supabaseKey: String

    @Bean
    fun supabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
