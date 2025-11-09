package com.bd2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Nombre de la base de datos: ");
            String db = sc.nextLine().trim();

            System.out.print("Consulta SQL: ");
            String sql = sc.nextLine().trim();

            String xml = "<query><database>" + db + "</database><sql>" + sql + "</sql></query>";

            String host = System.getProperty("switch.host", "switch");
            int port = Integer.parseInt(System.getProperty("switch.port", "5000"));

            try (Socket s = new Socket(host, port)) {
                s.setSoTimeout(30000);
                OutputStream out = s.getOutputStream();
                InputStream in = s.getInputStream();
                out.write(xml.getBytes("UTF-8"));
                out.flush();

                ByteArrayOutputStream resp = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int read;
                while ((read = in.read(buf)) != -1) {
                    resp.write(buf, 0, read);
                    String acc = resp.toString("UTF-8");
                    if (acc.contains("</query>")) break;
                }
                System.out.println("\nRespuesta del switch:\n");
                System.out.println(resp.toString("UTF-8"));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
