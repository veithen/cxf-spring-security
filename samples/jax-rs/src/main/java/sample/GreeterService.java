package sample;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.springframework.security.annotation.Secured;

@Path("/greeting")
public class GreeterService {
    private String greeting = "Hello %u!";
    
    @GET
    @Produces("text/plain")
    @Secured("ROLE_USER")
    public String getGreeting(@Context SecurityContext securityContext) {
        return greeting.replaceAll("%u", securityContext.getUserPrincipal().getName());
    }
    
    @PUT
    @Consumes("text/plain")
    @Secured("ROLE_ADMIN")
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
