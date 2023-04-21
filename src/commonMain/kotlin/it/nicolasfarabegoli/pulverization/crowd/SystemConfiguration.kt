package it.nicolasfarabegoli.pulverization.crowd

import it.nicolasfarabegoli.pulverization.crowd.laboratory.CongestionColor
import it.nicolasfarabegoli.pulverization.crowd.laboratory.LaboratoryActuatorsContainer
import it.nicolasfarabegoli.pulverization.crowd.laboratory.LaboratoryBehaviour
import it.nicolasfarabegoli.pulverization.crowd.laboratory.LaboratoryState
import it.nicolasfarabegoli.pulverization.crowd.laboratory.StateOps
import it.nicolasfarabegoli.pulverization.crowd.laboratory.laboratoryActuatorsLogic
import it.nicolasfarabegoli.pulverization.crowd.laboratory.roomBehaviourLogic
import it.nicolasfarabegoli.pulverization.crowd.laboratory.laboratoryStateLogic
import it.nicolasfarabegoli.pulverization.crowd.wearable.NeighboursRssi
import it.nicolasfarabegoli.pulverization.crowd.wearable.WearableBehaviour
import it.nicolasfarabegoli.pulverization.crowd.wearable.WearableSensorsContainer
import it.nicolasfarabegoli.pulverization.crowd.wearable.wearableBehaviourLogic
import it.nicolasfarabegoli.pulverization.crowd.wearable.wearableSensorsLogic
import it.nicolasfarabegoli.pulverization.dsl.model.Actuators
import it.nicolasfarabegoli.pulverization.dsl.model.Behaviour
import it.nicolasfarabegoli.pulverization.dsl.model.Capability
import it.nicolasfarabegoli.pulverization.dsl.model.Communication
import it.nicolasfarabegoli.pulverization.dsl.model.Sensors
import it.nicolasfarabegoli.pulverization.dsl.model.State
import it.nicolasfarabegoli.pulverization.dsl.pulverizationSystem
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqReconfigurator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.model.DeploymentUnitRuntimeConfiguration
import it.nicolasfarabegoli.pulverization.runtime.dsl.model.Host
import it.nicolasfarabegoli.pulverization.runtime.dsl.model.ReconfigurationEvent
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationRuntime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

@Serializable
object HighCPU : Capability

@Serializable
object EmbeddedDevice : Capability

@Serializable
object CanShow : Capability

@Serializable
object Server : Host {
    override val hostname: String = "server"
    override val capabilities: Set<Capability> = setOf(HighCPU)
}

@Serializable
object LocalPC : Host {
    override val hostname: String = "pc"
    override val capabilities: Set<Capability> = setOf(CanShow, HighCPU)
}

@Serializable
object Smartphone : Host {
    override val hostname: String = "smartphone"
    override val capabilities: Set<Capability> = setOf(EmbeddedDevice)
}

val availableHosts: Set<Host> = setOf(Server, LocalPC, Smartphone)

object HighLoadOnServer : ReconfigurationEvent<Double> {
    private const val CPU_THRESHOLD = 0.75
    private const val INCREMENT = 0.1
    private const val DELAY = 500L
    override val events: Flow<Double> = generateSequence(0.0) { it + INCREMENT }.asFlow().onEach { delay(DELAY) }
    override val predicate: (Double) -> Boolean = { it > CPU_THRESHOLD }
}

val config = pulverizationSystem {
    device("laboratory") {
        Behaviour and State deployableOn HighCPU
        Communication deployableOn HighCPU
        Actuators deployableOn CanShow
    }
    device("wearable") {
        Behaviour deployableOn setOf(HighCPU, EmbeddedDevice)
        Communication deployableOn EmbeddedDevice
        Sensors deployableOn EmbeddedDevice
    }
}

suspend fun wearableRuntimeSetup():
    DeploymentUnitRuntimeConfiguration<Unit, NeighboursDistances, NeighboursRssi, Unit, Unit> {
    return pulverizationRuntime(config, "wearable", availableHosts) {
        WearableBehaviour() withLogic ::wearableBehaviourLogic startsOn Server
        DeviceCommunication() withLogic ::wearableCommunicationLogic startsOn Smartphone
        WearableSensorsContainer() withLogic ::wearableSensorsLogic startsOn Smartphone

        reconfigurationRules {
            onDevice {
                HighLoadOnServer reconfigures { Behaviour movesTo Smartphone }
            }
        }

        withCommunicator { RabbitmqCommunicator(hostname = "rabbitmq") }
        withReconfigurator { RabbitmqReconfigurator(hostname = "rabbitmq") }
        withRemotePlaceProvider { defaultRabbitMQRemotePlace() }
    }
}

suspend fun laboratoryRuntimeSetup():
    DeploymentUnitRuntimeConfiguration<StateOps, NeighboursDistances, Unit, CongestionColor, Unit> {
    return pulverizationRuntime(config, "laboratory", availableHosts) {
        LaboratoryBehaviour() withLogic ::roomBehaviourLogic startsOn LocalPC
        LaboratoryState() withLogic ::laboratoryStateLogic startsOn LocalPC
        DeviceCommunication() withLogic ::laboratoryCommunicationLogic startsOn LocalPC
        LaboratoryActuatorsContainer() withLogic ::laboratoryActuatorsLogic startsOn LocalPC

        withCommunicator { RabbitmqCommunicator(hostname = "rabbitmq") }
        withReconfigurator { RabbitmqReconfigurator(hostname = "rabbitmq") }
        withRemotePlaceProvider { defaultRabbitMQRemotePlace() }
    }
}
