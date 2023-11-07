package server;

import framework.discovery.ControllerDiscovery;
import framework.engine.DIEngine;
import framework.response.JsonResponse;
import framework.response.Response;
import framework.request.enums.Method;
import framework.request.Header;
import framework.request.Helper;
import framework.request.Request;
import framework.request.exceptions.RequestNotValidException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public static ControllerDiscovery controllerDiscovery = ControllerDiscovery.getInstance();
    public static DIEngine diEngine = DIEngine.getInstance();

    public ServerThread(Socket socket){
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {

            Request request = generateRequest();

            if(request == null) {
                in.close();
                out.close();
                socket.close();
                return;
            }

            String route = parseTheRoute(request);

            System.out.println("ROUTE -> " + route);

            Object routeController = diEngine.getControllerForRoute(route);

            String controllerMethodKey;

            if(request.getMethod() == Method.GET){
                controllerMethodKey = "GET:" + request.getLocation();
            }else{
                controllerMethodKey = "POST:" + request.getLocation();
            }

            java.lang.reflect.Method controllerMethod = controllerDiscovery.getControllerMethod(controllerMethodKey);

            Response response = null;

            if(controllerMethod.getParameters().length != 0){
                Object methodResp = controllerMethod.invoke(routeController, request.getParameters());
                if(methodResp != null){
                    response = new JsonResponse(methodResp);
                }
            }else{
                Object methodResp = controllerMethod.invoke(routeController);
                if(methodResp != null){
                    response = new JsonResponse(methodResp);
                }
            }

            if(response != null)
                out.println(response.render());

            in.close();
            out.close();
            socket.close();

        } catch (IOException | RequestNotValidException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String parseTheRoute(Request request){
        String routeParts[];

        StringBuilder route = new StringBuilder("/");

        try{
            routeParts = request.getLocation().split("/");

            for(String part : routeParts){
                if (part.isEmpty())
                    continue;
                route.append(part);
                break;
            }

        }catch(Exception e){
           e.printStackTrace();
        }

        return route.toString();
    }

    private Request generateRequest() throws IOException, RequestNotValidException {
        String command = in.readLine();
        System.out.println(command);

        if(command == null) {
            return null;
        }

        String[] actionRow = command.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = actionRow[1];
        Header header = new Header();
        HashMap<String, String> parameters = Helper.getParametersFromRoute(route);

        do {
            command = in.readLine();
            System.out.println("PARSE -> " + command);
            String[] headerRow = command.split(": ");
            if(headerRow.length == 2) {
                header.add(headerRow[0], headerRow[1]);
            }
        } while(!command.trim().equals(""));

        if(method.equals(Method.POST)) {
            int contentLength = Integer.parseInt(header.get("Content-Length"));
            char[] buff = new char[contentLength];
            in.read(buff, 0, contentLength);
            String parametersString = new String(buff);

            HashMap<String, String> postParameters = Helper.getParametersFromString(parametersString);
            for (String parameterName : postParameters.keySet()) {
                parameters.put(parameterName, postParameters.get(parameterName));
            }
        }

        if(method.equals(Method.GET)) {
            int contentLength = Integer.parseInt(header.get("Content-Length"));
            char[] buff = new char[contentLength];
            in.read(buff, 0, contentLength);
            String parametersString = new String(buff);

            HashMap<String, String> postParameters = Helper.getParametersFromString(parametersString);
            for (String parameterName : postParameters.keySet()) {
                parameters.put(parameterName, postParameters.get(parameterName));
            }
        }

        Request request = new Request(method, route, header, parameters);

        return request;
    }
}
