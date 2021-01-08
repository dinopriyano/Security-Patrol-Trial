/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package com.dupat.demosecuritypatrol.facerecognition.env

import android.util.Log
import java.lang.Exception
import java.util.*

class Logger constructor(private val tag: String = DEFAULT_TAG, messagePrefix: String? = null) {
    companion object {
        private const val DEFAULT_TAG = "tensorflow"
        private const val DEFAULT_MIN_LOG_LEVEL = Log.DEBUG

        lateinit var IGNORED_CLASS_NAMES: MutableSet<String>
        private val callerSimpleName: String
            private get() {

                val stackTrace =
                    Thread.currentThread().stackTrace
                for (elem in stackTrace) {
                    val className = elem.className
                    if (!IGNORED_CLASS_NAMES!!.contains(
                            className
                        )
                    ) {
                        val classParts =
                            className.split("\\.").toTypedArray()
                        return classParts[classParts.size - 1]
                    }
                }
                return Logger::class.java.simpleName
            }

        init {
            IGNORED_CLASS_NAMES = HashSet(3)
            IGNORED_CLASS_NAMES.add("dalvik.system.VMStack")
            IGNORED_CLASS_NAMES.add("java.lang.Thread")
            IGNORED_CLASS_NAMES.add(Logger::class.java.canonicalName)
        }
    }

    private val messagePrefix: String
    private var minLogLevel =
        DEFAULT_MIN_LOG_LEVEL

    constructor(clazz: Class<*>) : this(clazz.simpleName) {}

    constructor(messagePrefix: String?) : this(DEFAULT_TAG, messagePrefix){}

    constructor(minLogLevel: Int) : this(DEFAULT_TAG,null) {
        this.minLogLevel = minLogLevel
    }

    fun setMinLogLevel(minLogLevel: Int) {
        this.minLogLevel = minLogLevel
    }

    fun isLoggable(logLevel: Int): Boolean {
        return logLevel >= minLogLevel || Log.isLoggable(tag, logLevel)
    }

    private fun toMessage(format: String, vararg args: Any): String {
        return try {
            var value: String = if (args.isNotEmpty()) String.format(format, args) else format
            Log.d("TAG", "isinya: $value")
            messagePrefix + value
        } catch (e : Exception){
            messagePrefix
        }
    }

    //The errors below have no effect on the application
    fun v(format: String, vararg args: Any?) {
        if (isLoggable(Log.VERBOSE)) {
            Log.v(tag, toMessage(format, args))
        }
    }

    fun v(t: Throwable?, format: String, vararg args: Any?) {
        if (isLoggable(Log.VERBOSE)) {
            Log.v(tag, toMessage(format, args), t)
        }
    }

    fun d(format: String, vararg args: Any?) {
        if (isLoggable(Log.DEBUG)) {
            Log.d(tag, toMessage(format, args))
        }
    }

    fun d(t: Throwable?, format: String, vararg args: Any?) {
        if (isLoggable(Log.DEBUG)) {
            Log.d(tag, toMessage(format, args), t)
        }
    }

    fun i(format: String, vararg args: Any?) {
        if (isLoggable(Log.INFO)) {
            Log.d("TAG", "args itu: $args")
            Log.i(tag, toMessage(format, args))
        }
    }

    fun i(t: Throwable?, format: String, vararg args: Any?) {
        if (isLoggable(Log.INFO)) {
            Log.i(tag, toMessage(format, args), t)
        }
    }

    fun w(format: String, vararg args: Any?) {
        if (isLoggable(Log.WARN)) {
            Log.w(tag, toMessage(format, args))
        }
    }

    fun w(t: Throwable?, format: String, vararg args: Any?) {
        if (isLoggable(Log.WARN)) {
            Log.w(tag, toMessage(format, args), t)
        }
    }

    fun e(format: String, vararg args: Any?) {
        if (isLoggable(Log.ERROR)) {
            Log.e(tag, toMessage(format, args))
        }
    }

    fun e(t: Throwable?, format: String, vararg args: Any?) {
        if (isLoggable(Log.ERROR)) {
            Log.e(tag, toMessage(format, args), t)
        }
    }

    init {
        val prefix =
            messagePrefix ?: callerSimpleName
        this.messagePrefix = if (prefix.length > 0) "$prefix: " else prefix
    }
}