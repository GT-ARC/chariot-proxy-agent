package com.gtarc.chariot.proxyagent;

import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.rsga.beans.AbstractRESTfulAgentBean;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProxyAgent extends AbstractRESTfulAgentBean {

    private HttpClient httpClient = new HttpClient();
    private HashMap<String, String> deviceIDToAgentID = new HashMap<>();
    private HashMap<String, IActionDescription> cachedActions = new HashMap<>();

    private static final String PROPERTY_ACTION = "com.gtarc.chariot.receive_property_action";

    @Override
    public void doStart() throws Exception {
        this.deviceIDToAgentID = httpClient.establishConnection();
    }

    /**
     * Example get request.
     * Invoke with localhost:8080/chariot/ProxyAgent/id?id=test&testParam=1
     *
     * @param id        example param
     * @param testParam example extra param must be from type int
     * @return a combination of both
     */
    @GET
    @Path("id")
    @Expose(scope = ActionScope.WEBSERVICE)
    public String exampleGet(String id, int testParam) {
        return id + " " + testParam;
    }

    /**
     * Example action for a post request in wich the parameters a send via
     * json in the request body. The header Content-Type application/json must be set.
     *
     * @param x first parameter
     * @param y second parameter
     * @return the sum of both parameters
     */
    @POST
    @Path("/add")
    @Expose(scope = ActionScope.WEBSERVICE)
    public int add(@QueryParam("x") int x,
                   @QueryParam("y") int y) {
        return x + y;
    }

    @POST
    @Path("sendAction")
    @Expose(scope = ActionScope.WEBSERVICE)
    public void relayPropertyDelegation(@QueryParam("device_id") String deviceID,
                                        @QueryParam("value") String jsonObject) {

        // Check if device id is in agent list
        String agentID = this.deviceIDToAgentID.get(deviceID);
        if (agentID == null) {
            // Query new updates
            this.deviceIDToAgentID = httpClient.establishConnection();

            agentID = this.deviceIDToAgentID.get(deviceID);
            if (agentID == null) {
                System.err.println("Agent not found for device: " + deviceID);
                return;
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
                return;
            }
            this.cachedActions.put(agentID, cachedDesc);
        }

        // Invoke the agent
        invoke(cachedDesc, new String[]{jsonObject});
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
