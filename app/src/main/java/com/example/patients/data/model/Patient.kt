package com.example.patients.data.model


import kotlinx.serialization.Serializable

// Patient tel que renvoyé par l'API (GET)
@Serializable
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

// Données envoyées pour créer ou modifier un patient (POST / PUT) : pas d'id
@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null
)

// Corps des réponses d'erreur de l'API : { "message": "...", "erreurs": [...] }
@Serializable
data class ApiError(
    val message: String,
    val erreurs: List<String> = emptyList()
)