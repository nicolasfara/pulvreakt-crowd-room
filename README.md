# ACSOS-2023-pulverization-crowd-room

# Scenario

People equipped with **smartphones** move around inside a **room**.
The smartphones sense the distance between them through the (simulated) RSSI signal of Bluetooth.
Each smartphone then sends the distance information to the room, which then provides a metric for aggregating people within the room.
The room shows on a monitor a color tending toward the green the farther away people are from each other; instead, it shows a color that turns toward red the closer people are to each other.

The system involves the definition of two devices: smartphone and room.
The available infrastructure includes a server, a local pc and the smartphones themselves.
The system, taking advantage of the pulverization approach, is intended to be reconfigured if certain conditions occur.
The system starts by executing the _behavior_ of the smartphones on the server, while _sensors_ and _communication_ are executed on the smartphones.
The room, on the other hand, has all components executed on the local pc.
In the demo, the event that triggers a deployment reconfiguration is the high load on the server.
Should this condition occur, the load on the server is reduced by shifting the behavior directly to the smartphones.

## Usage

Run the demo with:

```bash
$ docker-compose up
```

## Logs interpretation

After the startup of the `docker-compose`, the following logs represents the framework initialization:

```
...
acsos-2023-pulverization-crowd-room-server-1       | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-server-1       | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-pc-1           | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-server-1       | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and State[component]
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
...
acsos-2023-pulverization-crowd-room-server-1       | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Sensor[component]
acsos-2023-pulverization-crowd-room-pc-1           | Info: (Behaviour[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
acsos-2023-pulverization-crowd-room-server-1       | Info: (State[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from State[component] and Behaviour[component]
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (State[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from State[component] and Behaviour[component]
acsos-2023-pulverization-crowd-room-server-1       | Info: (State[component]Communicator) Setup communicator
acsos-2023-pulverization-crowd-room-server-1       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from State[component] and Behaviour[component]

```

After the initialization step, each deployment unit starts to run the components. In particular, each smartphone
starts sending to the room the distances from the other smartphones.
The following log example shows this interaction:

```
...
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [3]: {1=-57, 2=-70}
acsos-2023-pulverization-crowd-room-server-1       | Info: (SmartphoneBehaviour) Smartphone [3] distances: {1=0.7079457843841379, 2=3.1622776601683795}
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 1.8377612084846502
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=119, green=136, blue=0)
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [1]: {2=-69, 3=-53}
acsos-2023-pulverization-crowd-room-server-1       | Info: (SmartphoneBehaviour) Smartphone [1] distances: {2=2.8183829312644537, 3=0.44668359215096315}
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 1.5314239357477304
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=150, green=105, blue=0)
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [2]: {1=-75, 3=-56}
acsos-2023-pulverization-crowd-room-server-1       | Info: (SmartphoneBehaviour) Smartphone [2] distances: {1=5.623413251903491, 3=0.6309573444801932}
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 2.231610094058603
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=79, green=176, blue=0)
...
```

The order of the printed log do not represents the real oder on which the communication occurs.
Nevertheless, from the log above, `Info: (SmartphoneSensors) Perceived neighbours [<n>]: {<n_1>=-<rssi>, <n_2>=-<rssi>}`
represents that the device `<n>` has perceived `<n_1>` and `<n_2>` with their corresponding RSSI.
`Info: (SmartphoneBehaviour) Smartphone [<n>] distances: {<n_1>=0.7079457843841379, <n_2>=3.1622776601683795}` represents the conversion of the RSSI into a distance.

`Info: (RoomBehaviour) Mean distance: 1.496505733160854` means that the room has updated its congestion metrics and
`Info: (RoomActuator) New color shown: CongestionColor(red=184, green=71, blue=0)` represents the color update in the monitor relative to the metrics update.

### Reconfiguration event

When the reconfiguration event is triggered, the log is the following:

```
...
acsos-2023-pulverization-crowd-room-server-1       | New reconfiguration!
acsos-2023-pulverization-crowd-room-server-1       | New reconfiguration!
acsos-2023-pulverization-crowd-room-server-1       | New reconfiguration!
...
```

After the reconfiguration, the log change as following:

```
...
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 0.8481771675745647
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=220, green=35, blue=0)
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [2]: {1=-54, 3=-62}
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneBehaviour) Smartphone [2] distances: {1=0.5011872336272722, 3=1.2589254117941673}
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 0.9700185226391894
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=208, green=47, blue=0)
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [3]: {1=-75, 2=-53}
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneBehaviour) Smartphone [3] distances: {1=5.623413251903491, 2=0.44668359215096315}
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomBehaviour) Mean distance: 1.7828182183698418
acsos-2023-pulverization-crowd-room-pc-1           | Info: (RoomActuator) New color shown: CongestionColor(red=125, green=130, blue=0)
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneSensors) Perceived neighbours [1]: {2=-70, 3=-54}
acsos-2023-pulverization-crowd-room-smartphones-1  | Info: (SmartphoneBehaviour) Smartphone [1] distances: {2=3.1622776601683795, 3=0.5011872336272722}
...
```

As can be seen, the behaviour moves from the **server** into the **smartphone**. This can be seen because the log is printed from the
`acsos-2023-pulverization-crowd-room-smartphones-1` (previously executed on `acsos-2023-pulverization-crowd-room-server-1`).

## Verbose logs
