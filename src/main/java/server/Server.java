package server;

import framework.discovery.ControllerDiscovery;
import framework.engine.DIEngine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int TCP_PORT = 8080;
    public static ControllerDiscovery controllerDiscovery = ControllerDiscovery.getInstance();
    public static DIEngine diEngine = DIEngine.getInstance();

    public static void main(String[] args) throws IOException {

        try {
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server is running at http://localhost:"+TCP_PORT);
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new ServerThread(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}