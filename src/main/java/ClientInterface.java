
public interface ClientInterface {

    public boolean isFaultURIActive();
    //public ActiveMQSession createSession(String URI, boolean transacted, int acknowledgeMode);
    public boolean initActiveAgentConsumer(String RXQueueName, String URI);
    public boolean initActiveAgentProducer(String URI);
    public boolean hasActiveProducer();
    //public void sendAPMessage(MsgEvent msg);
    public void shutdown();
    //public boolean sendMessage(MsgEvent sm);


}