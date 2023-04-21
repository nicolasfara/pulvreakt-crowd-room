package it.nicolasfarabegoli.pulverization.crowd.laboratory

import it.nicolasfarabegoli.pulverization.crowd.laboratoryRuntimeSetup
import it.nicolasfarabegoli.pulverization.runtime.PulverizationRuntime
import kotlinx.coroutines.delay

suspend fun laboratoryProgram(deviceId: String, host: String) {
    val runtimeConfig = laboratoryRuntimeSetup()
    val runtime = PulverizationRuntime(deviceId, host, runtimeConfig)

    runtime.start()
    while (true) { delay(1) }
}
