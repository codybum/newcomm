import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Consumer {

    Connection connection = null;


    public Consumer() {



    }

    public void initConsumer() {
        try {


            TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());

            //TransportConfiguration transportConfiguration = new TransportConfiguration(InVMConnectorFactory.class.getName());

            ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF,transportConfiguration);




            connection = cf.createConnection();

            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue("exampleQueue");

            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {

                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String text = textMessage.getText();
                            text = text.replace("\r", "\n\t");
                            System.out.println("ActiveMQHL7Consumer(): [Received] \n\t" + text);
                            System.out.println("");
                        } else if( message instanceof  BytesMessage) {

                            System.out.println("other message");


                            File outputFile = new File("huge_message_received.dat");

                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                            BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);

// This will block until the entire content is saved on disk
                            message.setObjectProperty("JMS_AMQ_SaveStream", bufferedOutput);

                            System.out.println("other message save");
                        }

                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
