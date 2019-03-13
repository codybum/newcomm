
import org.apache.activemq.artemis.core.remoting.impl.ssl.SSLSupport;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Launcher {




    public static void main(String args[]) {

        try {


            /*
            Map<String,String> fileParts = splitFile("/Users/cody/Downloads/ubuntu-18.04.1.0-live-server-amd64.iso");

            for (Map.Entry<String, String> entry : fileParts.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key);
            }

            */





            int mode = 0;
            String agentpath = "test";
            String password = "cody";
            String port = "32010";
            String remote_address = "localhost";
            String remote_port = "32010";

            if(args.length > 1) {
                if (args[0] != null) {
                    mode = Integer.parseInt(args[0]);
                    if (args[1] != null) {
                        port = args[1];
                    }
                    if (args[2] != null) {
                        remote_address = args[2];
                    }
                    if (args[3] != null) {
                        remote_port = args[3];
                    }
                }
            }

            System.out.println("local_port: [" + port + "] remote_address: [" + remote_address + "] remote_port: [" + remote_port + "]");


            CertificateManager certificateManager = new CertificateManager(agentpath,password);



            if(mode == 0) {

                Broker broker = new Broker(agentpath, password, port, null, null);
                broker.initServer();

                Consumer consumer = new Consumer(agentpath, password, port);
                consumer.initConsumer();

                Producer producer = new Producer(agentpath, password, port);
                producer.initProducer();


            } else if(mode == 1) {


                Broker broker = new Broker(agentpath, password, port, remote_address, remote_port);
                broker.initServer();

                Consumer consumer = new Consumer(agentpath, password, port);
                consumer.initConsumer();

                //Producer producer = new Producer(agentpath, password, port);
                //producer.initProducer();


            }





        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


}
