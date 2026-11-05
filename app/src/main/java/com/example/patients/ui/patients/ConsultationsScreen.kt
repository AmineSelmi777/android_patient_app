package com.example.patients.ui.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Consultation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationsScreen(
    patientId: String,
    onBack: () -> Unit,
    app: PatientsApplication = LocalContext.current.applicationContext as PatientsApplication
) {
    val viewModel: ConsultationsViewModel = viewModel(
        factory = ConsultationsViewModel.provideFactory(app.container.patientRepository, patientId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    // Pour afficher les messages de succès/erreur
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter consultation")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is ConsultationsUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is ConsultationsUiState.Error ->
                    Text(state.message, Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)

                is ConsultationsUiState.Success -> {
                    if (state.consultations.isEmpty()) {
                        Text("Aucune consultation", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.consultations) { consultation ->
                                ConsultationCard(consultation)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddConsultationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { diag, trait, med ->
                viewModel.addConsultation(diag, trait, med)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ConsultationCard(consultation: Consultation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Date: ${consultation.date}", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            // Attention : on affiche le "diagnostic" (qui est mappé sur "motif" côté Node)
            Text(text = "Motif: ${consultation.diagnostic}", style = MaterialTheme.typography.titleSmall)
            Text(text = "Traitement: ${consultation.traitement}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AddConsultationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var diagnostic by remember { mutableStateOf("") }
    var traitement by remember { mutableStateOf("") }
    var medecin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Consultation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = diagnostic, onValueChange = { diagnostic = it },
                    label = { Text("Motif / Diagnostic *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = traitement, onValueChange = { traitement = it },
                    label = { Text("Traitement *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin *") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = diagnostic.isNotBlank() && traitement.isNotBlank() && medecin.isNotBlank(),
                onClick = { onConfirm(diagnostic, traitement, medecin) }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}