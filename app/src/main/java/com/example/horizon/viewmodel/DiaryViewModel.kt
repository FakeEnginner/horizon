package com.example.horizon.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.horizon.model.Database.DiaryDatabase
import com.example.horizon.model.entities.diary
import com.example.horizon.repository.DiaryRepository
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DiaryRepository
    val allDiaries: LiveData<List<diary>>

    init {
        val diaryDao = DiaryDatabase.getDairyDatabase(application).diaryDao()
        repository = DiaryRepository(diaryDao)
        allDiaries = repository.allDiaries
    }

    fun insert(diary: diary) = viewModelScope.launch {
        repository.insert(diary)
    }

    fun delete(diary: diary) = viewModelScope.launch {
        repository.delete(diary)
    }
    fun getDiaryById(id: Int): LiveData<diary> {
        return repository.getDiaryById(id)
    }
    fun searchDiaries(query: String): LiveData<List<diary>> {
        return repository.searchDiaries(query)
    }
}
