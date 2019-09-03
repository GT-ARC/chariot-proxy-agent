# Chariot Jiac Proxy Agent

This proxy agent is used as an interface between the front-end and the device agents.
It implements the AbstractRESTfulAgentBean as an rest interface.

###  Agent definition
In the `Proxy_Agent.xml` are the agents defined. 

The context path is defined in the spring bean of the AbstractRESTfulAgentBean. \
It can be changed to everything. Here `chariot`.  

###  Example

Two example methods have been implemented. \
\
**Add**\
A post method that adds the two parameters given in the request body of a post requst with the header
Content-Type application/json.\
Invokable with: `localhost:8080/chariot/ProxyAgent/add` \
Body in this simple json form:
```json
{
    "x": "5",
    "y": "10"
}
```
\
**exampleGet** \
An example get request to visualize how to pass parameters in the req url.\
Invokable with: `localhost:8080/chariot/ProxyAgent/id?param1=test&param2=1` \

The key of the parameter dosn't matter, only the order. 

### Usage
Maven is used in this project. Just run `mvn package`.\
To start the proxy-agent the plugin `appassembler-maven-plugin` 
is used to generate runnable files: `./target/appassembler/bin/ChariotProxyAgent`

