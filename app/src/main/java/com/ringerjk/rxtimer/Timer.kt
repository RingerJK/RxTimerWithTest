package com.ringerjk.rxtimer

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class TimerImpl : Timer {

    private val timerStateSubject = BehaviorSubject.create<TimerState>()

    private val currentValue = AtomicLong(0)
    private val offset = AtomicLong(0)

    private enum class TimerState {
        Start,
        Stop,
        Resume,
        Pause;
    }

    override fun start() = timerStateSubject.onNext(TimerState.Start)

    override fun stop() = timerStateSubject.onNext(TimerState.Stop)

    override fun resume() = timerStateSubject.onNext(TimerState.Resume)

    override fun pause() = timerStateSubject.onNext(TimerState.Pause)

    override val timer: Observable<Long> = timerStateSubject.switchMap { state ->
        when (state) {
            TimerState.Start -> Observable.interval(0, 1, TimeUnit.SECONDS)
                .doOnSubscribe {
                    currentValue.set(0)
                    offset.set(0)
                }
                .doOnNext {
                    currentValue.set(it)
                }
            TimerState.Stop -> {
                currentValue.set(0)
                offset.set(0)
                Observable.empty()
            }
            TimerState.Resume -> Observable.interval(0, 1, TimeUnit.SECONDS)
                .map { offset.get() + it }
                .doOnNext { currentValue.set(it) }

            TimerState.Pause -> {
                offset.set(currentValue.get())
                currentValue.set(0)
                Observable.empty()
            }
        }
    }
        .share()
}

interface Timer {
    fun start()

    fun stop()

    fun resume()

    fun pause()

    val timer: Observable<Long>
}