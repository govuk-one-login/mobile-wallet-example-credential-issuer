package uk.gov.di.mobile.wallet.cri.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/hello/{name}")
public class HelloWorldResource {
    @GET
    public String getGreeting(@PathParam("name") String name) {
        return "Hello, " + name + "!";
    }
}
