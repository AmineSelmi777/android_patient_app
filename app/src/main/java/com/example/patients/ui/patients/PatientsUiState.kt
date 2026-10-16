package com.example.patients.ui.patients


import com.example.patients.data.model.Patient

// Les seuls états possibles de l'écran
sealed interface PatientsUiState {
    data object Loading : PatientsUiState
    data class Success(val patients: List<Patient>) : PatientsUiState
    data class Error(val message: String) : PatientsUiState
}