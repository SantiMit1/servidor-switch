package com.bd2;

import java.net.*;
import java.io.*;

public class RequestHandler implements Runnable {
    private final Socket sock;
    private final DbRouter router;
    private static final int MAX_BYTES = 1_048_576;

    public RequestHandler(Socket s, DbRouter r) { sock = s; router = r; }

    private String readUntilEndTag(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int read;
        long total = 0;
        String acc;
        while ((read = bis.read(b)) != -1) {
            buf.write(b, 0, read);
            total += read;
            if (total > MAX_BYTES) throw new IOException("Mensaje demasiado grande");
            acc = buf.toString("UTF-8");
            if (acc.contains("</query>")) return acc;
        }
        throw new IOException("EOF antes de </query>");
    }

    @Override
    public void run() {
        try (Socket s = sock) {
            s.setSoTimeout(30000);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            String req = readUntilEndTag(in);
            System.out.println("Solicitud recibida:\n" + req);
            XmlUtils.Query q = XmlUtils.parseQuery(req);
            DbRouter.QueryResult res = router.execute(q.database, q.sql);
            String resp = XmlUtils.buildResponse(res.cols, res.rows);
            out.write(resp.getBytes("UTF-8"));
            out.flush();
        } catch (Exception e) {
            try {
                String err = "<query><error>" + e.getMessage() + "</error></query>";
                sock.getOutputStream().write(err.getBytes("UTF-8"));
            } catch (Exception ignore) {}
        }
    }
}
