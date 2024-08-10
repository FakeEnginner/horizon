package com.example.horizon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horizon.Interface.dao.DiaryDao
import com.example.horizon.model.entities.diary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiaryRepository(private val diaryDao: DiaryDao) : ViewModel() {

    val allDiaries: LiveData<List<diary>> = diaryDao.getAllDiaries()

    fun insert(diary: diary) {
        viewModelScope.launch(Dispatchers.IO) {
            diaryDao.insertDiary(diary)
        }
    }

    fun delete(diary: diary) {
        viewModelScope.launch(Dispatchers.IO) {
            diaryDao.deleteDiary(diary)
        }
    }
    fun getDiaryById(id: Int): LiveData<diary> {
        return diaryDao.getDiaryById(id)
    }
    fun searchDiaries(query: String): LiveData<List<diary>> {
        return diaryDao.searchDiaries("%$query%")
    }
}