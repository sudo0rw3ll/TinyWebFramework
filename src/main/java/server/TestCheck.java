package server;

import framework.annotations.DI.Bean;
import framework.annotations.DI.Qualifier;
import framework.annotations.DI.Service;
import framework.annotations.DI.scopes.BeanScope;
import framework.annotations.Path;
import framework.annotations.methods.GET;
import framework.annotations.methods.POST;

import java.util.HashMap;
import java.util.Map;

@Bean(scope = BeanScope.SINGLETON)
@Qualifier(value = "testIntfImpl")
public class TestCheck implements TestIntf {

    private Map<String, User> usersMap = new HashMap<String, User>(){{
        put("user1", new User("Vid", "vnikolic", "nekipass123"));
        put("user2", new User("Andrej", "anikolic", "nekipas1234"));
        put("user3", new User("Nikola", "nmatovic", "nekifinpas123"));
    }};

    @Override
    public String ping() {
        return "Pong";
    }

    @Override
    public User getUser(Map<String, String> requestParameters){
        String key = "";

        for(Map.Entry<String, String> set : requestParameters.entrySet()){
            System.out.println(set.getKey() + " = " + set.getValue());
            key = set.getValue();
            break;
        }

        return getUser(key);
    }

    private User getUser(String key){
        if(this.usersMap.containsKey(key))
            return this.usersMap.get(key);
        return null;
    }

    private boolean createUser(User user){
        if(!this.usersMap.containsKey("user" + this.usersMap.keySet().size() + 1)){
            this.usersMap.put("user" + this.usersMap.keySet().size() + 1, user);
            return true;
        }
        return false;
    }

    @Override
    public User createUser(Map<String, String> requestParameters){
        User user = new User(requestParameters.get("name"), requestParameters.get("username"), requestParameters.get("password"));

        boolean userCreation = createUser(user);

        if(userCreation){
            return user;
        }

        return null;
    }
}
