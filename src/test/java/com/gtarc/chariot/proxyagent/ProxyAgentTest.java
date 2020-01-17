package com.gtarc.chariot.proxyagent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ProxyAgentTest
{

    @Test
    public void testReceivePlanRequest() throws Exception {
        new ProxyAgent().receivePlanRequest(PlanRequestTest.getJson());
    }
}
