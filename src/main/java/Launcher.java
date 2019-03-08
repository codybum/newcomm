
import java.util.HashSet;

public class Launcher {


    public static void main(String args[]) {

        try {

            CertificateManager certificateManager = new CertificateManager("test");

            Broker broker = new Broker();
            broker.initServer();

            //Consumer consumer = new Consumer();
            //consumer.initConsumer();

            //Producer producer = new Producer();
            //producer.initProducer();


        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


}
