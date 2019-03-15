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
import javax.jms.Queue;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

public class Producer {

    Connection connection = null;


    private String agentpath;
    private String password;
    private String port;

    private Gson gson;

    private FileUtils fileUtils;

    public Producer(String agentpath, String password, String port) {

        this.agentpath = agentpath;
        this.password = password;
        this.port = port;
        gson = new Gson();

        String journalDirPath = FileSystems.getDefault().getPath("journal").toAbsolutePath().toString();
        fileUtils = new FileUtils(journalDirPath);

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

                FileObject fileObjectIn = fileUtils.createFileObject("/Users/vcbumg2/Downloads/OpenStack_in_Action_v4_MEAP.pdf");

                List<FileObject> fileObjects = new ArrayList<>();
                fileObjects.add(fileObjectIn);



                for(FileObject fileObject : fileObjects) {

                    message.setStringProperty("fileobjects", gson.toJson(fileObjects));

                    producer.send(message);

                    System.out.println("SEND TEXT MESSAGE");

                    //Thread.sleep(5000);

                    Path filePath = Paths.get(fileUtils.getJournalPath().toAbsolutePath().toString() + "/" + fileObject.getDataName());

                    for (String parList : fileObject.getOrderedPartList()) {

                        BytesMessage bytesMessage = session.createBytesMessage();
                        bytesMessage.setStringProperty("datapart", parList);
                        bytesMessage.setStringProperty("dataname", fileObject.getDataName());


                        File filePart = new File(filePath.toAbsolutePath().toString(), parList);

                        System.out.println("READING FILE TO MESSAGE : " + filePart.getAbsolutePath() + " " + parList);

                        byte[] fileContent = Files.readAllBytes(filePart.toPath());
                        bytesMessage.writeBytes(fileContent);
                        producer.send(bytesMessage);
                        filePart.delete();
                    }

                    filePath.toFile().delete();
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
