# Network Isolation

In case the server is isolated, say for a network failure, the server will be isolated for its peers on a network of brokers. If you are playing with replication the backup may think the backup failed and you may endup with two live nodes, what is called the split brain.

# Pinging the network

You may configure one more addresses on the broker.xml that are part of your network topology, that will be pinged through the life cycle of the server.

The server will stop itself until the network is back on such case.

If you execute the create command passing a -ping argument, you will create a default xml that is ready to be used with network checks:


```
./artemis create /myDir/myServer --ping 10.0.0.1
```


This XML part will be added to your broker.xml:

```xml
<!--
You can specify the NIC you want to use to verify if the network
 <network-check-NIC>theNickName</network-check-NIC>
-->

<!--
Use this to use an HTTP server to validate the network
 <network-check-URL-list>http://www.apache.org</network-check-URL-list> -->

<network-check-period>10000</network-check-period>
<network-check-timeout>1000</network-check-timeout>

<!-- this is a comma separated list, no spaces, just DNS or IPs
   it should accept IPV6

   Warning: Make sure you understand your network topology as this is meant to check if your network is up.
            Using IPs that could eventually disappear or be partially visible may defeat the purpose.
            You can use a list of multiple IPs, any successful ping will make the server OK to continue running -->
<network-check-list>10.0.0.1</network-check-list>

<!-- use this to customize the ping used for ipv4 addresses -->
<network-check-ping-command>ping -c 1 -t %d %s</network-check-ping-command>

<!-- use this to customize the ping used for ipv addresses -->
<network-check-ping6-command>ping6 -c 1 %2$s</network-check-ping6-command>

```


Once you lose connectivity towards 10.0.0.1 on the given example
, you will see see this output at the server:


```
09:49:24,562 WARN  [org.apache.activemq.artemis.core.server.NetworkHealthCheck] Ping Address /10.0.0.1 wasn't reacheable
09:49:36,577 INFO  [org.apache.activemq.artemis.core.server.NetworkHealthCheck] Network is unhealthy, stopping service ActiveMQServerImpl::serverUUID=04fd5dd8-b18c-11e6-9efe-6a0001921ad0
09:49:36,625 INFO  [org.apache.activemq.artemis.core.server] AMQ221002: Apache ActiveMQ Artemis Message Broker version 1.6.0 [04fd5dd8-b18c-11e6-9efe-6a0001921ad0] stopped, uptime 14.787 seconds
09:50:00,653 WARN  [org.apache.activemq.artemis.core.server.NetworkHealthCheck] ping: sendto: No route to host
09:50:10,656 WARN  [org.apache.activemq.artemis.core.server.NetworkHealthCheck] Host is down: java.net.ConnectException: Host is down
	at java.net.Inet6AddressImpl.isReachable0(Native Method) [rt.jar:1.8.0_73]
	at java.net.Inet6AddressImpl.isReachable(Inet6AddressImpl.java:77) [rt.jar:1.8.0_73]
	at java.net.InetAddress.isReachable(InetAddress.java:502) [rt.jar:1.8.0_73]
	at org.apache.activemq.artemis.core.server.NetworkHealthCheck.check(NetworkHealthCheck.java:295) [artemis-commons-1.6.0-SNAPSHOT.jar:1.6.0-SNAPSHOT]
	at org.apache.activemq.artemis.core.server.NetworkHealthCheck.check(NetworkHealthCheck.java:276) [artemis-commons-1.6.0-SNAPSHOT.jar:1.6.0-SNAPSHOT]
	at org.apache.activemq.artemis.core.server.NetworkHealthCheck.run(NetworkHealthCheck.java:244) [artemis-commons-1.6.0-SNAPSHOT.jar:1.6.0-SNAPSHOT]
	at org.apache.activemq.artemis.core.server.ActiveMQScheduledComponent$2.run(ActiveMQScheduledComponent.java:189) [artemis-commons-1.6.0-SNAPSHOT.jar:1.6.0-SNAPSHOT]
	at org.apache.activemq.artemis.core.server.ActiveMQScheduledComponent$3.run(ActiveMQScheduledComponent.java:199) [artemis-commons-1.6.0-SNAPSHOT.jar:1.6.0-SNAPSHOT]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [rt.jar:1.8.0_73]
	at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:308) [rt.jar:1.8.0_73]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:180) [rt.jar:1.8.0_73]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:294) [rt.jar:1.8.0_73]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [rt.jar:1.8.0_73]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [rt.jar:1.8.0_73]
	at java.lang.Thread.run(Thread.java:745) [rt.jar:1.8.0_73]

```

Once you re establish your network connections towards the configured check list:

```
09:53:23,461 INFO  [org.apache.activemq.artemis.core.server.NetworkHealthCheck] Network is healthy, starting service ActiveMQServerImpl::
09:53:23,462 INFO  [org.apache.activemq.artemis.core.server] AMQ221000: live Message Broker is starting with configuration Broker Configuration (clustered=false,journalDirectory=./data/journal,bindingsDirectory=./data/bindings,largeMessagesDirectory=./data/large-messages,pagingDirectory=./data/paging)
09:53:23,462 INFO  [org.apache.activemq.artemis.core.server] AMQ221013: Using NIO Journal
09:53:23,462 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-server]. Adding protocol support for: CORE
09:53:23,463 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-amqp-protocol]. Adding protocol support for: AMQP
09:53:23,463 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-hornetq-protocol]. Adding protocol support for: HORNETQ
09:53:23,463 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-mqtt-protocol]. Adding protocol support for: MQTT
09:53:23,464 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-openwire-protocol]. Adding protocol support for: OPENWIRE
09:53:23,464 INFO  [org.apache.activemq.artemis.core.server] AMQ221043: Protocol module found: [artemis-stomp-protocol]. Adding protocol support for: STOMP
09:53:23,541 INFO  [org.apache.activemq.artemis.core.server] AMQ221003: Deploying queue jms.queue.DLQ
09:53:23,541 INFO  [org.apache.activemq.artemis.core.server] AMQ221003: Deploying queue jms.queue.ExpiryQueue
09:53:23,549 INFO  [org.apache.activemq.artemis.core.server] AMQ221020: Started Acceptor at 0.0.0.0:61616 for protocols [CORE,MQTT,AMQP,STOMP,HORNETQ,OPENWIRE]
09:53:23,550 INFO  [org.apache.activemq.artemis.core.server] AMQ221020: Started Acceptor at 0.0.0.0:5445 for protocols [HORNETQ,STOMP]
09:53:23,554 INFO  [org.apache.activemq.artemis.core.server] AMQ221020: Started Acceptor at 0.0.0.0:5672 for protocols [AMQP]
09:53:23,555 INFO  [org.apache.activemq.artemis.core.server] AMQ221020: Started Acceptor at 0.0.0.0:1883 for protocols [MQTT]
09:53:23,556 INFO  [org.apache.activemq.artemis.core.server] AMQ221020: Started Acceptor at 0.0.0.0:61613 for protocols [STOMP]
09:53:23,556 INFO  [org.apache.activemq.artemis.core.server] AMQ221007: Server is now live
09:53:23,556 INFO  [org.apache.activemq.artemis.core.server] AMQ221001: Apache ActiveMQ Artemis Message Broker version 1.6.0 [0.0.0.0, nodeID=04fd5dd8-b18c-11e6-9efe-6a0001921ad0] 
```

# Warning

> Make sure you understand your network topology as this is meant to validate your network.
> Using IPs that could eventually disappear or be partially visible may defeat the purpose.
> You can use a list of multiple IPs. Any successful ping will make the server OK to continue running
