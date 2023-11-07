package server;

import java.util.Map;

public interface TestIntf {
    String ping();
    User createUser(Map<String, String> requestParameters);
    User getUser(Map<String, String> requestParameters);
}
