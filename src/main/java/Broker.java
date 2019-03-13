import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.BridgeConfiguration;
import org.apache.activemq.artemis.core.config.ClusterConnectionConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.cluster.ClusterController;
import org.apache.activemq.artemis.core.server.cluster.ClusterManager;
import org.apache.activemq.artemis.core.server.cluster.impl.ClusterConnectionImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class Broker {

    public String agentpath;
    public String password;
    public String port;
    public String remote_address;
    public String remote_port;

    public Broker(String agentpath, String password, String port, String remote_address, String remote_port) {

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
            //transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

            config.setAcceptorConfigurations(transports);

            config.setSecurityEnabled(false);


            TransportConfiguration transportConfigurationLocal = new TransportConfiguration(InVMConnectorFactory.class.getName());
            config.addConnectorConfiguration("local-connector", transportConfigurationLocal);

            ClusterConnectionConfiguration clusterConnectionConfiguration = null;
            clusterConnectionConfiguration = new ClusterConnectionConfiguration();
            clusterConnectionConfiguration.setName("test");
            //clusterConnectionConfiguration.setStaticConnectors(staticConnectors);
            clusterConnectionConfiguration.setAddress("jms");
            clusterConnectionConfiguration.setConnectorName("local-connector");
            clusterConnectionConfiguration.setConnectionTTL(5000);
            clusterConnectionConfiguration.setMinLargeMessageSize(50000);
            clusterConnectionConfiguration.setCallTimeout(5000);
            clusterConnectionConfiguration.setRetryInterval(500);
            clusterConnectionConfiguration.setRetryIntervalMultiplier(1.0);
            clusterConnectionConfiguration.setMaxRetryInterval(5000);
            clusterConnectionConfiguration.setInitialConnectAttempts(1);
            clusterConnectionConfiguration.setDuplicateDetection(true);
            clusterConnectionConfiguration.setMaxHops(10);
            clusterConnectionConfiguration.setConfirmationWindowSize(32000);
            clusterConnectionConfiguration.setCallFailoverTimeout(30000);
            clusterConnectionConfiguration.setClusterNotificationInterval(1000);
            clusterConnectionConfiguration.setClusterNotificationAttempts(2);
            //clusterConnectionConfiguration2.setDiscoveryGroupName("my-discovery-group");
            config.addClusterConfiguration(clusterConnectionConfiguration);

            config.setClusterUser("test");
            config.setClusterPassword("test");

            TransportConfiguration transportConfigurationRemote = null;

            if (remote_address != null) {


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

                config.addConnectorConfiguration("remote-connector", transportConfigurationRemote);

            }

            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            server.start();


            if (remote_address != null) {


                ClusterConnectionConfiguration clusterConnectionConfiguration2 = new ClusterConnectionConfiguration();
                clusterConnectionConfiguration2.setName("test");
                //clusterConnectionConfiguration2.setStaticConnectors(staticConnectors);
                clusterConnectionConfiguration2.setAddress("jms");
                clusterConnectionConfiguration2.setConnectorName("remote-connector");
                clusterConnectionConfiguration2.setConnectionTTL(5000);
                clusterConnectionConfiguration2.setMinLargeMessageSize(50000);
                clusterConnectionConfiguration2.setCallTimeout(5000);
                clusterConnectionConfiguration2.setRetryInterval(500);
                clusterConnectionConfiguration2.setRetryIntervalMultiplier(1.0);
                clusterConnectionConfiguration2.setMaxRetryInterval(5000);
                clusterConnectionConfiguration2.setInitialConnectAttempts(1);
                clusterConnectionConfiguration2.setDuplicateDetection(true);
                clusterConnectionConfiguration2.setMaxHops(10);
                clusterConnectionConfiguration2.setConfirmationWindowSize(32000);
                clusterConnectionConfiguration2.setCallFailoverTimeout(30000);
                clusterConnectionConfiguration2.setClusterNotificationInterval(1000);
                clusterConnectionConfiguration2.setClusterNotificationAttempts(2);
                //clusterConnectionConfiguration2.setDiscoveryGroupName("my-discovery-group");

                TransportConfiguration[] transportConfigurations = new TransportConfiguration[1];
                transportConfigurations[0] = transportConfigurationRemote;


                ClusterManager cm = server.getActiveMQServer().getClusterManager();


                if (cm != null) {

                    ClusterController CC = cm.getClusterController();




                    if (CC != null) {

                        SimpleString ss = SimpleString.toSimpleString("word");

                        //ClusterConnectionImpl clusterConnection;
                        //clusterConnection = new ClusterConnectionImpl(cm, transportConfigurations, transportConfigurationRemote, new SimpleString(config.getName()), new SimpleString(config.getAddress()), config.getMinLargeMessageSize(), config.getClientFailureCheckPeriod(), config.getConnectionTTL(), config.getRetryInterval(), config.getRetryIntervalMultiplier(), config.getMaxRetryInterval(), config.getInitialConnectAttempts(), config.getReconnectAttempts(), config.getCallTimeout(), config.getCallFailoverTimeout(), config.isDuplicateDetection(), config.getMessageLoadBalancingType(), config.getConfirmationWindowSize(), config.getProducerWindowSize(), executorFactory, server, postOffice, managementService, scheduledExecutor, config.getMaxHops(), nodeManager, server.getConfiguration().getClusterUser(), server.getConfiguration().getClusterPassword(), config.isAllowDirectConnectionsOnly(), config.getClusterNotificationInterval(), config.getClusterNotificationAttempts());
                        //clusterConnection = new ClusterConnectionImpl(cm, transportConfigurations, transportConfigurationRemote, new SimpleString(clusterConnectionConfiguration2.getName()), new SimpleString(clusterConnectionConfiguration2.getAddress()), clusterConnectionConfiguration2.getMinLargeMessageSize(), clusterConnectionConfiguration2.getClientFailureCheckPeriod(), clusterConnectionConfiguration2.getConnectionTTL(), clusterConnectionConfiguration2.getRetryInterval(), clusterConnectionConfiguration2.getRetryIntervalMultiplier(), clusterConnectionConfiguration2.getMaxRetryInterval(), clusterConnectionConfiguration2.getInitialConnectAttempts(), clusterConnectionConfiguration2.getReconnectAttempts(), clusterConnectionConfiguration2.getCallTimeout(), clusterConnectionConfiguration2.getCallFailoverTimeout(), clusterConnectionConfiguration2.isDuplicateDetection(), clusterConnectionConfiguration2.getMessageLoadBalancingType(), clusterConnectionConfiguration2.getConfirmationWindowSize(), clusterConnectionConfiguration2.getProducerWindowSize(), executorFactory, server, postOffice, managementService, scheduledExecutor, clusterConnectionConfiguration2.getMaxHops(), nodeManager, config.getClusterUser(), config.getClusterPassword(), clusterConnectionConfiguration2.isAllowDirectConnectionsOnly(), clusterConnectionConfiguration2.getClusterNotificationInterval(), clusterConnectionConfiguration2.getClusterNotificationAttempts());


                        //CC.addClusterTopologyListenerForReplication();
                        CC.addClusterConnection(ss,transportConfigurations,clusterConnectionConfiguration);
                        ServerLocator serverLocator = CC.getServerLocator(ss);

                        System.out.println("Is Closed: " + serverLocator.isClosed());
                        serverLocator.initialize();
                        ClientSessionFactory clientSessionFactory = serverLocator.createSessionFactory();
                        ClientSession clientSession = clientSessionFactory.createSession();
                        clientSession.createConsumer("exampleQueue");
                        System.out.println("Is Closed: " + serverLocator.getGroupID());

                        //CC.connectToNode(transportConfigurationRemote);

                        System.out.println("Describe: " + server.getActiveMQServer().getClusterManager().describe());


                        //CC.connectToNode(transportConfigurationRemote);

                                    /*
                                    HashMap<String, Object> map = new HashMap<String, Object>();
                                    map.put("host", "myhost");
                                    map.put("port", "61616");
                                    TransportConfiguration server1 = new TransportConfiguration(NettyConnectorFactory.class.getName(), map);
                                    HashMap<String, Object> map2 = new HashMap<String, Object>();
                                    map2.put("host", "myhost2");
                                    map2.put("port", "61617");
                                    TransportConfiguration server2 = new TransportConfiguration(NettyConnectorFactory.class.getName(), map2);
                                    */

                                    /*
                                    ServerLocator locator = ActiveMQClient.createServerLocatorWithHA(transportConfigurationLocal, transportConfigurationRemote);
                                    ClientSessionFactory factory = locator.createSessionFactory();
                                    ClientSession session = factory.createSession();
                                    System.out.println(session.getVersion());
                                    */
                                    /*
                                   CC.addClusterConnection(ss,transportConfigurations,clusterConnectionConfiguration2);

                                    ServerLocator SL = CC.getServerLocator(ss);

                                    if(SL != null) {
                                        SL.initialize();
                                        ClientSessionFactory sessionFactory = SL.createSessionFactory();
                                        System.out.println(sessionFactory.getConnection().getClientID());
                                        ClientSession clientSession = sessionFactory.createSession();

                                    }
                                    */


                    }


                }

            }


            //server.getActiveMQServer().getClusterManager().getClusterController().connectToNode(transportConfigurationRemote);
            //server.getActiveMQServer().getClusterManager().getClusterController().add


                /*
                server.getActiveMQServer().getClusterManager().deployBridge(bridgeConfiguration);
                Map<String, Bridge> bridgeMap = server.getActiveMQServer().getClusterManager().getBridges();

                for (Map.Entry<String, Bridge> entry : bridgeMap.entrySet()) {
                    String key = entry.getKey();
                    Bridge value = entry.getValue();

                    System.out.println("AL " + key + " " + value.getName());
                }
                */




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
