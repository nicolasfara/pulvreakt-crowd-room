package it.nicolasfarabegoli.pulverization.crowd.room

import it.nicolasfarabegoli.pulverization.crowd.roomRuntimeSetup
import it.nicolasfarabegoli.pulverization.runtime.PulverizationRuntime
import kotlinx.coroutines.delay

suspend fun roomProgram(deviceId: String, host: String) {
    val runtimeConfig = roomRuntimeSetup()
    val runtime = PulverizationRuntime(deviceId, host, runtimeConfig)

    runtime.start()
    while (true) { delay(1) }
}
