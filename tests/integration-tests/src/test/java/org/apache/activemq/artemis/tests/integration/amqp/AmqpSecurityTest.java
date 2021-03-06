/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.tests.integration.amqp;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.settings.HierarchicalRepository;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.server.impl.JMSServerManagerImpl;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.apache.activemq.transport.amqp.client.AmqpSession;
import org.apache.activemq.transport.amqp.client.AmqpValidator;
import org.apache.qpid.proton.engine.Delivery;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AmqpSecurityTest extends AmqpClientTestSupport {

   @Override
   protected ActiveMQServer createServer() throws Exception {
      ActiveMQServer server = createServer(true, true);
      ActiveMQJAASSecurityManager securityManager = (ActiveMQJAASSecurityManager) server.getSecurityManager();
      securityManager.getConfiguration().addUser("foo", "bar");
      securityManager.getConfiguration().addRole("foo", "none");
      HierarchicalRepository<Set<Role>> securityRepository = server.getSecurityRepository();
      HashSet<Role> value = new HashSet<>();
      value.add(new Role("none", false, true, true, true, true, true, true, true));
      securityRepository.addMatch(getTestName(), value);

      serverManager = new JMSServerManagerImpl(server);
      Configuration serverConfig = server.getConfiguration();
      serverConfig.getAddressesSettings().put("jms.queue.#", new AddressSettings().setAutoCreateJmsQueues(true).setDeadLetterAddress(new SimpleString("jms.queue.ActiveMQ.DLQ")));
      serverConfig.setSecurityEnabled(true);
      serverManager.start();
      server.start();
      return server;
   }

   @Test(timeout = 60000)
   public void testSendAndRejected() throws Exception {
      AmqpConnection connection = null;
      AmqpClient client = createAmqpClient("foo", "bar");
      CountDownLatch latch = new CountDownLatch(1);
      client.setValidator(new AmqpValidator() {
         @Override
         public void inspectDeliveryUpdate(Delivery delivery) {
            super.inspectDeliveryUpdate(delivery);
            if (!delivery.remotelySettled()) {
               markAsInvalid("delivery is not remotely settled");
            }
            latch.countDown();
         }
      });
      connection = addConnection(client.connect());
      AmqpSession session = connection.createSession();

      AmqpSender sender = session.createSender(getTestName());
      AmqpMessage message = new AmqpMessage();

      message.setMessageId("msg" + 1);
      message.setMessageAnnotation("serialNo", 1);
      message.setText("Test-Message");

      try {
         sender.send(message);
      } catch (IOException e) {
         //
      }
      assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      connection.getStateInspector().assertValid();
      connection.close();
   }
}
