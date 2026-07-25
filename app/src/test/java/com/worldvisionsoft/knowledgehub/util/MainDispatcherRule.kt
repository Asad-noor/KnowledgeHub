package com.worldvisionsoft.knowledgehub.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [Dispatchers.Main] for a [TestDispatcher] for the duration of a test.
 *
 * `viewModelScope` runs on the main dispatcher, which does not exist on a plain JVM
 * unit test — without this rule every ViewModel test would fail with
 * "Module with the Main dispatcher had failed to initialize".
 *
 * The default [StandardTestDispatcher] also gives us a virtual clock, so tests can
 * skip the ViewModel's debounce with `advanceTimeBy` instead of really sleeping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
