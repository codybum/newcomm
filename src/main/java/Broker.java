
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import java.util.HashSet;
import java.util.Map;


public class Broker {

    public String agentpath;
    public String password;

    public Broker(String agentpath, String password) {

    this.agentpath = agentpath;
    this.password = password;

    }

    public void initServer() {
        try {

            Configuration config = new ConfigurationImpl();

            HashSet<TransportConfiguration> transports = new HashSet<TransportConfiguration>();

            TransportConfiguration transportConfiguration = new TransportConfiguration(NettyAcceptorFactory.class.getName());

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

            transports.add(transportConfiguration);
            //transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

            config.setAcceptorConfigurations(transports);

            config.setSecurityEnabled(false);


            //NetworkBridge
            //NetworkConnector



            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            server.start();

            System.out.println(server.getActiveMQServer().isActive());



        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
