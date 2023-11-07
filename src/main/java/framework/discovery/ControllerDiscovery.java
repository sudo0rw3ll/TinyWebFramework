package framework.discovery;

import framework.annotations.APIPath;
import framework.annotations.Controller;
import framework.annotations.Path;
import framework.annotations.methods.GET;
import framework.annotations.methods.POST;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Array;
import java.util.*;

public class ControllerDiscovery {

    private static ControllerDiscovery instance;
    private static Map<String, Object> controllerInstances = new HashMap<String, Object>();
    private static Map<String, Method> controllerMethodMaps = new HashMap<String, Method>();

    private ControllerDiscovery(){}

    public static synchronized ControllerDiscovery getInstance(){
        if(instance == null){
            synchronized (ControllerDiscovery.class){
                if (instance == null){
                    instance = new ControllerDiscovery();
                    mapRoutesToControllers();
                }
            }
        }

        return instance;
    }

    private static void mapRoutesToControllers(){
        try{
            Class controllerClasses[] = findControllers("server");

            if(controllerClasses.length == 0)
                return;

            for(Class clazz : controllerClasses){
//                instantiateController(clazz);
                mapMethods(clazz);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void instantiateController(Class clazz){
        try{
            if(clazz.isAnnotationPresent(APIPath.class)){
                APIPath apiPath = (APIPath) clazz.getAnnotation(APIPath.class);

                String path = apiPath.apiPath();

                if(!path.isEmpty()){
                    if(!path.startsWith("/"))
                        path = "/" + path;

                    System.out.println(path);
                    Constructor controllerConstructor = clazz.getDeclaredConstructors()[0];
                    Object controllerInstance = controllerConstructor.newInstance();

                    controllerInstances.put(path, controllerInstance);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void mapMethods(Class clazz){
        try{
            Method controllerMethods[] = clazz.getDeclaredMethods();

            String apiPath = "";

            if(clazz.isAnnotationPresent(APIPath.class)){
                apiPath =  ((APIPath) clazz.getAnnotation(APIPath.class)).apiPath();

                if(!apiPath.startsWith("/"))
                    apiPath = "/" + apiPath;
            }

            for(Method method : controllerMethods){
                String path = "";
                if(method.isAnnotationPresent(Path.class)){
                    Path methodPath = (Path) method.getAnnotation(Path.class);
                    path = methodPath.path();
                }

                if(!path.startsWith("/"))
                    path = "/" + path;

                if(method.isAnnotationPresent(GET.class)){
                    String controllerMethodKey = apiPath.isEmpty() ? "GET:" + path : "GET:" + apiPath + path;
                    controllerMethodMaps.put(controllerMethodKey, method);
                    continue;
                }

                if(method.isAnnotationPresent(POST.class)){
                    String controllerMethodKey = apiPath.isEmpty() ? "POST:" + path : "POST:" + apiPath + path;
                    controllerMethodMaps.put(controllerMethodKey, method);
                }
            }

            for(Map.Entry<String, Method> set : controllerMethodMaps.entrySet()){
                System.out.println(set.getKey() + " maps to method " + (((Method) set.getValue()).getName()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static Class[] findControllers(String packageName) throws ClassNotFoundException, IOException {
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

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
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

    private static List<Class> filterClasses(ArrayList<Class> classes){
        List<Class> controllerClasses = new ArrayList<>();

        for(Class clazz : classes){
            if(clazz.isAnnotationPresent(Controller.class)){
                controllerClasses.add(clazz);
            }
        }

        return controllerClasses;
    }

    public Method getControllerMethod(String path){
        if(controllerMethodMaps.containsKey(path)){
            return controllerMethodMaps.get(path);
        }

        return null;
    }
}
