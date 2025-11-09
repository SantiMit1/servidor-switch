package com.bd2;

import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream is = Server.class.getResourceAsStream("/switch.properties")) {
            props.load(is);
        }
        int port = Integer.parseInt(props.getProperty("server.port", "5000"));
        DbRouter router = new DbRouter(props);
        ExecutorService pool = Executors.newFixedThreadPool(20);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Switch escuchando en puerto " + port);
            while (true) {
                Socket s = server.accept();
                pool.submit(new RequestHandler(s, router));
            }
        } finally {
            router.close();
            pool.shutdown();
        }
    }
}
