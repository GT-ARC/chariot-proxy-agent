# Chariot Proxy Agent
Proxy Agent is conceptualized for fulfilling the requirement of the interaction between business (front-end) applications and IoT device agents.
CHARIOT monitoring and maintenance (MM) application is an example front-end application that uses the proxy-agent to access the IoT devices.
Proxy-agent receives the required message from the front-end app, finds the related device agent ID through the provided device UUID, and then call the related action in the device agent.

The agent configuration is located in the `resources/Proxy_Agent.xml`. 
Proxy-agent extends GatewayConfig in order to access all agents running in the different sub network domains. 
Note that the KMS interface would be accessible with a correct KMS url address in `HTTPClient`. 
The default url is  `http://localhost:8080/v1/` and should be adapted according to the test environment. This parameter can be later added through the .xml file. 

A request message from the front-end app to Proxy-Agent:

```json
  {
    "command": "stop",
    "uuid": "example-uuid",
    "inputs": {
      "device-uuid": "device-example-uuid",
      "second-parameter": "example-parameter"
    }
  }
```
Each IoT device agent includes `handleProperty` function, which is called by this proxy agent, and the aforementioned parameters are passed to it.
As mentioned before, proxy agent can access this function with the matching agentId stored by the monitoring agent in the CHARIOT database. 


### Usage
Maven is used in this project. Just run `mvn package`.\
To start the proxy-agent the plugin `appassembler-maven-plugin` 
is used to generate runnable files: `./target/appassembler/bin/ChariotProxyAgent`

## Contacts

The following persons can answer your questions: 

- Frederic Abraham: [mail@fabraham.dev](mailto://mail@fabraham.dev)
- Cem Akpolat: [akpolatcem@gmail.com](mailto://akpolatcem@gmail.com)

 
