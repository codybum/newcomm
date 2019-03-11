
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.BridgeConfiguration;
import org.apache.activemq.artemis.core.config.ClusterConnectionConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.ConnectorServiceConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.persistence.StorageManager;
import org.apache.activemq.artemis.core.postoffice.PostOffice;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.ConnectorService;
import org.apache.activemq.artemis.core.server.ConnectorServiceFactory;
import org.apache.activemq.artemis.core.server.cluster.Bridge;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.server.impl.ConnectorsService;
import org.apache.activemq.artemis.spi.core.remoting.ConnectorFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;


public class Broker {

    public String agentpath;
    public String password;
    public String port;

    public Broker(String agentpath, String password, String port) {

    this.agentpath = agentpath;
    this.password = password;
    this.port = port;

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


            for (Map.Entry<String, Object> entry : transportConfiguration.getParams().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key + ":" + value);
            }

            transports.add(transportConfiguration);
            //transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

            config.setAcceptorConfigurations(transports);

            config.setSecurityEnabled(false);

            config.setClusterUser("test");
            config.setClusterPassword("test");

            ClusterConnectionConfiguration clusterConnectionConfiguration = new ClusterConnectionConfiguration();
            clusterConnectionConfiguration.setName("test");
            List<ClusterConnectionConfiguration> clusterConnectionConfigurations = new ArrayList<>();
            clusterConnectionConfigurations.add(clusterConnectionConfiguration);
            config.addClusterConfiguration(clusterConnectionConfiguration);


