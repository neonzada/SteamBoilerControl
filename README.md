# Steam Boiler Control
This project consists of an implementation of a real time control system for a steam boiler, using Java and the RTSJ (Real-Time Specification for Java), executing under **JamaicaVM**. The system has the objective of maintaining the water level within reasonable limits, even with failure in multiple components. This was achieved via a PID controller.

There are two main layers: the **physical simulator** updates the state of the boiler each second, calculating input via pumps and output via steam, and the **controller** which runs every 5 seconds, reading the sensors, deciding the operation state and adjusting the pumps. 

## Operation states
There are five states in which the system can be:
- Initialization: verifies the initial conditions.
- Normal: standard operation with all pumps and sensors.
- Degraded: operation with one failed pump.
- Salvage: operation with failed sensors.
- Emergency stop: all pumps failed and failed signal.

## Build
To build and run this program, you will need a UNIX-based OS with JamaicaVM installed, as well as Java 8. Simply compile with this command (check if both `jamaica-8.10-1` and `java-8-openjdk-amd64` paths are correct):
```bash
/usr/lib/jvm/java-8-openjdk-amd64/bin/javac -bootclasspath /usr/local/jamaica-8.10-1/target/linux-x86_64/lib/rt.jar -extdirs /usr/local/jamaica-8.10-1/target/linux-x86_64/lib/ext *.java
```
...and then run the App:
```bash
jamaicavm App
```

## Example output
```
[PHYS] t=1s | Level=430.0 L | Steam=70.0 L/s
[PHYS] t=2s | Level=360.0 L | Steam=70.0 L/s
[PHYS] t=3s | Level=290.0 L | Steam=70.0 L/s
[PHYS] t=4s | Level=220.0 L | Steam=70.0 L/s
[PHYS] t=5s | Level=150.0 L | Steam=70.0 L/s
===================================================================================
[CTRL] t=5s | Mode=NORMAL | Level=219.9 L | Steam=69.8 L/s | Pumps: #1=50.0 #2=50.0
===================================================================================
[PHYS] t=6s | Level=180.0 L | Steam=70.0 L/s
[PHYS] t=7s | Level=210.0 L | Steam=70.0 L/s
[PHYS] t=8s | Level=240.0 L | Steam=70.0 L/s
[PHYS] t=9s | Level=270.0 L | Steam=70.0 L/s
[PHYS] t=10s | Level=300.0 L | Steam=70.0 L/s
====================================================================================
[CTRL] t=10s | Mode=NORMAL | Level=299.6 L | Steam=70.0 L/s | Pumps: #1=50.0 #2=50.0
====================================================================================
```