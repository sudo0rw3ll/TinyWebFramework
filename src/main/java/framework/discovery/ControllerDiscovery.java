package framework.discovery;

import framework.annotations.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Array;
import java.util.*;

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

    public Class[] findControllers(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.','/');

        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<File>();

        while(resources.hasMoreElements()){
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<Class>();

        for(File directory : dirs){
            classes.addAll(findClasses(directory, packageName));
        }

        ArrayList<Class> finalClasses = new ArrayList<>(filterClasses(classes));

        return finalClasses.toArray(new Class[finalClasses.size()]);
    }

    public List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();

        if(!directory.exists()){
            return classes;
        }

        File[] files = directory.listFiles();

        for(File file : files){
            if(file.isDirectory()){
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }else if (file.getName().endsWith(".class")){
                classes.add(Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }

    public List<Class> filterClasses(ArrayList<Class> classes){
        List<Class> controllerClasses = new ArrayList<>();

        for(Class clazz : classes){
            if(clazz.isAnnotationPresent(Controller.class)){
                controllerClasses.add(clazz);
            }
        }

        return controllerClasses;
    }
}
