package com.gtarc.chariot.proxyagent;

import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.rsga.beans.AbstractRESTfulAgentBean;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public class ProxyAgent extends AbstractRESTfulAgentBean
{

    /**
     * Example get request.
     * Invoke with localhost:8080/chariot/ProxyAgent/id?id=test&testParam=1
     * @param id example param
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
     * @param x first parameter
     * @param y second parameter
     * @return the sum of both parameters
     */
    @POST
    @Path("/add")
    @Expose(scope = ActionScope.WEBSERVICE)
    public int add( @QueryParam("x") int x ,
                    @QueryParam("y") int y) {
        return x + y;
    }

    /**
     * Start the Proxy Agent Node
     * @param args main args
     */
    public static void main( String[] args )
    {
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
