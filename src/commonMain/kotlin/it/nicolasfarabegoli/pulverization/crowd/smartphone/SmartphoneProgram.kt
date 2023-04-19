package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.crowd.smartphoneRuntimeSetup
import it.nicolasfarabegoli.pulverization.runtime.PulverizationRuntime
import kotlinx.coroutines.delay

suspend fun smartphoneProgram(deviceId: String, host: String) {
    val runtimeConfig = smartphoneRuntimeSetup()
    val runtime = PulverizationRuntime(deviceId, host, runtimeConfig)

    runtime.start()
    while (true) { delay(1) }
}
