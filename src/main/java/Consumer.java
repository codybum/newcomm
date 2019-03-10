import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.remoting.impl.ssl.SSLSupport;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.utils.ClassloadingUtil;

import javax.jms.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.Map;

public class Consumer {

    Connection connection = null;

    public String agentpath;
    public String password;

    public Consumer(String agentpath, String password) {

    this.agentpath = agentpath;
    this.password = password;

    }

    private URL findResource(final String resourceName) {
        return (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run() {
                return ClassloadingUtil.findResource(resourceName);
            }
        });
    }

    private URL validateStoreURL(String storePath) throws Exception {
        assert storePath != null;

        try {
            return new URL(storePath);
        } catch (MalformedURLException var4) {
            File file = new File(storePath);
            if (file.exists() && file.isFile()) {
                return file.toURI().toURL();
            } else {
                URL url = findResource(storePath);
                if (url != null) {
                    return url;
                } else {
                    throw new Exception("Failed to find a store at " + storePath);
                }
            }
        }
    }

    public KeyStore loadKeystore(String keystoreProvider, String keystorePath, String keystorePassword) throws Exception {
        KeyStore ks = KeyStore.getInstance(keystoreProvider);
        InputStream in = null;

        try {
            if (keystorePath != null) {
                URL keystoreURL = validateStoreURL(keystorePath);
                in = keystoreURL.openStream();
            }

            ks.load(in, keystorePassword == null ? null : keystorePassword.toCharArray());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException var11) {
                }
            }

        }

        return ks;
    }

    public String[] getEnabledCipherSuites() throws Exception
    {
        SSLContext context = SSLSupport.createContext("PKCS12", agentpath + "-key.pkcs12", password, "PKCS12", agentpath + "-trust.pkcs12", password);
//                 context = SSLSupport.createContext(this.keyStoreProvider, this.keyStorePath, this.keyStorePassword, this.trustStoreProvider, this.trustStorePath, this.trustStorePassword, this.crlPath);

        SSLEngine engine = context.createSSLEngine();
        return engine.getEnabledCipherSuites();
    }



    public void initConsumer() {
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

            for (Map.Entry<String, Object> entry : transportConfiguration.getParams().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key + ":" + value);
            }

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
