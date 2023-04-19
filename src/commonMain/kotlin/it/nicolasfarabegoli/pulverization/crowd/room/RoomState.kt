package it.nicolasfarabegoli.pulverization.crowd.room

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
sealed interface StateOps

@Serializable
data class RoomCongestion(val congestion: Double) : StateOps

@Serializable
object GetState : StateOps

class RoomState : State<StateOps> {
    override val context: Context by inject()

    private var actualState = RoomCongestion(0.0)

    override fun get(): StateOps = actualState

    override fun update(newState: StateOps): RoomCongestion {
        return when (newState) {
            is GetState -> actualState
            is RoomCongestion -> {
                actualState = newState
                newState
            }
        }
    }
}

suspend fun roomStateLogic(
    state: State<StateOps>,
    behaviourRef: BehaviourRef<StateOps>,
) = coroutineScope {
    behaviourRef.receiveFromComponent().collect {
        when (it) {
            is GetState -> behaviourRef.sendToComponent(state.get())
            is RoomCongestion -> state.update(it)
        }
    }
}
