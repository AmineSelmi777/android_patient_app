package com.example.patients

import android.app.Application
import com.example.patients.di.AppContainer

class PatientsApplication : Application() {
    // Créé une seule fois, partagé par toute l'application
    val container by lazy { AppContainer() }
}