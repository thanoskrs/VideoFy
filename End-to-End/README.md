# EXECUTION

**BACKEND EXECUTION**:
* First, we run MainServer.
* We have created 3 configurations for Brokers, Broker1, Broker2, Broker3. We execute them.



**ANDROID EXECUTION**:

* 1st way (Execute from android studio emulators):

We create 2 configurations app1, app2.
e.g.	app1: launch flags: -e key 5017
	app2: launch flags: -e key 5018

app1 and app2 should be executed in different emulators.

Assume we have 2 emulators. Let first be the 5554 and second the 5556.
App1 will be executed to first emulator(5554)
App2 will be executed to second emulator(5556)

That's because of redirections where we run:
first emulator--> redir add:tcp:5017:5017
second emulatos--> redir add:tcp:5018:5018



* 2nd way (Execute from real android devices):

MainServer's IP address needed to be changed from localhost IP to local IP of the machine running the MainServer.
Change should be done to: LogInActivity.java>line:62
