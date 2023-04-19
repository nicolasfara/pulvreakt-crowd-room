package it.nicolasfarabegoli.pulverization.crowd

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject

actual class DeviceCommunication : Communication<NeighboursDistances> {
    override val context: Context by inject()

    override fun receive(): Flow<NeighboursDistances> {
        TODO("Not yet implemented")
    }

    override suspend fun send(payload: NeighboursDistances) {
        TODO("Not yet implemented")
    }
}
