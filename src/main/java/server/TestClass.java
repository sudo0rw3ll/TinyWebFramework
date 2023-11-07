package server;

import framework.annotations.APIPath;
import framework.annotations.Controller;
import framework.annotations.DI.Autowired;
import framework.annotations.DI.Qualifier;
import framework.annotations.Path;
import framework.annotations.methods.GET;
import framework.annotations.methods.POST;
import framework.request.Header;
import framework.request.Request;

import java.util.*;

@Controller
@APIPath(apiPath = "users")
public class TestClass {

    private Header header;

    private Request request;

    @Autowired
    @Qualifier("testIntfImpl")
    private TestIntf testIntf;


    @GET
    @Path(path = "ping")
    public String ping(){
        return this.testIntf.ping();
    }

    @GET
    @Path(path = "getUser")
    public User getUser(Map<String, String> requestParameters){
        return this.testIntf.getUser(requestParameters);
    }


    @POST
    @Path(path = "createUser")
    public User createUser(Map<String, String> requestParameters){
        return this.testIntf.createUser(requestParameters);
    }
}
