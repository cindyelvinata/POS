package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
 * Model untuk membaca data dari tabel profiles.
 * Kolom: id, full_name, role, created_at, updated_at
 */
@Serializable
data class UserProfile(
    val id: String = "",
    val role: String = "cashier",

    @SerialName("full_name")
    val fullName: String? = null,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = ""
)

class AuthRepository {

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
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    /*
     * Login user.
     */
    suspend fun login(email: String, password: String) {
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
     * Cek apakah user sudah login.
     */
    suspend fun isLoggedIn(): Boolean {
        try { supabase.auth.awaitInitialization() } catch (e: Exception) { }
        return supabase.auth.currentSessionOrNull() != null
    }

    /*
     * Tunggu auth selesai inisialisasi.
     */
    suspend fun awaitAuthInitialization() {
        supabase.auth.awaitInitialization()
    }

    /*
     * Ambil profil user yang sedang login dari tabel profiles.
     * Dipakai AuthViewModel untuk mendapatkan role (admin/cashier).
     * Mengembalikan null jika belum login atau profil tidak ditemukan.
     */
    suspend fun getProfile(): UserProfile? {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return null

            supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                    limit(1)
                }
                .decodeList<UserProfile>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}