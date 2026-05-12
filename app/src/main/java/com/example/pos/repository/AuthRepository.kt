package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    /*
     * Mengambil client Supabase yang sudah dibuat sebelumnya.
     */
    private val supabase = SupabaseClientProvider.client

    /*
     * Flow untuk memantau perubahan status session.
     */
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    /*
     * Register user baru.
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ) {

        supabase.auth.signUpWith(Email) {

            this.email = email
            this.password = password

            /*
             * Mengirim full_name ke metadata Supabase.
             */
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    /*
     * Login user.
     */
    suspend fun login(
        email: String,
        password: String
    ) {

        supabase.auth.signInWith(Email) {

            this.email = email
            this.password = password
        }
    }

    /*
     * Logout user.
     */
    suspend fun logout() {
        supabase.auth.signOut()
    }

    /*
     * Mengecek apakah user sudah login.
     */
    suspend fun isLoggedIn(): Boolean {

        try {
            supabase.auth.awaitInitialization()
        } catch (e: Exception) {
        }

        return supabase.auth.currentSessionOrNull() != null
    }

    /*
     * Menunggu auth selesai inisialisasi.
     */
    suspend fun awaitAuthInitialization() {
        supabase.auth.awaitInitialization()
    }
}