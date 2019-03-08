
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import java.util.HashSet;


public class Broker {

    public Broker() {



    }

    public void initServer() {
        try {

            Configuration config = new ConfigurationImpl();

            HashSet<TransportConfiguration> transports = new HashSet<TransportConfiguration>();

            TransportConfiguration transportConfiguration = new TransportConfiguration(NettyAcceptorFactory.class.getName());

            /*
            @Parameterized.Parameters(name = "storeType={0}")
   public static Collection getParameters() {
      return Arrays.asList(new Object[][]{{"JCEKS"}, {"JKS"}, {"PKCS12"}});
   }
             */

            transportConfiguration.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
            transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME, "jks");
            transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, "client");
            //transportConfiguration.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);


            transports.add(transportConfiguration);
            //transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

            config.setAcceptorConfigurations(transports);

            config.setSecurityEnabled(false);


            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            server.start();

            System.out.println(server.getActiveMQServer().isActive());



        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
