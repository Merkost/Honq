package com.merkost.honq.data.remote.api

import com.merkost.honq.data.remote.dto.QuestionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class QuestionApi(
    private val client: SupabaseClient
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> =
        client.postgrest["questions"]
            .select()
            .decodeList()
}
