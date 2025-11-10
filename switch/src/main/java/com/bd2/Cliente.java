package com.bd2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Nombre de la base de datos: ");
            String db = sc.nextLine().trim();

            String host = "127.0.0.1";
            int port = 5000;

            System.out.println("Introduce consultas SQL una por línea. Escribe 'exit' o deja vacío para salir.");

            while (true) {
                System.out.print("Consulta SQL: ");
                String sql = sc.nextLine();
                if (sql == null) break;
                sql = sql.trim();
                if (sql.isEmpty() || "exit".equalsIgnoreCase(sql)) {
                    System.out.println("Saliendo.");
                    break;
                }

                String xml = "<query><database>" + escapeXml(db) + "</database><sql>" + escapeXml(sql) + "</sql></query>";

                try (Socket s = new Socket(host, port)) {
                    System.out.println("Conectado al switch en " + host + ":" + port);
                    s.setSoTimeout(30000);
                    OutputStream out = s.getOutputStream();
                    InputStream in = s.getInputStream();
                    out.write(xml.getBytes("UTF-8"));
                    out.flush();
                    System.out.println("Consulta enviada al switch.");

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
                } catch (SocketTimeoutException ste) {
                    System.err.println("Tiempo de espera agotado esperando respuesta del switch.");
                } catch (Exception e) {
                    System.err.println("Error al enviar la consulta: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // función para escapar caracteres XML básicos
    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
