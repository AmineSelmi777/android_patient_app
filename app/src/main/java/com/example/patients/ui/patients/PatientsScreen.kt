package com.example.patients.ui.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory)
) {
    var selectedPatientId by remember { mutableStateOf<String?>(null) }

    if (selectedPatientId != null) {
        ConsultationsScreen(
            patientId = selectedPatientId!!,
            onBack = { selectedPatientId = null }
        )
    } else {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val filterStatut by viewModel.filterStatut.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        var patientToEdit by remember { mutableStateOf<Patient?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestion des Patients") },
                    actions = {
                        IconButton(onClick = { viewModel.loadPatients() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null to "Tous", "Actif" to "Actif", "Inactif" to "Inactif").forEach { (valeur, label) ->
                        FilterChip(
                            selected = filterStatut == valeur,
                            onClick = { viewModel.setFilter(valeur) },
                            label = { Text(label) }
                        )
                    }
                }

                PullToRefreshBox(
                    isRefreshing = uiState is PatientsUiState.Loading && (uiState as? PatientsUiState.Success)?.patients?.isNotEmpty() == true,
                    onRefresh = { viewModel.loadPatients() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = uiState) {
                        is PatientsUiState.Loading -> if ((uiState as? PatientsUiState.Success)?.patients?.isEmpty() != false) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is PatientsUiState.Error ->
                            ErrorContent(
                                message = state.message,
                                onRetry = { viewModel.loadPatients() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        is PatientsUiState.Success ->
                            PatientList(
                                patients = state.patients,
                                onDelete = { viewModel.deletePatient(it) },
                                onEdit = { patientToEdit = it },
                                onConsultations = { selectedPatientId = it.id }
                            )
                    }
                }
            }
        }

        if (showAddDialog || patientToEdit != null) {
            PatientDialog(
                patient = patientToEdit,
                onDismiss = {
                    showAddDialog = false
                    patientToEdit = null
                },
                onConfirm = { request ->
                    if (patientToEdit == null) viewModel.addPatient(request)
                    else viewModel.updatePatient(patientToEdit!!.id, request)
                    showAddDialog = false
                    patientToEdit = null
                }
            )
        }
    }
}

@Composable
fun PatientList(
    patients: List<Patient>,
    onDelete: (Patient) -> Unit,
    onEdit: (Patient) -> Unit,
    onConsultations: (Patient) -> Unit
) {
    if (patients.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun patient trouvé")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(patients, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onDelete = { onDelete(patient) },
                    onEdit = { onEdit(patient) },
                    onClick = { onConsultations(patient) }
                )
            }
        }
    }
}

@Composable
fun PatientCard(patient: Patient, onDelete: () -> Unit, onEdit: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${patient.prenom} ${patient.nom}", style = MaterialTheme.typography.titleMedium)
                Text(text = "${patient.age ?: "?"} ans · ${patient.statut}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Modifier")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun PatientDialog(
    patient: Patient? = null,
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit
) {
    var nom by remember { mutableStateOf(patient?.nom ?: "") }
    var prenom by remember { mutableStateOf(patient?.prenom ?: "") }
    var age by remember { mutableStateOf(patient?.age?.toString() ?: "") }
    var medecin by remember { mutableStateOf(patient?.medecin ?: "") }

    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Nouveau patient" else "Modifier le patient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom *") }, singleLine = true)
                OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom *") }, singleLine = true)
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = medecin, onValueChange = { medecin = it }, label = { Text("Médecin") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(PatientRequest(nom.trim(), prenom.trim(), age.toIntOrNull(), medecin.trim().ifBlank { null }))
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}