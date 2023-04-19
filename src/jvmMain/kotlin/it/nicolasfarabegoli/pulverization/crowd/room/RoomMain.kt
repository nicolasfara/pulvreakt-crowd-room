package it.nicolasfarabegoli.pulverization.crowd.room

suspend fun main(args: Array<String>) {
    require(args.size == 2) { "usage: java -jar smartphone.jar device-id host" }
    val deviceId = args[0]
    val host = args[1]
    roomProgram(deviceId, host)
}
