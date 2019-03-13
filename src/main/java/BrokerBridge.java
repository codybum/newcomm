import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.BridgeConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.cluster.Bridge;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class BrokerBridge {

    public String agentpath;
    public String password;
    public String port;
    public String remote_address;
    public String remote_port;

    public BrokerBridge(String agentpath, String password, String port, String remote_address, String remote_port) {

    this.agentpath = agentpath;
    this.password = password;
    this.port = port;
    this.remote_address = remote_address;
    this.remote_port = remote_port;

    }

    public void initServer() {
        try {

            Configuration config = new ConfigurationImpl();

            config.setBindingsDirectory("data/bind" + port);
            config.setJournalDirectory("data/jour" + port);
            config.setPagingDirectory("data/page" + port);
            config.setLargeMessagesDirectory("data/large" + port);


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
            transportConfiguration.getParams().put(TransportConstants.PORT_PROP_NAME, port);



            transports.add(transportConfiguration);

            config.setAcceptorConfigurations(transports);

            config.setSecurityEnabled(false);


/*
            config.setClusterUser("test");
            config.setClusterPassword("test");

            ClusterConnectionConfiguration clusterConnectionConfiguration = new ClusterConnectionConfiguration();
            clusterConnectionConfiguration.setName("word");
            List<ClusterConnectionConfiguration> clusterConnectionConfigurations = new ArrayList<>();
            clusterConnectionConfigurations.add(clusterConnectionConfiguration);
            config.addClusterConfiguration(clusterConnectionConfiguration);
*/

            TransportConfiguration transportConfigurationLocal = new TransportConfiguration(InVMConnectorFactory.class.getName());

            config.addConnectorConfiguration("local-connector",transportConfigurationLocal);

            BridgeConfiguration bridgeConfiguration = null;
            TransportConfiguration transportConfigurationRemote = null;

            if(remote_address != null) {

                transportConfigurationRemote = new TransportConfiguration(NettyConnectorFactory.class.getName());
                transportConfigurationRemote.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
                transportConfigurationRemote.getParams().put(TransportConstants.TRUST_ALL_PROP_NAME, true);
                transportConfigurationRemote.getParams().put(TransportConstants.VERIFY_HOST_PROP_NAME, false);
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME, "PKCS12");
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, agentpath + "-trust.pkcs12");
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, password);
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME, "PKCS12");
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PATH_PROP_NAME, agentpath + "-key.pkcs12");
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, password);
                transportConfigurationRemote.getParams().put(TransportConstants.HOST_PROP_NAME, remote_address);
                transportConfigurationRemote.getParams().put(TransportConstants.PORT_PROP_NAME, remote_port);

                config.addConnectorConfiguration("remote-connector",transportConfigurationRemote);
                

                bridgeConfiguration = new BridgeConfiguration();
                bridgeConfiguration.setName("MASTERBLASTER");
                bridgeConfiguration.setQueueName("exampleQueue");
                //bridgeConfiguration.setForwardingAddress("exampleQueue");
                bridgeConfiguration.setHA(false);
                bridgeConfiguration.setConfirmationWindowSize(1);

                List<String> staticConnectors = new ArrayList<>();
                staticConnectors.add("local-connector");
                staticConnectors.add("remote-connector");
                bridgeConfiguration.setStaticConnectors(staticConnectors);

                //start cluster

            }

            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            server.start();


            if(bridgeConfiguration != null) {



                server.getActiveMQServer().getClusterManager().deployBridge(bridgeConfiguration);
                Map<String, Bridge> bridgeMap = server.getActiveMQServer().getClusterManager().getBridges();

                for (Map.Entry<String, Bridge> entry : bridgeMap.entrySet()) {
                    String key = entry.getKey();
                    Bridge value = entry.getValue();

                    System.out.println("AL " + key + " " + value.getName());
                }

            }

            /*
            public void addClusterConnection(SimpleString name,
                                    TransportConfiguration[] tcConfigs,
                                    ClusterConnectionConfiguration config) {
      ServerLocatorImpl serverLocator = (ServerLocatorImpl) ActiveMQClient.createServerLocatorWithHA(tcConfigs);
      configAndAdd(name, serverLocator, config);
   }
             */



        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
