package com.dupat.demosecuritypatrol.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Corountines {
    fun main(work: suspend(() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }
}