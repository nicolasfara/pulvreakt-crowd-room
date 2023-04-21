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
pc           | Info: (Behaviour[component]Communicator) Setup communicator
pc           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Communication[component]
pc           | Info: (Behaviour[component]Communicator) Setup communicator
pc           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Actuators[component]
server       | Info: (Behaviour[component]Communicator) Setup communicator
server       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Sensor[component]
pc           | Info: (Sensor[component]Communicator) Setup communicator
pc           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Sensor[component] and Behaviour[component]
server       | Info: (Communication[component]Communicator) Setup communicator
server       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Communication[component] and Behaviour[component]
smartphones  | Info: (Behaviour[component]Communicator) Setup communicator
smartphones  | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Behaviour[component] and Sensor[component]
server       | Info: (State[component]Communicator) Setup communicator
pc           | Info: (Actuators[component]Communicator) Setup communicator
pc           | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from Actuators[component] and Behaviour[component]
server       | Info: (RabbitmqCommunicator) Setup RabbitMQ communicator from State[component] and Behaviour[component]
...
...
```

After the initialization step, each deployment unit starts to run the components. In particular, each smartphone
starts sending to the **laboratory** the distances from the other smartphones.
The following log example shows this interaction:

```
...
smartphones  | Info: (WearableSensors) Wearable#1 Perceived neighbours: {2=-56, 3=-56}
server       | Info: (WearableBehaviour) Wearable#1 distances: {2=0.6309573444801932, 3=0.6309573444801932}
pc           | Info: (LaboratoryBehaviour) Mean distance: 1.620525177448518
pc           | Info: (LaboratoryActuator) Print color: ██████████
smartphones  | Info: (WearableSensors) Wearable#3 Perceived neighbours: {1=-63, 2=-57}
server       | Info: (WearableBehaviour) Wearable#3 distances: {1=1.4125375446227544, 2=0.7079457843841379}
pc           | Info: (LaboratoryBehaviour) Mean distance: 1.6333565840991753
pc           | Info: (LaboratoryActuator) Print color: ██████████
smartphones  | Info: (WearableSensors) Wearable#2 Perceived neighbours: {1=-66, 3=-74}
server       | Info: (WearableBehaviour) Wearable#2 distances: {1=1.9952623149688795, 3=5.011872336272722}
pc           | Info: (LaboratoryBehaviour) Mean distance: 1.7315887782014798
pc           | Info: (LaboratoryActuator) Print color: ██████████
...
```

The order of the printed log does not represents the real order on which the communication occurs.
Nevertheless, from the log above, `Info: (SmartphoneSensors) Wearable#<n> Perceived neighbours: {<n_1>=-<rssi>, <n_2>=-<rssi>}`
represents that the device `<n>` has perceived `<n_1>` and `<n_2>` with their corresponding RSSI.
`Info: (SmartphoneBehaviour) Wearable#<n> distances: {<n_1>=<distance>, <n_2>=<distance>}` represents the conversion
of the RSSI into a distance.

`Info: (RoomBehaviour) Mean distance: <mean_distance>` means that the laboratory has updated its congestion metrics and
`Info: (LaboratoryActuator) Print color: [COLOR]` represents the color update in the monitor relative to the metrics update.

### Reconfiguration event

When the reconfiguration event is triggered, the log is the following:

```
...
server       | New reconfiguration!
server       | New reconfiguration!
server       | New reconfiguration!
...
```

After the reconfiguration, the log change as following:

```
...
smartphones  | Info: (WearableSensors) Wearable#2 Perceived neighbours: {1=-68, 3=-69}
smartphones  | Info: (WearableBehaviour) Wearable#2 distances: {1=2.51188643150958, 3=2.8183829312644537}
pc           | Info: (LaboratoryBehaviour) Mean distance: 1.8090095008099896
pc           | Info: (LaboratoryActuator) Print color: ██████████
smartphones  | Info: (WearableSensors) Wearable#1 Perceived neighbours: {2=-60, 3=-75}
smartphones  | Info: (WearableBehaviour) Wearable#1 distances: {2=1.0, 3=5.623413251903491}
pc           | Info: (LaboratoryBehaviour) Mean distance: 2.516087731777883
pc           | Info: (LaboratoryActuator) Print color: ██████████
smartphones  | Info: (WearableSensors) Wearable#3 Perceived neighbours: {1=-62, 2=-73}
smartphones  | Info: (WearableBehaviour) Wearable#3 distances: {1=1.2589254117941673, 2=4.466835921509632}
pc           | Info: (LaboratoryBehaviour) Mean distance: 2.9465739913302205
pc           | Info: (LaboratoryActuator) Print color: ██████████
...
```

As can be seen, the behaviour moves from the **Server** into the **Smartphones**.
This can be seen because the log is printed from the `smartphones` (previously executed on `server`).

## Verbose logs

To have a more verbose log, the DEBUG_LOG_LEVEL environment variable can be set to "1".
This enables the debug log inside the framework to show the interaction and communication between components to
better understand how the framework behaves.
