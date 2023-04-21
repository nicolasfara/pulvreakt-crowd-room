package it.nicolasfarabegoli.pulverization.crowd

import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

@Serializable
data class NeighboursDistances(val deviceId: String, val distances: Map<String, Double>)

expect class DeviceCommunication() : Communication<NeighboursDistances>

suspend fun laboratoryCommunicationLogic(
    communication: Communication<NeighboursDistances>,
    behaviourRef: BehaviourRef<NeighboursDistances>,
) = coroutineScope {
    communication.receive().collect {
        behaviourRef.sendToComponent(it)
    }
}

suspend fun wearableCommunicationLogic(
    communication: Communication<NeighboursDistances>,
    behaviourRef: BehaviourRef<NeighboursDistances>,
) = coroutineScope {
    behaviourRef.receiveFromComponent().collect {
        communication.send(it)
    }
}
