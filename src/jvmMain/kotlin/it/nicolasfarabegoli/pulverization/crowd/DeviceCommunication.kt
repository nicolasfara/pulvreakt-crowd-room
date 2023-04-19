package it.nicolasfarabegoli.pulverization.crowd

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.utils.PulverizationKoinModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import reactor.rabbitmq.BindingSpecification
import reactor.rabbitmq.ExchangeSpecification
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.QueueSpecification
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Receiver
import reactor.rabbitmq.ReceiverOptions
import reactor.rabbitmq.Sender
import reactor.rabbitmq.SenderOptions

actual class DeviceCommunication : Communication<NeighboursDistances> {
    override val context: Context by inject()

    override fun getKoin(): Koin = PulverizationKoinModule.koinApp?.koin ?: error("Koin app not initialized")

    private lateinit var sender: Sender
    private lateinit var receiver: Receiver
    private lateinit var queue: String

    companion object {
        private const val HOST = "rabbitmq" // TODO(change hostname in docker container)
        private const val RMQ_PORT = 5672
        private const val EXCHANGE = "pulverization.crowd"
    }

    override suspend fun initialize() {
        val connection = setupConnection()
        with(connection) {
            val senderOption = SenderOptions().connectionSupplier { this }
            val receiverOptions = ReceiverOptions().connectionSupplier { this }
            sender = RabbitFlux.createSender(senderOption)
            receiver = RabbitFlux.createReceiver(receiverOptions)
        }

        with(sender) {
            queue = "communication/${context.deviceID}"
            declareExchange(ExchangeSpecification.exchange(EXCHANGE).durable(false).type("fanout")).awaitSingleOrNull()
                ?: error("Unable to declare the exchange `$EXCHANGE`")
            declareQueue(QueueSpecification.queue(queue).durable(false)).awaitSingleOrNull()
                ?: error("Unable to declare queue `$queue`")
            bindQueue(BindingSpecification().exchange(EXCHANGE).queue(queue).routingKey("")).awaitSingleOrNull()
                ?: error("Unable to bind `$EXCHANGE` with `$queue`")
        }
    }

    override suspend fun send(payload: NeighboursDistances) {
        val message = OutboundMessage(EXCHANGE, "", Json.encodeToString(payload).toByteArray())
        sender.send(Mono.just(message)).awaitSingleOrNull()
    }

    override fun receive(): Flow<NeighboursDistances> = receiver.consumeAutoAck(queue)
        .asFlow()
        .map { Json.decodeFromString(it.body.decodeToString()) }

    private fun setupConnection(): Connection {
        val connectionFactory = ConnectionFactory()
        connectionFactory.useNio()
        connectionFactory.apply {
            host = HOST
            port = RMQ_PORT
            username = "guest"
            password = "guest"
            virtualHost = "/"
        }
        return connectionFactory.newConnection()
    }
}