            BridgeConfiguration bridgeConfiguration = null;
            if(port.equals("32011")) {

                System.out.println("WOOT");

                TransportConfiguration transportConfigurationRemote = new TransportConfiguration(NettyConnectorFactory.class.getName());
                transportConfigurationRemote.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
                transportConfigurationRemote.getParams().put(TransportConstants.TRUST_ALL_PROP_NAME, true);
                transportConfigurationRemote.getParams().put(TransportConstants.VERIFY_HOST_PROP_NAME, false);
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME, "PKCS12");
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, agentpath + "-trust.pkcs12");
                transportConfigurationRemote.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, password);
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME, "PKCS12");
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PATH_PROP_NAME, agentpath + "-key.pkcs12");
                transportConfigurationRemote.getParams().put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, password);

                transportConfigurationRemote.getParams().put(TransportConstants.HOST_PROP_NAME, "localhost");


                transportConfigurationRemote.getParams().put(TransportConstants.PORT_PROP_NAME, "32010");

                //Map<String, TransportConfiguration> connectorMap = new HashMap<>();
                //connectorMap.put("remote-connector", transportConfigurationRemote);
                //config.setConnectorConfigurations(connectorMap);


                //config.addConnectorConfiguration("remote-connector","tcp://localhost:32011?sslEnabled=true;key-store-password=cody;key-store-path=/Users/cody/IdeaProjects/newcomm/test-key.pkcs12");

                config.addConnectorConfiguration("remote-connector",transportConfigurationRemote);


                bridgeConfiguration = new BridgeConfiguration();
                bridgeConfiguration.setName("MASTERBLASTER");
                bridgeConfiguration.setQueueName("exampleQueue");
                bridgeConfiguration.setForwardingAddress("localhost");
                bridgeConfiguration.setHA(false);

                //List<BridgeConfiguration> bridgeConfigurations = new ArrayList<>();
                //bridgeConfigurations.add(bridgeConfiguration);

                //config.setBridgeConfigurations(bridgeConfigurations);


                List<String> staticConnectors = new ArrayList<>();
                staticConnectors.add("remote-connector");
                bridgeConfiguration.setStaticConnectors(staticConnectors);

                List<BridgeConfiguration> bridgeConfigurations = new ArrayList<>();
                bridgeConfigurations.add(bridgeConfiguration);

                //config.setBridgeConfigurations(bridgeConfigurations);

            }
            //NetworkBridge
            //NetworkConnector


            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            /*
            final String testAddress = "testAddress";
            final String queueName0 = "queue0";
            final String forwardAddress = "forwardAddress";
            final String queueName1 = "queue1";

            final int messageSize = 1024 * 1024 * 5;

            final int numMessages = 10;

            ArrayList<String> connectorConfig = new ArrayList<String>();
            connectorConfig.add(transportConfiguration.getName());
            BridgeConfiguration bridgeConfiguration = new BridgeConfiguration().setName("bridge1").setQueueName(queueName0).setForwardingAddress(forwardAddress).setRetryInterval(1000).setReconnectAttemptsOnSameNode(-1).setUseDuplicateDetection(false).setConfirmationWindowSize(numMessages * messageSize / 2).setStaticConnectors(connectorConfig);

            List<BridgeConfiguration> bridgeConfigs = new ArrayList<BridgeConfiguration>();
            bridgeConfigs.add(bridgeConfiguration);
            server.getActiveMQServer().getConfiguration().setBridgeConfigurations(bridgeConfigs);
            */



            server.start();


            if(bridgeConfiguration != null) {

                System.out.println("START");

//                server.getActiveMQServer().getClusterManager().deploy();
                server.getActiveMQServer().getClusterManager().deployBridge(bridgeConfiguration);
                Map<String, Bridge> bridgeMap = server.getActiveMQServer().getClusterManager().getBridges();

                for (Map.Entry<String, Bridge> entry : bridgeMap.entrySet()) {
                    String key = entry.getKey();
                    Bridge value = entry.getValue();

                    System.out.println("AL " + key + " " + value.getName());


                }

            }

            //server.getActiveMQServer().getConnectorsService().start();

            //Map<String,ConnectorService> connectorServiceMap = server.getActiveMQServer().getConnectorsService().getConnectors();




            //ConnectorServiceConfiguration connectorServiceConfiguration = new ConnectorServiceConfiguration();
            //connectorServiceConfiguration.setFactoryClassName(NettyConnectorFactory.class.getName());
            //connectorServiceConfiguration.setName("test");

            /*
            Map<String, Object> connectionParams = new HashMap<String, Object>();

            connectionParams.put(org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME,
                    32010);

            server.getActiveMQServer().getActiveMQServerControl().createConnectorService("test", NettyConnectorFactory.class.getName(), connectionParams);
            */

            //server.getActiveMQServer().getConnectorsService().start();



            //server.getActiveMQServer().getConnectorsService().createService();
            //List<ConnectorService> connectorServices = server.getActiveMQServer().getServiceRegistry().getConnectorServices()



              //      ConnectorServiceFactory connectorServiceFactory = new ConnectorsService(config, null, null, null, server.getActiveMQServer().getServiceRegistry());
            //server.getActiveMQServer().getConnectorsService().createService(connectorServiceConfiguration,);
            //server.getActiveMQServer().getConnectorsService().

            //ConnectorServiceFactory


            //ConnectorServiceConfiguration connectorServiceConfiguration = new ConnectorServiceConfiguration().setFactoryClassName(null).setParams(new HashMap<String, Object>()).setName("myfact");
            // Creates a fake connector service factory that returns the fake connector service object
            //ConnectorService connectorService = server.getActiveMQServer().getConnectorsService();
            //FakeConnectorServiceFactory connectorServiceFactory = new FakeConnectorServiceFactory();
            //serviceRegistry.addConnectorService(connectorServiceFactory, connectorServiceConfiguration);
            //server.getActiveMQServer().getConnectorsService().createService();
            //ConnectorsService connectorsService = new ConnectorsService(configuration, null, null, null, serviceRegistry);
            //connectorsService.start();
            //assertTrue(connectorsService.getConnectors().size() == 1);
            //assertTrue(connectorsService.getConnectors().contains(connectorServiceFactory.getConnectorService()));
        //}


            //server.getActiveMQServer().getConnectorsService().createService();


        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
