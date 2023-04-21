# ACSOS-2023-pulverization-crowd-room

# Scenario

Many people may have access to a laboratory and for security reasons they must maintain a certain distance on average.
The laboratory can sense the distance between people through a wearable that they wear.
To ensure the distance between people within the laboratory, a green color is shown on a monitor when people are far
apart, and a red color the closer people are to each other.

A possible implementation of this scenario may involve the definition of the following devices:
**wearable** and **laboratory**.
The former has the task of sensing other wearables by determining their distances, then sending that information to
the **laboratory**.
The latter has the task of determining a laboratory congestion metric based on the average of the distances
(received from the **wearables**) and transforming this metric into the corresponding color that will be shown on
the screen.

The proposed demo exploits the pulverization approach to implement the above system via the
[pulverization-framework](https://github.com/nicolasfara/pulverization-framework).
Specifically, the two devices, **wearable** and **laboratory**, are structured as follows:

- **wearable**: _Sensor_, _Behavior_, and _Communication_
- **laboratory**: _Actuator_, _Behavior_, _State_, and _Communication_

The **wearable** through the _Sensor_ component senses the other devices through the RSSI value of Bluetooth (simulated).
Through the _Behavior_, it converts the RSSI values to distances and then sends this data to the **laboratory** through
the _Communication_ component.

Similarly, the **laboratory** receives the distance data from the various **wearables** through the _Communication_ component,
determines the average distance of the devices in the room and persists this information in the _State_ component,
and then converts the average distance into the respective color which is then shown on the screen through
the _Actuator_ component.

The infrastructure provided to run this system includes the following hosts:

- 1 _Server_
- 1 _PC_
- $N$ _Smartphones_ (as many as there are people inside the lab).

Initially the **wearables** start with _Behavior_ component running on the server, while the other components are
run on the Smartphone.
The **laboratory** device has all components running on the PC.
A reconfiguration rule is defined such that when the server has a high load, the **wearables**' _Behavior_ component
is moved from the Server to the Smartphone.

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
...
```

After the initialization step, each deployment unit starts to run the components. In particular, each smartphone
starts sending to the **laboratory** the distances from the other smartphones.
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

The order of the printed log does not represents the real order on which the communication occurs.
Nevertheless, from the log above, `Info: (SmartphoneSensors) Perceived neighbours [<n>]: {<n_1>=-<rssi>, <n_2>=-<rssi>}`
represents that the device `<n>` has perceived `<n_1>` and `<n_2>` with their corresponding RSSI.
`Info: (SmartphoneBehaviour) Smartphone [<n>] distances: {<n_1>=<distance>, <n_2>=<distance>}` represents the conversion
of the RSSI into a distance.

`Info: (RoomBehaviour) Mean distance: 1.496505733160854` means that the laboratory has updated its congestion metrics and
`Info: (RoomActuator) New color shown: CongestionColor(red=<red_channel>, green=<green_channel>, blue=<blue_channel>)`
represents the color update in the monitor relative to the metrics update.

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

As can be seen, the behaviour moves from the **Server** into the **Smartphones**. This can be seen because the log is printed from the
`acsos-2023-pulverization-crowd-room-smartphones-1` (previously executed on `acsos-2023-pulverization-crowd-room-server-1`).

## Verbose logs

To have a more verbose log, the DEBUG_LOG_LEVEL environment variable can be set to "1".
This enables the debug log inside the framework to show the interaction and communication between components to
better understand how the framework behaves.
