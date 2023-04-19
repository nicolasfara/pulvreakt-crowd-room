package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.crowd.NeighboursDistances
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.inject
import kotlin.math.pow

class SmartphoneBehaviour : Behaviour<Unit, NeighboursDistances, NeighboursRssi, Unit, Unit> {
    override val context: Context by inject()

    companion object {
        private const val RSSI_ONE_METER = -40
        private const val ENV_CONSTANT = (10.0 * 2.0)
    }

    override fun invoke(
        state: Unit,
        export: List<NeighboursDistances>,
        sensedValues: NeighboursRssi,
    ): BehaviourOutput<Unit, NeighboursDistances, Unit, Unit> {
        val perceivedRssi = sensedValues.neighboursRssi
        val distances = perceivedRssi.mapValues { (_, rssi) -> 10.0.pow((RSSI_ONE_METER - rssi) / ENV_CONSTANT) }
        val message = NeighboursDistances(context.deviceID, distances)
        return BehaviourOutput(Unit, message, Unit, Unit)
    }
}

@Suppress("UnusedPrivateMember")
suspend fun smartphoneBehaviourLogic(
    behaviour: Behaviour<Unit, NeighboursDistances, NeighboursRssi, Unit, Unit>,
    stateRef: StateRef<Unit>,
    commRef: CommunicationRef<NeighboursDistances>,
    sensorsRef: SensorsRef<NeighboursRssi>,
    actuatorsRef: ActuatorsRef<Unit>,
) = coroutineScope {
    stateRef.setup()
    actuatorsRef.setup()
    sensorsRef.receiveFromComponent().collect { sensorRead ->
        val (_, message, _) = behaviour(Unit, emptyList(), sensorRead)
        commRef.sendToComponent(message)
    }
}
