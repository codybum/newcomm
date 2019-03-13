import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Consumer {

    private Connection connection = null;

    public String agentpath;
    public String password;
    public String port;

    private Gson gson;
    private Type typeOfHashMap;


    public Consumer(String agentpath, String password, String port) {

    this.agentpath = agentpath;
    this.password = password;
    this.port = port;

    gson = new Gson();

        typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();


    }

    public  void mergeFiles(List<File> files, File into)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(into);
             BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
                //f.delete();
            }
        }
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
            transportConfiguration.getParams().put(TransportConstants.PORT_PROP_NAME, port);

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


            Map<String,Map<String,String>> dataPartMap = new HashMap<>();

            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {

                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String text = textMessage.getText();
                            text = text.replace("\r", "\n\t");
                            System.out.println("ActiveMQHL7Consumer(): [Received] \n\t" + text);
                            System.out.println("");

                            String dataMapString = textMessage.getStringProperty("datamap");
                            String dataName = textMessage.getStringProperty("dataname");

                            if((dataMapString != null) && (dataName != null)) {
                                Map<String, String> dataMap = gson.fromJson(dataMapString, typeOfHashMap);
                                dataPartMap.put(dataName,dataMap);

                                System.out.println("NEW DATA!!!");

                            }

                        } else if( message instanceof  BytesMessage) {

                            System.out.println("data");

                            String dataName = message.getStringProperty("dataname");
                            String dataPart = message.getStringProperty("datapart");

                            if((dataName != null) && (dataPart != null)) {

                                System.out.println("in loop");

                                Path journalPath = FileSystems.getDefault().getPath("journal-rec");
                                Files.createDirectories(journalPath);

                                File filePart = new File(journalPath.toAbsolutePath().toString(), dataPart);



                                byte[] data = new byte[(int) ((BytesMessage) message).getBodyLength()];
                                Files.write(filePart.toPath(), data);

                                String remoteFileSize = dataPartMap.get(dataName).get(dataPart);
                                dataPartMap.get(dataName).put(dataPart,remoteFileSize + "-1");

                                boolean isComplete = true;
                                for (Map.Entry<String, String> entry : dataPartMap.get(dataName).entrySet()) {
                                    //String key = entry.getKey();
                                    String value = entry.getValue();
                                    if(!value.endsWith("-1")) {
                                        isComplete = false;
                                    }
                                }

                                if(isComplete) {

                                    System.out.println("COMBINE FILES!!!!");
                                    List<File> filePartList = new ArrayList<>();

                                    File combinedFile = new File(journalPath.toAbsolutePath().toString(), dataName);

                                    for (String key : dataPartMap.get(dataName).keySet()) {
                                        File tmpFile = new File(journalPath.toAbsolutePath().toString(), key);
                                        if(tmpFile.exists()) {
                                            System.out.println(filePart.getName() + " exist");
                                            filePartList.add(tmpFile);
                                        } else {
                                            System.out.println(filePart.getName());
                                            System.exit(0);
                                        }

                                    }

                                    mergeFiles(filePartList,combinedFile);

                                    System.out.println("COMBINED FILE " + combinedFile.length());



                                }

                            }


                            /*
                            File outputFile = new File("huge_message_received.dat");

                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                            BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);

// This will block until the entire content is saved on disk
                            message.setObjectProperty("JMS_AMQ_SaveStream", bufferedOutput);

                            System.out.println("other message save");
                            */
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
