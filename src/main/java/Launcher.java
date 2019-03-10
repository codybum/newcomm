
import org.apache.activemq.artemis.core.remoting.impl.ssl.SSLSupport;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.HashSet;

public class Launcher {


    public static void main(String args[]) {

        try {

            String agentpath = "test";
            String password = "cody";

            CertificateManager certificateManager = new CertificateManager(agentpath,password);

            Broker broker = new Broker(agentpath, password);
            broker.initServer();

            Consumer consumer = new Consumer(agentpath, password);
            for(String c : consumer.getEnabledCipherSuites()) {
                System.out.println(c);
            }

            consumer.initConsumer();
            /*
            KeyStore ks = consumer.loadKeystore("PKCS12", agentpath + "-key.pkcs12", password);
            System.out.println(ks.getType() + " " + ks.getProvider().getName() + " " + ks.getKey("test",password.toCharArray()));


            */



            //Producer producer = new Producer();
            //producer.initProducer();


        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


}
