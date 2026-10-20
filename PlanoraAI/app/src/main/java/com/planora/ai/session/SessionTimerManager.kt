package com.planora.ai.session

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Exercise session timer + attention heuristic.
 *
 * Tracks ONLY app process foreground/background (not whole-phone surveillance).
 * Short interrupts (call, music app) = process goes background → counted as interrupt.
 * Continuous long foreground streak without leaving → lean "on phone".
 * Mostly background → lean "probably training".
 * User always confirms Yes/No at the end.
 */
data class ActiveSession(
    val programId: Long,
    val sessionId: Long,
    val title: String,
    val durationMin: Int,
    val startedAt: Long,
    val endsAt: Long,
    var visibleMs: Long = 0,
    var hiddenMs: Long = 0,
    var interruptCount: Int = 0,
    var longForegroundStreakMs: Long = 0,
    var lastChange: Long = System.currentTimeMillis(),
    var isForeground: Boolean = true,
    var fgStreakStart: Long? = System.currentTimeMillis(),
    var completed: Boolean = false
)

data class SessionAnalysis(
    val verdict: String, // trained | phone | unknown
    val message: String,
    val programId: Long,
    val sessionId: Long,
    val title: String,
    val durationMin: Int
)

object SessionTimerManager : DefaultLifecycleObserver {
    private val _active = MutableStateFlow<ActiveSession?>(null)
    val active: StateFlow<ActiveSession?> = _active.asStateFlow()

    private val _ended = MutableStateFlow<SessionAnalysis?>(null)
    val endedAnalysis: StateFlow<SessionAnalysis?> = _ended.asStateFlow()

    private var registered = false

    fun init(app: Application) {
        if (!registered) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            registered = true
        }
    }

    fun start(programId: Long, sessionId: Long, title: String, durationMin: Int) {
        val now = System.currentTimeMillis()
        val ms = durationMin.coerceAtLeast(1) * 60_000L
        _ended.value = null
        _active.value = ActiveSession(
            programId = programId,
            sessionId = sessionId,
            title = title,
            durationMin = durationMin,
            startedAt = now,
            endsAt = now + ms
        )
    }

    fun remainingMs(): Long {
        val a = _active.value ?: return 0
        return (a.endsAt - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun tickCheckEnd(): Boolean {
        val a = _active.value ?: return false
        if (a.completed) return false
        if (System.currentTimeMillis() >= a.endsAt) {
            finish()
            return true
        }
        return false
    }

    override fun onStart(owner: LifecycleOwner) {
        onVisibility(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        onVisibility(false)
    }

    private fun onVisibility(foreground: Boolean) {
        val a = _active.value ?: return
        if (a.completed) return
        val now = System.currentTimeMillis()
        val elapsed = now - a.lastChange
        if (a.isForeground) {
            a.visibleMs += elapsed
            if (a.fgStreakStart == null) a.fgStreakStart = a.lastChange
        } else {
            a.hiddenMs += elapsed
            a.interruptCount += 1
            a.fgStreakStart?.let {
                a.longForegroundStreakMs =
                    maxOf(a.longForegroundStreakMs, a.lastChange - it)
            }
            a.fgStreakStart = null
        }
        a.isForeground = foreground
        a.lastChange = now
        if (foreground && a.fgStreakStart == null) a.fgStreakStart = now
        _active.value = a.copy()
    }

    private fun flush() {
        val a = _active.value ?: return
        val now = System.currentTimeMillis()
        val elapsed = now - a.lastChange
        if (a.isForeground) a.visibleMs += elapsed else a.hiddenMs += elapsed
        a.fgStreakStart?.let {
            a.longForegroundStreakMs = maxOf(a.longForegroundStreakMs, now - it)
        }
        a.lastChange = now
    }

    fun finish(): SessionAnalysis? {
        val a = _active.value ?: return null
        if (a.completed) return _ended.value
        flush()
        a.completed = true
        val total = (a.visibleMs + a.hiddenMs).coerceAtLeast(1)
        val visibleRatio = a.visibleMs.toDouble() / total
        val hiddenRatio = a.hiddenMs.toDouble() / total
        val longFgRatio = a.longForegroundStreakMs.toDouble() / (a.durationMin * 60_000.0).coerceAtLeast(1.0)

        val (verdict, message) = when {
            hiddenRatio >= 0.4 || a.interruptCount >= 2 ->
                "trained" to
                        "به نظر می‌رسد بیشتر وقت بیرون از اپ بودید (تمرین، موسیقی یا تماس کوتاه). آفرین اگر انجام دادید — مرحله بعد؟"
            visibleRatio >= 0.75 && longFgRatio >= 0.6 ->
                "phone" to
                        "به نظر می‌رسد اپ بیشتر جلسه روی صفحه بوده. اگر تمرین کردید عالی؛ اگر نه، الان وقت تمرین است."
            else ->
                "unknown" to
                        "زمان جلسه تمام شد. آیا این تمرین را انجام دادید؟ (تماس و موسیقی کوتاه مشکلی نیست.)"
        }

        val analysis = SessionAnalysis(
            verdict = verdict,
            message = message,
            programId = a.programId,
            sessionId = a.sessionId,
            title = a.title,
            durationMin = a.durationMin
        )
        _ended.value = analysis
        _active.value = a
        return analysis
    }

    fun clear() {
        _active.value = null
        _ended.value = null
    }

    fun dismissAnalysis() {
        _ended.value = null
        _active.value = null
    }
}
