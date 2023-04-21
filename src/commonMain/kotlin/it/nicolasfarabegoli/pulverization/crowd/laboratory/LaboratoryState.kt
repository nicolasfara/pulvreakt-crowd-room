package it.nicolasfarabegoli.pulverization.crowd.laboratory

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
sealed interface StateOps

@Serializable
data class LaboratoryCongestion(val congestion: Double) : StateOps

@Serializable
object GetState : StateOps

class LaboratoryState : State<StateOps> {
    override val context: Context by inject()

    private var actualState = LaboratoryCongestion(0.0)

    override fun get(): StateOps = actualState

    override fun update(newState: StateOps): LaboratoryCongestion {
        return when (newState) {
            is GetState -> actualState
            is LaboratoryCongestion -> {
                actualState = newState
                newState
            }
        }
    }
}

suspend fun laboratoryStateLogic(
    state: State<StateOps>,
    behaviourRef: BehaviourRef<StateOps>,
) = coroutineScope {
    behaviourRef.receiveFromComponent().collect {
        when (it) {
            is GetState -> behaviourRef.sendToComponent(state.get())
            is LaboratoryCongestion -> state.update(it)
        }
    }
}
