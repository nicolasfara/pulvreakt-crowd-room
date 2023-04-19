package it.nicolasfarabegoli.pulverization.crowd.room

import co.touchlab.kermit.Logger
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
data class CongestionColor(val red: Int, val green: Int, val blue: Int)

class RoomActuator : Actuator<CongestionColor> {
    private val logger = Logger.withTag("RoomActuator")
    override suspend fun actuate(payload: CongestionColor) = logger.i { "New color shown: $payload" }
}

class RoomActuatorsContainer : ActuatorsContainer() {
    override val context: Context by inject()
    override suspend fun initialize() {
        this += RoomActuator().apply { initialize() }
    }
}

suspend fun roomActuatorsLogic(
    actuators: ActuatorsContainer,
    behaviourRef: BehaviourRef<CongestionColor>,
) = coroutineScope {
    actuators.get<RoomActuator> {
        behaviourRef.receiveFromComponent().collect { actuate(it) }
    }
}
