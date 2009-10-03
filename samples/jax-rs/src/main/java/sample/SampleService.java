package sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class SampleService {
    @GET
    @Produces("text/plain")
    @Path("/test")
    public String test() {
        return "Hello!";
    }
}
