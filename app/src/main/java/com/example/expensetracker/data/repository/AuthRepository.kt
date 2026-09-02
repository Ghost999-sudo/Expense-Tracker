package com.example.expensetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.expensetracker.data.local.UserDao
import com.example.expensetracker.data.local.UserEntity
import com.example.expensetracker.util.PasswordHasher

class AuthRepository(
    private val userDao: UserDao,
    context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "auth_prefs",
            Context.MODE_PRIVATE
        )

    private val sessionKey = "current_user_id"

    fun getSessionUserId(): Long? {

        val id = prefs.getLong(sessionKey, -1L)

        return if (id >= 0) id else null
    }

    private fun setSessionUserId(id: Long) {

        prefs.edit()
            .putLong(sessionKey, id)
            .apply()
    }

    fun clearSession() {

        prefs.edit()
            .remove(sessionKey)
            .apply()
    }

    suspend fun register(
        email: String,
        password: String
    ): Result<UserEntity> {

        val normalizedEmail = email.trim().lowercase()

        if (userDao.findByEmail(normalizedEmail) != null) {
            return Result.failure(
                IllegalArgumentException(
                    "An account with this email already exists."
                )
            )
        }

        val salt = PasswordHasher.generateSalt()
        val passwordHash = PasswordHasher.hash(password, salt)

        val user = UserEntity(
            email = normalizedEmail,
            passwordHash = passwordHash,
            salt = salt
        )

        val id = userDao.insert(user)
        val created = user.copy(id = id)

        setSessionUserId(id)

        return Result.success(created)
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserEntity> {

        val user = userDao.findByEmail(
            email.trim().lowercase()
        ) ?: return Result.failure(
            IllegalArgumentException(
                "No account found for this email."
            )
        )

        if (!PasswordHasher.verify(
                password,
                user.salt,
                user.passwordHash
            )
        ) {
            return Result.failure(
                IllegalArgumentException(
                    "Incorrect password."
                )
            )
        }

        setSessionUserId(user.id)

        return Result.success(user)
    }

    suspend fun getCurrentUser(): UserEntity? {

        val id = getSessionUserId() ?: return null

        return userDao.findById(id)
    }

    fun logout() {

        clearSession()
    }
}