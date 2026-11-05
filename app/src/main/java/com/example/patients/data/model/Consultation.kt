package com.example.patients.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Consultation(
    val id: String,
    val date: String,
    @SerialName("motif")
    val diagnostic: String,
    val traitement: String,
    @SerialName("patient")
    val patientId: String
)

@Serializable
data class ConsultationRequest(
    @SerialName("patient")
    val patientId: String,
    val date: String,
    @SerialName("motif")
    val diagnostic: String,
    val traitement: String,
    val medecin: String,
    val notes: String? = null,
    val statut: String = "En attente"
)
