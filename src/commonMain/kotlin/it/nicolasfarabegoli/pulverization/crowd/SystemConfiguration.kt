package it.nicolasfarabegoli.pulverization.crowd

import it.nicolasfarabegoli.pulverization.crowd.room.CongestionColor
import it.nicolasfarabegoli.pulverization.crowd.room.RoomActuatorsContainer
import it.nicolasfarabegoli.pulverization.crowd.room.RoomBehaviour
import it.nicolasfarabegoli.pulverization.crowd.room.RoomState
import it.nicolasfarabegoli.pulverization.crowd.room.StateOps
import it.nicolasfarabegoli.pulverization.crowd.room.roomActuatorsLogic
import it.nicolasfarabegoli.pulverization.crowd.room.roomBehaviourLogic
import it.nicolasfarabegoli.pulverization.crowd.room.roomStateLogic
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursRssi
import it.nicolasfarabegoli.pulverization.crowd.smartphone.SmartphoneBehaviour
import it.nicolasfarabegoli.pulverization.crowd.smartphone.SmartphoneSensorsContainer
import it.nicolasfarabegoli.pulverization.crowd.smartphone.smartphoneBehaviourLogic
import it.nicolasfarabegoli.pulverization.crowd.smartphone.smartphoneSensorsLogic
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
    device("room") {
        Behaviour and State deployableOn HighCPU
        Communication deployableOn HighCPU
        Actuators deployableOn CanShow
    }
    device("smartphone") {
        Behaviour deployableOn setOf(HighCPU, EmbeddedDevice)
        Communication deployableOn EmbeddedDevice
        Sensors deployableOn EmbeddedDevice
    }
}

suspend fun smartphoneRuntimeSetup():
    DeploymentUnitRuntimeConfiguration<Unit, NeighboursDistances, NeighboursRssi, Unit, Unit> {
    return pulverizationRuntime(config, "smartphone", availableHosts) {
        SmartphoneBehaviour() withLogic ::smartphoneBehaviourLogic startsOn Server
        DeviceCommunication() withLogic ::smartphoneCommunicationLogic startsOn Smartphone
        SmartphoneSensorsContainer() withLogic ::smartphoneSensorsLogic startsOn Smartphone

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

suspend fun roomRuntimeSetup():
    DeploymentUnitRuntimeConfiguration<StateOps, NeighboursDistances, Unit, CongestionColor, Unit> {
    return pulverizationRuntime(config, "room", availableHosts) {
        RoomBehaviour() withLogic ::roomBehaviourLogic startsOn LocalPC
        RoomState() withLogic ::roomStateLogic startsOn LocalPC
        DeviceCommunication() withLogic ::roomCommunicationLogic startsOn LocalPC
        RoomActuatorsContainer() withLogic ::roomActuatorsLogic startsOn LocalPC

        withCommunicator { RabbitmqCommunicator(hostname = "rabbitmq") }
        withReconfigurator { RabbitmqReconfigurator(hostname = "rabbitmq") }
        withRemotePlaceProvider { defaultRabbitMQRemotePlace() }
    }
}
