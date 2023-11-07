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

@Controller
@APIPath(apiPath = "users")
public class TestClass {

    private Header header;

    private Request request;

    @Autowired
    @Qualifier("testIntfImpl")
    private TestIntf testIntf;


    @GET
    @Path(path = "getAllUsers")
    public void getUsers(){
        this.testIntf.hello();
    }

    @POST
    @Path(path = "createUser")
    public int createUser(){
        return 1;
    }
}
