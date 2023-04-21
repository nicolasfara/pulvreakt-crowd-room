package it.nicolasfarabegoli.pulverization.crowd.laboratory

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

private fun String.colorize(color: CongestionColor): String {
    val (r, g, b) = color
    return "\u001B[38;2;$r;$g;${b}m${this}\u001B[0m"
}

class LaboratoryActuator : Actuator<CongestionColor> {
    private val logger = Logger.withTag("LaboratoryActuator")
    private val solidBlocks = (1..BLOCK_LENGTH).map { BLOCK }.joinToString("")

    companion object {
        private const val BLOCK_LENGTH = 10
        private const val BLOCK = '\u2588'
    }

    override suspend fun actuate(payload: CongestionColor) = logger.i {
        "Print color: ${solidBlocks.colorize(payload)}"
    }
}

class LaboratoryActuatorsContainer : ActuatorsContainer() {
    override val context: Context by inject()
    override suspend fun initialize() {
        this += LaboratoryActuator().apply { initialize() }
    }
}

suspend fun laboratoryActuatorsLogic(
    actuators: ActuatorsContainer,
    behaviourRef: BehaviourRef<CongestionColor>,
) = coroutineScope {
    actuators.get<LaboratoryActuator> {
        behaviourRef.receiveFromComponent().collect { actuate(it) }
    }
}
