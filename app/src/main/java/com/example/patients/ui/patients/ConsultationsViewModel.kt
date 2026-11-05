package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConsultationsViewModel(
    private val repository: PatientRepository,
    private val patientId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ConsultationsUiState>(ConsultationsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadConsultations()
    }

    fun loadConsultations() {
        _uiState.value = ConsultationsUiState.Loading
        viewModelScope.launch {
            repository.getConsultations(patientId)
                .onSuccess { consultations ->
                    _uiState.value = ConsultationsUiState.Success(consultations)
                }
                .onFailure { e ->
                    _uiState.value = ConsultationsUiState.Error(e.message ?: "Erreur inconnue")
                }
        }
    }

    fun addConsultation(diagnostic: String, traitement: String, medecin: String) {
        viewModelScope.launch {
            val request = ConsultationRequest(
                patientId = patientId,
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                diagnostic = diagnostic,
                traitement = traitement,
                medecin = medecin
            )
            repository.createConsultation(request)
                .onSuccess { consultation ->
                    _uiState.update { state ->
                        if (state is ConsultationsUiState.Success) {
                            state.copy(consultations = listOf(consultation) + state.consultations)
                        } else state
                    }
                    _messages.emit("Consultation ajoutée")
                }
                .onFailure { e ->
                    _messages.emit(e.message ?: "Erreur lors de l'ajout")
                }
        }
    }

    companion object {
        fun provideFactory(
            repository: PatientRepository,
            patientId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConsultationsViewModel(repository, patientId) as T
            }
        }
    }
}

sealed interface ConsultationsUiState {
    data object Loading : ConsultationsUiState
    data class Success(val consultations: List<Consultation>) : ConsultationsUiState
    data class Error(val message: String) : ConsultationsUiState
}
