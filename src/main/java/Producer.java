import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;

import javax.jms.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Producer {

    Connection connection = null;


    private String agentpath;
    private String password;
    private String port;


    public Producer(String agentpath, String password, String port) {

        this.agentpath = agentpath;
        this.password = password;
        this.port = port;

    }

    public void initProducer() {
        try {


            TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());

            transportConfiguration.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
            transportConfiguration.getParams().put(TransportConstants.TRUST_ALL_PROP_NAME, true);
            transportConfiguration.getParams().put(TransportConstants.VERIFY_HOST_PROP_NAME, false);
            transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME, "PKCS12");
            transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, agentpath + "-trust.pkcs12");
            transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, password);
            transportConfiguration.getParams().put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME, "PKCS12");
            transportConfiguration.getParams().put(TransportConstants.KEYSTORE_PATH_PROP_NAME, agentpath + "-key.pkcs12");
            transportConfiguration.getParams().put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, password);
            transportConfiguration.getParams().put(TransportConstants.PORT_PROP_NAME, port);


            //TransportConfiguration transportConfiguration = new TransportConfiguration(InVMConnectorFactory.class.getName());

            ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF,transportConfiguration);





            //ConnectionFactory cf = new ActiveMQConnectionFactory();

            connection = cf.createConnection();
            connection.start();

            ActiveMQSession session = (ActiveMQSession)connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue("exampleQueue");

            MessageProducer producer = session.createProducer(queue);


            while(true) {
                TextMessage message = session.createTextMessage("This is a text message");

                System.out.println("Sent message: " + message.getText());

                producer.send(message);

                Thread.sleep(5000);

                /*
                BytesMessage bigmessage = session.createBytesMessage();

                FileInputStream fileInputStream = new FileInputStream("/Users/cody/Downloads/ubuntu-18.04.1.0-live-server-amd64.iso");

                BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);

                bigmessage.setObjectProperty("JMS_AMQ_InputStream", bufferedInput);

                producer.send(bigmessage);
                */
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


}
