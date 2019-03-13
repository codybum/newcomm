import com.google.gson.Gson;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;

import javax.jms.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Producer {

    Connection connection = null;


    private String agentpath;
    private String password;
    private String port;

    private Gson gson;


    public Producer(String agentpath, String password, String port) {

        this.agentpath = agentpath;
        this.password = password;
        this.port = port;
        gson = new Gson();

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


            //while(true) {


                TextMessage message = session.createTextMessage("This is a text message from " + port);

                String dataName = UUID.randomUUID().toString();

                Map<String,String> dataMap = splitFile(dataName,"/Users/cody/Downloads/ubuntu-18.04.1.0-live-server-amd64.iso");

                message.setStringProperty("dataname", dataName);
                message.setStringProperty("datamap",gson.toJson(dataMap));

                producer.send(message);

                Thread.sleep(5000);

                //sending parts

                Path journalPath = FileSystems.getDefault().getPath("journal");

                for (String key : dataMap.keySet()) {

                    BytesMessage bytesMessage = session.createBytesMessage();
                    bytesMessage.setStringProperty("datapart",key);
                    bytesMessage.setStringProperty("dataname",dataName);

                    File filePart = new File(journalPath.toAbsolutePath().toString(), key);
                    byte[] fileContent = Files.readAllBytes(filePart.toPath());
                    bytesMessage.writeBytes(fileContent);
                    producer.send(bytesMessage);
                    filePart.delete();
                }


                //Thread.sleep(5000);

                /*
                String dataName = message.getStringProperty("datapart");

                            String dataMapString = textMessage.getStringProperty("datamap");
                            String dataMapName = textMessage.getStringProperty("datamapname");

                 */


                /*
                BytesMessage bigmessage = session.createBytesMessage();

                FileInputStream fileInputStream = new FileInputStream("/Users/cody/Downloads/ubuntu-18.04.1.0-live-server-amd64.iso");

                BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);

                bigmessage.setObjectProperty("JMS_AMQ_InputStream", bufferedInput);

                producer.send(bigmessage);
                */
            //}


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




    public Map<String,String> splitFile(String dataName, String fileName)  {

        Map<String,String> filePartNames = null;
        try {

            File f = new File(fileName);

            //try-with-resources to ensure closing stream
            FileInputStream fis = new FileInputStream(f);

            filePartNames = streamToSplitFile(dataName, fis);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return filePartNames;
    }


    public Map<String,String> streamToSplitFile(String dataName, InputStream is)  {

        Map<String,String> filePartNames = null;
        try {

            Path journalPath = FileSystems.getDefault().getPath("journal");
            Files.createDirectories(journalPath);

            filePartNames = new HashMap<>();

            int partCounter = 1;//I like to name parts from 001, 002, 003, ...
            //you can change it to 0 if you want 000, 001, ...

            int sizeOfFiles = 1024 * 1024 * 5;// 1MB
            byte[] buffer = new byte[sizeOfFiles];


            //String fileName = UUID.randomUUID().toString();

            //try-with-resources to ensure closing stream
            try (BufferedInputStream bis = new BufferedInputStream(is)) {

                int bytesAmount = 0;
                while ((bytesAmount = bis.read(buffer)) > 0) {
                    //write each chunk of data into separate file with different number in name
                    //String filePartName = String.format("%s.%03d", fileName, partCounter++);

                    String filePartName = dataName + "." + partCounter;
                    partCounter++;

                    File newFile = new File(journalPath.toAbsolutePath().toString(), filePartName);
                    try (FileOutputStream out = new FileOutputStream(newFile)) {
                        out.write(buffer, 0, bytesAmount);
                    }
                    filePartNames.put(filePartName, String.valueOf(newFile.length()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return filePartNames;
    }



}
