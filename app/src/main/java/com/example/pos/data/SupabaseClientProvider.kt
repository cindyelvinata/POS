package com.example.pos.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = "https://utyafmyuwlzitnyisxhm.supabase.co",
        supabaseKey = "sb_publishable_ZH0KbCRiItYd2TL_2fI6jw_r5IFNfQT"
    ) {

        install(Auth)

        install(Postgrest)
    }
}