package it.nicolasfarabegoli.pulverization.crowd.wearable

import it.nicolasfarabegoli.pulverization.crowd.wearableRuntimeSetup
import it.nicolasfarabegoli.pulverization.runtime.PulverizationRuntime
import kotlinx.coroutines.delay

suspend fun wearableProgram(deviceId: String, host: String) {
    val runtimeConfig = wearableRuntimeSetup()
    val runtime = PulverizationRuntime(deviceId, host, runtimeConfig)

    runtime.start()
    while (true) { delay(1) }
}
