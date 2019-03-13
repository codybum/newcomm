import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
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
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class BrokerBak {

    public String agentpath;
    public String password;
    public String port;
    public String remote_address;
    public String remote_port;

    public BrokerBak(String agentpath, String password, String port, String remote_address, String remote_port) {

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


            for (Map.Entry<String, Object> entry : transportConfiguration.getParams().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key + ":" + value);
            }

            transports.add(transportConfiguration);
            //transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

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

/*
            TransportConfiguration transportConfigurationLocal = null;
            transportConfigurationLocal = new TransportConfiguration(NettyConnectorFactory.class.getName());
            transportConfigurationLocal.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
            transportConfigurationLocal.getParams().put(TransportConstants.TRUST_ALL_PROP_NAME, true);
            transportConfigurationLocal.getParams().put(TransportConstants.VERIFY_HOST_PROP_NAME, false);
            transportConfigurationLocal.getParams().put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME, "PKCS12");
            transportConfigurationLocal.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, agentpath + "-trust.pkcs12");
            transportConfigurationLocal.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, password);
            transportConfigurationLocal.getParams().put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME, "PKCS12");
            transportConfigurationLocal.getParams().put(TransportConstants.KEYSTORE_PATH_PROP_NAME, agentpath + "-key.pkcs12");
            transportConfigurationLocal.getParams().put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, password);
            //transportConfigurationLocal.getParams().put(TransportConstants.HOST_PROP_NAME, "localhost");
            transportConfigurationLocal.getParams().put(TransportConstants.PORT_PROP_NAME, port);
*/

            TransportConfiguration transportConfigurationLocal = new TransportConfiguration(InVMConnectorFactory.class.getName());

            config.addConnectorConfiguration("local-connector",transportConfigurationLocal);

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

            BridgeConfiguration bridgeConfiguration = null;
            TransportConfiguration transportConfigurationRemote = null;



            //if(port.equals("32011")) {
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
                bridgeConfiguration.setQueueName("*");
                //bridgeConfiguration.setForwardingAddress("exampleQueue");
                bridgeConfiguration.setHA(false);
                bridgeConfiguration.setConfirmationWindowSize(1);

                /*
                List<String> staticConnectors = new ArrayList<>();
                staticConnectors.add("remote-connector");
                bridgeConfiguration.setStaticConnectors(staticConnectors);
                */

                //start cluster



            }






            EmbeddedActiveMQ server = new EmbeddedActiveMQ();
            server.setConfiguration(config);

            server.start();


            if(bridgeConfiguration != null) {


                //server.getActiveMQServer().getClusterManager().clear();
                //server.getActiveMQServer().getConfiguration().clearClusterConfigurations();
                //List<String> staticConnectors = new ArrayList<>();
                //staticConnectors.add("remote-connector");

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


                /*
                <cluster-connections>
   <cluster-connection name="my-cluster">
      <address>jms</address>
      <connector-ref>netty-connector</connector-ref>
      <check-period>1000</check-period>
      <connection-ttl>5000</connection-ttl>
      <min-large-message-size>50000</min-large-message-size>
      <call-timeout>5000</call-timeout>
      <retry-interval>500</retry-interval>
      <retry-interval-multiplier>1.0</retry-interval-multiplier>
      <max-retry-interval>5000</max-retry-interval>
      <initial-connect-attempts>-1</initial-connect-attempts>
      <reconnect-attempts>-1</reconnect-attempts>
      <use-duplicate-detection>true</use-duplicate-detection>
      <forward-when-no-consumers>false</forward-when-no-consumers>
      <max-hops>1</max-hops>
      <confirmation-window-size>32000</confirmation-window-size>
      <call-failover-timeout>30000</call-failover-timeout>
      <notification-interval>1000</notification-interval>
      <notification-attempts>2</notification-attempts>
      <discovery-group-ref discovery-group-name="my-discovery-group"/>
   </cluster-connection>
</cluster-connections>
                 */




                //List<ClusterConnectionConfiguration> clusterConnectionConfigurations = new ArrayList<>();
                //clusterConnectionConfigurations.add(clusterConnectionConfiguration);
                //config.addClusterConfiguration(clusterConnectionConfiguration);

                //server.getActiveMQServer().getClusterManager().getClusterController().stop();
                //List<TransportConfiguration> transportConfigurations = new ArrayList<>();
                //transportConfigurations.add(transportConfigurationRemote);

                TransportConfiguration[] transportConfigurations = new TransportConfiguration[1];
                transportConfigurations[0] = transportConfigurationRemote;

                System.out.println("Describe: " + server.getActiveMQServer().getClusterManager().describe());

                ClusterManager cm = server.getActiveMQServer().getClusterManager();
                if(cm != null) {
                    System.out.println("CM != null");

                    //cm.start();


                    System.out.println("GET BY NAME: " + cm.getClusterConnection("test").getName());

                    //cm.getClusterConnection("test").addClusterTopologyListener();

                    //cm.getClusterController().connectToNodeInReplicatedCluster(transportConfigurationRemote);

                    List<String> staticConnectors = new ArrayList<>();
                    staticConnectors.add("remote-connector");


                    for(ClusterConnectionConfiguration cc : server.getActiveMQServer().getConfiguration().getClusterConfigurations()) {
                        cc.setStaticConnectors(staticConnectors);
                        System.out.println("******** " + cc.getName() + " " + cc.getAddress());
                        for(String connectorName : cc.getStaticConnectors()) {
                            System.out.println("connector: " + connectorName);
                        }
                    }




                    //ClusterControl clusterControl = cm.getClusterController().connectToNode(transportConfigurationRemote);
                    //System.out.println("USER: " + clusterControl.getClusterUser());
                    //System.out.println("PASS: " + clusterControl.getClusterPassword());
                    //clusterControl.authorize();
                    //Channel channel = clusterControl.createReplicationChannel();




                    /*
                    Set<ClusterConnection> clusterConnections = cm.getClusterConnections();
                    for(ClusterConnection clusterConnection : clusterConnections) {
                        System.out.println(clusterConnection.getName().toString());
                        ClusterTopologyListener clusterTopologyListener

                    }
                    */

//                    clusterConnectionConfiguration.setConnectorName("remote-connector");
                    //ClusterConnection clusterConnection = cm.getClusterConnection("test");
                    //clusterConnection.start();

                    ClusterController CC = cm.getClusterController();
                    if(CC != null) {
                        System.out.println("CC != null");



                        SimpleString ss = SimpleString.toSimpleString("word");

                        if(ss != null) {
                            System.out.println("SS != null");
                            if(transportConfigurations != null) {
                                //System.out.println("Trans != null " + transportConfigurations.length);


                                if(clusterConnectionConfiguration2 != null) {
                                    //System.out.println("cluster != null");

                                    //CC.addClusterConnection(ss,transportConfigurations,clusterConnectionConfiguration);
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
