package com.aistudio.unibuddy.qywvsp.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PomodoroState {
    private val _timeLeftSeconds = MutableStateFlow(0)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _isWorkMode = MutableStateFlow(true)
    val isWorkMode: StateFlow<Boolean> = _isWorkMode.asStateFlow()

    fun updateTime(seconds: Int) {
        _timeLeftSeconds.value = seconds
    }
    
    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }
    
    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }
    
    fun setWorkMode(work: Boolean) {
        _isWorkMode.value = work
    }
}
