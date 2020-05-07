package ua.com.cuteteam.cutetaxi.viewmodels.viewmodelsfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.com.cuteteam.cutetaxi.repositories.StartUpRepository
import ua.com.cuteteam.cutetaxi.viewmodels.StartUpViewModel

@Suppress("UNCHECKED_CAST")
class StartUpViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StartUpViewModel(StartUpRepository()) as T
    }
}