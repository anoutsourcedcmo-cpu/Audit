package com.example.data

import kotlinx.coroutines.flow.Flow

class AuditRepository(private val auditDao: AuditDao) {

    val profileFlow: Flow<ProfileEntity?> = auditDao.getProfileFlow()

    suspend fun getProfile(): ProfileEntity? = auditDao.getProfile()

    suspend fun saveProfile(profile: ProfileEntity) {
        auditDao.saveProfile(profile)
    }

    val allAnswersFlow: Flow<List<AuditAnswerEntity>> = auditDao.getAllAnswersFlow()

    suspend fun getAllAnswers(): List<AuditAnswerEntity> = auditDao.getAllAnswers()

    suspend fun saveAnswer(questionId: String, answerText: String) {
        auditDao.saveAnswer(AuditAnswerEntity(questionId, answerText))
    }

    suspend fun clearAll() {
        auditDao.clearAllAnswers()
        auditDao.saveProfile(ProfileEntity())
    }
}
