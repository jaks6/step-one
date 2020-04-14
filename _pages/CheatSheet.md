# STEP-ONE Process Variables Cheat Sheet

## Process variables assigned dynamically by engine

| Object                  | variable name             | Description                                                  |
| ----------------------- | ------------------------- | ------------------------------------------------------------ |
| Message                 | last_msg_source_address   | origin of last received message                              |
| Start Process Message   | startMessageSenderAddress |                                                              |
| Signal "Device Nearby"  | sig_remote_address        |                                                              |
| Signal "Disconnected"   | sig_remote_address        |                                                              |
| Signal "New Coordinate" |                           | Need to add an execution listener that will subscribe this process to a given coordinates signals |

