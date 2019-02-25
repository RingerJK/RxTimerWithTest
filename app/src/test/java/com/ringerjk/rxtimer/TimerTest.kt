package com.ringerjk.rxtimer

import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimerTest {

    private lateinit var timer: Timer
    private val testScheduler = TestScheduler()

    @Before
    fun setup() {
        timer = TimerImpl()

        RxJavaPlugins.reset()

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }

    @After
    fun tearDown() = RxJavaPlugins.reset()

    private fun advancedByTime(seconds: Long) {
        testScheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }

    private fun provideTimerTestObservable(): TestObserver<Long> = timer.timer.test()

    @Test
    fun start() {
        val testObs = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)

        testObs.assertValues(0, 1, 2, 3, 4, 5)
        testObs.assertNotTerminated()
    }

    @Test
    fun startPause() {
        val testObs = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)
        timer.pause()
        advancedByTime(5)

        testObs.assertValues(0, 1, 2, 3, 4, 5)
        testObs.assertNotTerminated()
    }

    @Test
    fun startPauseResume() {
        val testObs = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)
        timer.pause()
        advancedByTime(5)
        timer.resume()
        advancedByTime(2)

        testObs.assertValues(0, 1, 2, 3, 4, 5, 5, 6, 7)
        testObs.assertNotTerminated()
    }

    @Test
    fun startPauseResumeStartPauseResume() {
        val testObs = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)
        timer.pause()
        advancedByTime(5)
        timer.resume()
        advancedByTime(2)
        timer.start()
        advancedByTime(2)
        timer.pause()
        advancedByTime(2)
        timer.resume()
        advancedByTime(2)

        testObs.assertValues(0, 1, 2, 3, 4, 5, 5, 6, 7, 0, 1, 2, 2, 3, 4)
        testObs.assertNotTerminated()
    }

    @Test
    fun startStop() {
        val testObs = provideTimerTestObservable()
        timer.start()
        advancedByTime(5)
        timer.stop()
        advancedByTime(2)

        testObs.assertValues(0, 1, 2, 3, 4, 5)
        testObs.assertNotTerminated()
    }

    @Test
    fun startStopResume() {
        val testObs = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)
        timer.stop()
        advancedByTime(2)
        timer.resume()
        advancedByTime(3)

        testObs.assertValues(0, 1, 2, 3, 4, 5, 0, 1, 2, 3)
        testObs.assertNotTerminated()
    }

    @Test
    fun neverStartOrResume(){
        val testObs = provideTimerTestObservable()

        timer.pause()
        advancedByTime(5)
        timer.stop()
        advancedByTime(5)
        timer.pause()
        advancedByTime(5)

        testObs.assertNoValues()
        testObs.assertNotTerminated()
    }

    @Test
    fun fewObservables() {
        val testObs1 = provideTimerTestObservable()
        val testObs2 = provideTimerTestObservable()

        timer.start()
        advancedByTime(5)
        timer.pause()
        advancedByTime(5)

        testObs1.assertValues(0, 1, 2, 3, 4, 5)
        testObs2.assertValues(0, 1, 2, 3, 4, 5)

        val testObs3 = provideTimerTestObservable()

        timer.start()
        advancedByTime(2)

        testObs1.assertValues(0, 1, 2, 3, 4, 5, 0, 1, 2)
        testObs2.assertValues(0, 1, 2, 3, 4, 5, 0, 1, 2)
        testObs3.assertValues(0, 1, 2)

        testObs1.assertNotTerminated()
        testObs2.assertNotTerminated()
        testObs3.assertNotTerminated()
    }
}