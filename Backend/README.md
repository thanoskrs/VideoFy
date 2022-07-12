# STRUCTURE

MainServer:
	* Each broker(server) is connected to MainServer and the MainServer stores in an appropriate structure the IP and port of each Broker in order to be sent to  the Appnodes-Users of the app.
	* Maintains the usernames and passwords of the AppNodes and stores them locally to credentials.txt to be used forAppnodes login later.
	* Stores in a hashmap the responsible broker for each topic-hashtag and channelName in order to be sent to Appnodes(as consumers), when they request to watch videos with that topic or channelName.

Broker:
	* A serverSocket is initialized with a random, available port when a broker is executed.
	* Each brocker sends both its IP and port to the MainServer.
	* When a new video is uploaded, the responsible broker connects to the MainServer in order to inform it of its new topics and send its info(IP/port).
	* Each broker is rensponsible for Consumer-Publisher communication when a consumer requests videos from a specific topic or channelName.

AppNode:
	* When an AppNode is executed, will be able either to log in -if has already sign up- or signn up to the system. So, Appnode connects to MainServer that has the information about AppNodes credentials.
	* Besides, AppNodes get the hashMap of brokers from MainServer.
	* A serverSocket<PublisherServer> is initialized for each AppNode. In it brokers will connect to get the videos from each Appnode and send them to the Consumer who requested them.
	* When they upload a new video as publishers, they are connected to responsible brokers of publishised topics in order to inform the brokers about the new topics and their publisherSocket IP/port.
	* As consumers thewy connect to Mainserver to get the information about the existing topics and channelNames and the responsible broker for each of them. When they request a specific topic or channelName they connect to the responsible broker in order to get the videos.

 PublisherServer:
	* It is initialized in the AppNode component in order to receive connections from the brokers and execute the push function, sending the requested videos.



EXECUTION *:
	* First, we execute MainServer.
	* We have created 3 configurations for Brokers, Broker1, Broker2, Broker3. We execute them.
	* We have created 3 configurations for AppNodes, AppNode1, AppNode2, AppNode3. We execute them. Note, that we can dynamically create more AppNodes.

	
* * The only information needed for both brokers and AppNodes is MainServer's IP and port. This information is saved in MainServer.txt, where the IP saved, is the localhost "127.0.0.1". If any of the components run on a different machine, the MainServer.txt will need to be updated with the IP and port of the machine running the MainServer.
  * If the above configurations do not appear, they should be created in Intellij.
  * For video metadata we used tika library and shold be included to the project.Download here: https://downloads.apache.org/tika/
  * Here you can download some videos in order to try the app: https://drive.google.com/file/d/1gKP6veMRkRh6DBDfYzf6VKaoAlJv3Wp6/view?usp=sharing
