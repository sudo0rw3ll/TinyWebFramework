package framework.engine;

import framework.annotations.APIPath;
import framework.annotations.Controller;
import framework.annotations.DI.*;
import framework.annotations.DI.scopes.BeanScope;
import framework.discovery.ControllerDiscovery;
import framework.request.exceptions.AutowiredWrongType;
import framework.request.exceptions.BeanQualifierNotUnique;
import framework.request.exceptions.InterfaceMissingQualifier;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class DIEngine {

    private static DIEngine instance;
    private static Map<String, Object> controllersMap = new HashMap<String, Object>();
    private static Map<String, Object> dependencyContainer = new HashMap<String, Object>();
    private static Map<String, Object> beansAndServices = new HashMap<>();

    private DIEngine() {}

    public static synchronized DIEngine getInstance() {
        if(instance == null){
            synchronized (DIEngine.class){
                if(instance == null){
                    instance = new DIEngine();
                    mapSingletons("server");

                    try {
                        findInterfaces("server");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    initializeControllers();
                }
            }
        }

        return instance;
    }

    private static void initializeControllers(){
        try{
            Class controllerClasses[] = ControllerDiscovery.findControllers("server");

            for(Class controllerClazz : controllerClasses){
                Constructor<?> controllerConstructor = controllerClazz.getConstructor();

                Object controllerObject = controllerConstructor.newInstance();

                Field declaredFields[] = controllerClazz.getDeclaredFields();

                inject(controllerObject, declaredFields);

                String apiPath = ((APIPath)(controllerClazz.getAnnotation(APIPath.class))).apiPath();

                if(!apiPath.startsWith("/"))
                    apiPath = "/" + apiPath;

                if(!controllersMap.containsKey(apiPath)){
                    controllersMap.put(apiPath, controllerObject);
                }
            }

            for(Map.Entry<String, Object> set : controllersMap.entrySet()){
                System.out.println("Initialized controller for APIpath " + set.getKey());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void inject(Object injectionPoint, Field[] declaredFields) throws Exception {
        for(Field field : declaredFields){
            if(field.isAnnotationPresent(Autowired.class)){
                field.setAccessible(true);
                Class<?> type = field.getType();

//                System.out.println(field.getType());

                Object dependencyObj = null;

                if(type.isInterface()){
                    if(field.isAnnotationPresent(Qualifier.class)){
                        String qualifier = (String) ((Qualifier) field.getAnnotation(Qualifier.class)).value();
                        if(dependencyContainer.containsKey(qualifier)){
                            dependencyObj = dependencyContainer.get(qualifier);
                        }
                    }else{
                        throw new InterfaceMissingQualifier("Interface " + type.getName() + "is missing Qualifier");
                    }
                }

                if(dependencyObj == null && !field.getClass().isAnnotationPresent(Bean.class)){
                    throw new AutowiredWrongType();
                }

                if(dependencyObj == null && beansAndServices.containsKey(type.getName())){
                    dependencyObj = beansAndServices.get(type.getName());
                }

                if(dependencyObj == null)
                    dependencyObj = type.getDeclaredConstructor().newInstance();


                field.set(injectionPoint, dependencyObj);
                field.setAccessible(false);

                inject(dependencyObj, type.getDeclaredFields());

                if(field.getAnnotation(Autowired.class).verbose()){
                    System.out.println("[*] Initialized " + type + " " + field.getName() + " in " + injectionPoint.getClass().getName() + " on " + LocalDateTime.now() + " with " + injectionPoint.hashCode());
                }
            }
        }
    }

    private static void mapSingletons(String packagename){
        try{

            Class singletonClasses[] = findSingletons(packagename);

            for(Class singletonClass : singletonClasses){
                if(!beansAndServices.containsKey(singletonClass.getName())){
                    Constructor constructor = singletonClass.getConstructor();
                    Object singletonObj = constructor.newInstance();
                    beansAndServices.put(singletonClass.getName(), singletonObj);
                }
            }

            for(Map.Entry<String, Object> set : beansAndServices.entrySet()){
                System.out.println(set.getKey() + " singleton initialized");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Class[] findSingletons(String packageName) throws ClassNotFoundException, IOException {
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

    public static void findInterfaces(String packageName) throws ClassNotFoundException, IOException {
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

        filterQualifiers(classes);

        return;
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
        List<Class> singletonClasses = new ArrayList<>();

        for(Class clazz : classes){
            if(clazz.isAnnotationPresent(Bean.class)){
                if(((Bean)(clazz.getAnnotation(Bean.class))).scope() == BeanScope.SINGLETON){
                    singletonClasses.add(clazz);
                    continue;
                }
            }

            if(clazz.isAnnotationPresent(Service.class)){
                singletonClasses.add(clazz);
            }
        }

        return singletonClasses;
    }

    private static void filterQualifiers(ArrayList<Class> classes) {
        try {
            for(Class clazz : classes){
                if(clazz.isAnnotationPresent(Qualifier.class) && clazz.isAnnotationPresent(Bean.class)){
                    String qualifierValue = (String) ((Qualifier) clazz.getAnnotation(Qualifier.class)).value();

                    if(!dependencyContainer.containsKey(qualifierValue)){
                        dependencyContainer.put(qualifierValue, clazz.getDeclaredConstructor().newInstance());
                    }else{
                        throw new BeanQualifierNotUnique();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Object getControllerForRoute(String route) {
        if(controllersMap.containsKey(route)){
            return controllersMap.get(route);
        }

        return null;
    }
}
