package com.gtarc.chariot.proxyagent;

import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProxyAgent extends AbstractMethodExposingBean {

    private HttpClient httpClient = new HttpClient();
    private HashMap<String, String> deviceIDToAgentID = new HashMap<>();
    private HashMap<String, IActionDescription> cachedActions = new HashMap<>();

    private static final String PROPERTY_ACTION = "com.gtarc.chariot.receive_property_action";
    private static final String ADD_AGENT_ACTION = "com.gtarc.chariot.proxyagent#addAgent";
    private static final String REMOVE_AGENT_ACTION = "com.gtarc.chariot.proxyagent#removeAgent";

    private MyHttpServer httpServer;

    @Override
    public void doStart() throws Exception {
        this.deviceIDToAgentID = httpClient.establishConnection();
        httpServer = new MyHttpServer(this);
    }

    public void relayPropertyDelegation(String deviceID, String jsonObject) throws Exception {

        // Check if device id is in agent list
        String agentID = this.deviceIDToAgentID.get(deviceID);
        if (agentID == null) {
            // Query new updates
            this.deviceIDToAgentID = httpClient.establishConnection();

            agentID = this.deviceIDToAgentID.get(deviceID);
            if (agentID == null) {
                System.err.println("Agent not found for device: " + deviceID);
                throw new Exception("Agent not found for device: " + deviceID);
            }
        }

        // Receive the cached action if no action is cached search for new one
        IActionDescription cachedDesc = this.cachedActions.get(agentID);
        if (cachedDesc == null) {
            List<IActionDescription> actionDescriptions = thisAgent.searchAllActions(new Action(PROPERTY_ACTION));
            System.out.println(Arrays.toString(actionDescriptions.toArray()));
            String finalAgentID = agentID;
            Optional<IActionDescription> optionalDesc = actionDescriptions.stream().filter(
                    desc -> desc.getProviderDescription().getAid().equals(finalAgentID)).findAny();
            if (optionalDesc.isPresent())
                cachedDesc = optionalDesc.get();
            else {
                System.err.println("No action description found for device agent: " + agentID + " of device " + deviceID);
                throw new Exception("No action description found for device agent: " + agentID + " of device " + deviceID);
            }
            this.cachedActions.put(agentID, cachedDesc);
        }

        // Invoke the agent
        invoke(cachedDesc, new String[]{jsonObject});
    }

    @Expose(name = ADD_AGENT_ACTION, scope = ActionScope.GLOBAL)
    public void addAgent(String agentID, String deviceID) {
        this.deviceIDToAgentID.put(deviceID, agentID);
        log.info("Add Agent Mapping: " + deviceID + " -> " + agentID);
    }

    @Expose(name = REMOVE_AGENT_ACTION, scope = ActionScope.GLOBAL)
    public void removeAgent(String agentID, String deviceID) {
        this.deviceIDToAgentID.remove(deviceID);
        this.cachedActions.remove(agentID);
        log.info("Remove Agent Mapping: " + deviceID + " -> " + agentID);
    }

    /**
     * Start the Proxy Agent Node
     *
     * @param args main args
     */
    public static void main(String[] args) {
        ApplicationContext context = SimpleAgentNode.startAgentNode("classpath:Proxy_Agent.xml", "jiactng_log4j.properties");
        SimpleAgentNode node = (SimpleAgentNode) context.getBean("ProxyAgentNode");
        try {
            System.out.println("Start Node");
            node.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}
