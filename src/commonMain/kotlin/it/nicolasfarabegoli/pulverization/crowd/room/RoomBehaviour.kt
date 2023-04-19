package it.nicolasfarabegoli.pulverization.crowd.room

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.crowd.NeighboursDistances
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject

class RoomBehaviour : Behaviour<StateOps, NeighboursDistances, Unit, CongestionColor, Unit> {
    override val context: Context by inject()

    companion object {
        private const val MIN_DISTANCE = 0.5
        private const val MAX_DISTANCE = 3.0
        private const val MAX_RGB_VALUE = 255
    }

    override fun invoke(
        state: StateOps,
        export: List<NeighboursDistances>,
        sensedValues: Unit,
    ): BehaviourOutput<StateOps, NeighboursDistances, CongestionColor, Unit> {
        val distances = export
            .filter { it.deviceId != "0" }
            .map { it.distances.values }
            .flatten()
        var meanDistance = distances.sum() / distances.size

        if (meanDistance < MIN_DISTANCE) meanDistance = MIN_DISTANCE
        if (meanDistance > MAX_DISTANCE) meanDistance = MAX_DISTANCE

        val greenChannel = ((meanDistance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)).toInt()
        val redChannel = MAX_RGB_VALUE - greenChannel
        val congestionColor = CongestionColor(redChannel, greenChannel, 0)

        return BehaviourOutput(
            RoomCongestion(meanDistance),
            NeighboursDistances("0", emptyMap()),
            congestionColor,
            Unit,
        )
    }
}

@Suppress("UnusedPrivateMember")
suspend fun roomBehaviourLogic(
    behaviour: Behaviour<StateOps, NeighboursDistances, Unit, CongestionColor, Unit>,
    stateRef: StateRef<StateOps>,
    communicationRef: CommunicationRef<NeighboursDistances>,
    sensorsRef: SensorsRef<Unit>,
    actuatorsRef: ActuatorsRef<CongestionColor>,
) = coroutineScope {
    var neighboursMessages = emptyList<NeighboursDistances>()
    sensorsRef.setup()
    communicationRef.receiveFromComponent().collect { mess ->
        neighboursMessages = neighboursMessages.filter { it.deviceId != mess.deviceId } + mess
        stateRef.sendToComponent(GetState)
        val state = stateRef.receiveFromComponent().first()
        val (newState, _, actions) = behaviour(state, neighboursMessages, Unit)
        stateRef.sendToComponent(newState)
        actuatorsRef.sendToComponent(actions)
    }
}
