package it.nicolasfarabegoli.pulverization.crowd.smartphone

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

class SmartphoneSensors : Sensor<NeighboursRssi> {
    companion object {
        private const val MIN_RSSI = -90
        private const val MAX_RSSI = -30
        private const val PERCEIVED_NEIGHBOURS = 2
    }
    override suspend fun sense(): NeighboursRssi {
        val perceived = (1..PERCEIVED_NEIGHBOURS)
            .map { it.toString() }
            .associateWith { Random.nextInt(MIN_RSSI, MAX_RSSI) }
        return NeighboursRssi(perceived)
    }
}

class SmartphoneSensorsContainer : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += SmartphoneSensors().apply { initialize() }
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
