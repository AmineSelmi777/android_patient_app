package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        _uiState.value = PatientsUiState.Loading
        viewModelScope.launch {
            repository.getPatients()
                .onSuccess { patients -> _uiState.value = PatientsUiState.Success(patients) }
                .onFailure { e -> _uiState.value = PatientsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                PatientViewModel(app.container.patientRepository)
            }
        }
    }
}