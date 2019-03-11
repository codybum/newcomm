
import org.apache.activemq.artemis.core.remoting.impl.ssl.SSLSupport;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.HashSet;

public class Launcher {


    public static void main(String args[]) {

        try {

            String agentpath = "test";
            String password = "cody";
            String port = "32010";

            CertificateManager certificateManager = new CertificateManager(agentpath,password);

            Broker broker = new Broker(agentpath, password, port);
            broker.initServer();

            Broker broker2 = new Broker(agentpath, password, "32011");
            broker2.initServer();

/*
            Consumer consumer = new Consumer(agentpath, password, "32011");
            consumer.initConsumer();


            Producer producer = new Producer(agentpath, password, port);
            producer.initProducer();
*/

        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


}
