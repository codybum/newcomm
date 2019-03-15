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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.MessageDigest;
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
    private Type typeOfListFileObject;

    private Map<String,FileObject> fileObjectMap;

    private FileUtils fileUtils;

    public Consumer(String agentpath, String password, String port) {

    this.agentpath = agentpath;
    this.password = password;
    this.port = port;

    fileObjectMap = new HashMap<>();

    gson = new Gson();

        typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();
        typeOfListFileObject = new TypeToken<List<FileObject>>() { }.getType();

        String journalDirPath = FileSystems.getDefault().getPath("journal-rec").toAbsolutePath().toString();
        fileUtils = new FileUtils(journalDirPath);
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


    private boolean registerIncomingFiles(String fileobjectString) {
        boolean isRegistered = false;
        try {

            List<FileObject> fileObjects = gson.fromJson(fileobjectString,typeOfListFileObject);

            for(FileObject fileObject : fileObjects) {
                System.out.println("Registered Data: " + fileObject.getDataName());
                fileObjectMap.put(fileObject.getDataName(), fileObject);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRegistered;
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




            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {

                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String text = textMessage.getText();
                            text = text.replace("\r", "\n\t");
                            System.out.println("ActiveMQHL7Consumer(): [Received] \n\t" + text);
                            System.out.println("");

                            String fileobjectString = textMessage.getStringProperty("fileobjects");
                            //String dataName = textMessage.getStringProperty("dataname");

                            //if((dataMapString != null) && (dataName != null)) {
                            if(fileobjectString != null) {
                                if(registerIncomingFiles(fileobjectString)) {
                                    System.out.println("NEW FILE PAYLOAD");
                                }
                            }

                        } else if( message instanceof  BytesMessage) {

                            System.out.println("data");

                            String dataName = message.getStringProperty("dataname");
                            String dataPart = message.getStringProperty("datapart");

                            if((dataName != null) && (dataPart != null)) {

                                if(fileObjectMap.containsKey(dataName)) {

                                    System.out.println("in loop");

                                    //Path journalPath = FileSystems.getDefault().getPath("journal-rec");
                                    Files.createDirectories(fileUtils.getJournalPath());

                                    Path filePath = Paths.get(fileUtils.getJournalPath().toAbsolutePath().toString() + "/" + dataName);
                                    Files.createDirectories(filePath);

                                    File filePart = new File(filePath.toAbsolutePath().toString(), dataPart);

                                    byte[] data = new byte[(int) ((BytesMessage) message).getBodyLength()];
                                    ((BytesMessage) message).readBytes(data);

                                    Files.write(filePart.toPath(), data);

                                    String filePartMD5Hash = fileUtils.getMD5(filePart.getAbsolutePath());


                                    MessageDigest m= MessageDigest.getInstance("MD5");
                                    m.update(data);
                                    String md5Hash = new BigInteger(1,m.digest()).toString(16);

                                    System.out.println("INCOMING HASH: " + filePartMD5Hash + " " + md5Hash);

                                    fileObjectMap.get(dataName).setDestFilePart(dataPart,filePartMD5Hash);

                                    boolean isComplete = fileObjectMap.get(dataName).isFilePartComplete();

                                    if (isComplete) {

                                        List<String> orderedFilePartNameList = fileObjectMap.get(dataName).getOrderedPartList();
                                        String combinedFileName = fileObjectMap.get(dataName).getFileName();
                                        String combinedFileHash = fileObjectMap.get(dataName).getFileMD5Hash();

                                        List<File> orderedFilePartList = new ArrayList<>();


                                        File combinedFile = new File(filePath.toAbsolutePath().toString(), combinedFileName);

                                        for(String filePartName : orderedFilePartNameList) {

                                            File partFile = new File(filePath.toAbsolutePath().toString(), filePartName);
                                            if(partFile.exists()) {
                                                System.out.println("File Part : " + partFile.getName());
                                                orderedFilePartList.add(partFile);
                                            } else {
                                                System.out.println("File Part : " + partFile.getName() + " DOES NOT EXIST");
                                            }
                                        }


                                        fileUtils.mergeFiles(orderedFilePartList, combinedFile, true);
                                        if(combinedFile.exists()) {

                                            String localCombinedFilePath = combinedFile.getAbsolutePath();
                                            String localCombinedFileHash = fileUtils.getMD5(localCombinedFilePath);
                                            System.out.println("File: " + localCombinedFileHash + " original_hash:" + combinedFileHash + " local_hash:" + localCombinedFileHash);

                                            if(combinedFileHash.equals(localCombinedFileHash)) {
                                                System.out.println("WE HAVE A FILE!!!");
                                            }

                                        } else {
                                            System.out.println("ERROR COMBINING FILE : " + combinedFileHash);
                                        }

                                    }
                                }

                            }

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
