package it.nicolasfarabegoli.pulverization.crowd.room

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

suspend fun main(args: Array<String>) {
    require(args.size == 2) { "usage: java -jar smartphone.jar device-id host" }
    val debugVar = System.getenv("DEBUG_LOG_LEVEL")
    val severity = if (debugVar == null || debugVar == "0") Severity.Info else Severity.Debug
    Logger.setMinSeverity(severity)
    val deviceId = args[0]
    val host = args[1]
    roomProgram(deviceId, host)
}
