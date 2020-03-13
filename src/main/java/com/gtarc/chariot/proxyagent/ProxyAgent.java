package com.gtarc.chariot.proxyagent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

public class ProxyAgent extends AbstractMethodExposingBean {

    public static ProxyAgent INSTANCE = null;

    private HttpClient httpClient = new HttpClient();
    private HashMap<String, IActionDescription> cachedActions = new HashMap<>();
    private HashMap<String, Long> cachedActionsTimer = new HashMap<>();

    private static final String GET_AGENT_ID = "com.gtarc.chariot.proxyagent#getAgentID";
    public static final String PROPERTY_ACTION = "de.gtarc.chariot.handlePropertyAction";
    private static final String ADD_AGENT_ACTION = "com.gtarc.chariot.proxyagent#addAgent";
    private static final String REMOVE_AGENT_ACTION = "com.gtarc.chariot.proxyagent#removeAgent";
    private static final String RECEIVE_PLAN_REQUEST = "com.gtarc.chariot.proxyagent#receivePlanRequest";

    private MyHttpServer httpServer;



    @Override
    public void doStart() throws Exception {
        INSTANCE = this;
        Util.setDeviceIDToAgentID(httpClient.establishConnection());
        httpServer = new MyHttpServer(this);
    }

    public void relayPropertyDelegation(ProxyRelayRequest receivedProxyRelayRequest) throws Exception {

        String agentID = Util.getAgentIDByUUID(receivedProxyRelayRequest.getUuid());

        // Receive the cached action if no action is cached search for new one
        IActionDescription cachedDesc = this.cachedActions.get(agentID);

        // Remove the cached actions if the timer is bigger then 10 seconds
        if(cachedDesc != null && this.cachedActionsTimer.containsKey(agentID)) {
            if (new Date().getTime() - this.cachedActionsTimer.get(agentID) > 10000){
               this.cachedActions.remove(agentID);
               cachedDesc = null;
            }
        }

        if (cachedDesc == null) {
            List<IActionDescription> actionDescriptions = thisAgent.searchAllActions(new Action(PROPERTY_ACTION));
            System.out.println(Arrays.toString(actionDescriptions.toArray()));
            String finalAgentID = agentID;
            Optional<IActionDescription> optionalDesc = actionDescriptions.stream().filter(
                    desc -> desc.getProviderDescription().getAid().equals(finalAgentID)).findAny();
            if (optionalDesc.isPresent())
                cachedDesc = optionalDesc.get();
            else {
                System.err.println("No action description found for device agent: " + agentID + " of device " + receivedProxyRelayRequest.getUuid());
                throw new Exception("No action description found for device agent: " + agentID + " of device " + receivedProxyRelayRequest.getUuid());
            }
            this.cachedActions.put(agentID, cachedDesc);
            this.cachedActionsTimer.put(agentID, new Date().getTime());
        }

        // Invoke the agent
        System.out.println("Agent found and invoked");
        invoke(cachedDesc, new Serializable[]{receivedProxyRelayRequest.getJson()});
    }

    @Expose(name = RECEIVE_PLAN_REQUEST, scope = ActionScope.GLOBAL)
    public void receivePlanRequest(String planRequestJson) throws Exception {

        // Parse the json into a plan request
        Type listType = new TypeToken<ArrayList<ProxyRelayRequest>>(){}.getType();
        List<ProxyRelayRequest> proxyRelayRequestList = new Gson().fromJson(planRequestJson, listType);

        for(ProxyRelayRequest proxyRelayRequest : proxyRelayRequestList) {
            proxyRelayRequest.addAgentIDMappingToInput();
            relayPropertyDelegation(proxyRelayRequest);
        }
    }


    @Expose(name = ADD_AGENT_ACTION, scope = ActionScope.GLOBAL)
    public void addAgent(String agentID, String deviceID) {
        Util.addAgent(deviceID, agentID);
        log.info("Add Agent Mapping: " + deviceID + " -> " + agentID);
    }

    @Expose(name = REMOVE_AGENT_ACTION, scope = ActionScope.GLOBAL)
    public void removeAgent(String agentID, String deviceID) {
        Util.removeAgent(deviceID);
        this.cachedActions.remove(agentID);
        log.info("Remove Agent Mapping: " + deviceID + " -> " + agentID);
    }

    @Expose(name = GET_AGENT_ID, scope = ActionScope.GLOBAL)
    public String getAgentId(String deviceID) throws Exception {
        System.out.println("Get agent ID is called: " + deviceID);
        return Util.getAgentIDByUUID(deviceID);
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
