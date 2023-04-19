package it.nicolasfarabegoli.pulverization.crowd.smartphone

import co.touchlab.kermit.Logger
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class NeighboursRssi(val neighboursRssi: Map<String, Int>)

class SmartphoneSensors(private val deviceId: String) : Sensor<NeighboursRssi> {
    private val logger = Logger.withTag("SmartphoneSensors")

    companion object {
        private const val MIN_RSSI = -75
        private const val MAX_RSSI = -50
        private const val PERCEIVED_NEIGHBOURS = 3
    }
    override suspend fun sense(): NeighboursRssi {
        val perceived = (1..PERCEIVED_NEIGHBOURS)
            .filter { it != deviceId.toInt() }
            .map { it.toString() }
            .associateWith { Random.nextInt(MIN_RSSI, MAX_RSSI) }
        logger.i { "Perceived neighbours [$deviceId]: $perceived" }
        return NeighboursRssi(perceived)
    }
}

class SmartphoneSensorsContainer : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += SmartphoneSensors(context.deviceID).apply { initialize() }
    }
}

suspend fun smartphoneSensorsLogic(
    sensors: SensorsContainer,
    behaviourRef: BehaviourRef<NeighboursRssi>,
) = coroutineScope {
    sensors.get<SmartphoneSensors> {
        while (true) {
            behaviourRef.sendToComponent(sense())
            delay(500.milliseconds)
        }
    }
}
