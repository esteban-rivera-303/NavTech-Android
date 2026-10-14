package com.zanaaustralis.argos

import androidx.lifecycle.ViewModel
import com.zanaaustralis.core.session.SessionManagerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class AppRootViewModel @Inject constructor(
    val sessionManager: SessionManagerImpl
) : ViewModel()