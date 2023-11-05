package framework.discovery;

public class ControllerDiscovery {

    private static ControllerDiscovery instance;

    private ControllerDiscovery(){}

    public static synchronized ControllerDiscovery getInstance(){
        if(instance == null){
            synchronized (ControllerDiscovery.class){
                if (instance == null){
                    instance = new ControllerDiscovery();
                }
            }
        }

        return instance;
    }
}
