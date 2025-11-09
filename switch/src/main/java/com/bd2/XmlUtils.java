package com.bd2;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.util.*;

public class XmlUtils {
    public static class Query {
        public final String database;
        public final String sql;
        public Query(String d, String s){ database=d; sql=s; }
    }

    public static Query parseQuery(String xml) throws Exception {
        XMLInputFactory f = XMLInputFactory.newFactory();
        try (StringReader sr = new StringReader(xml)) {
            XMLEventReader r = f.createXMLEventReader(sr);
            String db=null, sql=null;
            while (r.hasNext()) {
                XMLEvent e = r.nextEvent();
                if (e.isStartElement()) {
                    String name = e.asStartElement().getName().getLocalPart();
                    if ("database".equalsIgnoreCase(name)) db = r.getElementText().trim();
                    if ("sql".equalsIgnoreCase(name)) sql = r.getElementText().trim();
                }
            }
            if (db==null || sql==null) throw new IllegalArgumentException("Falta <database> o <sql>");
            return new Query(db, sql);
        }
    }

    public static String buildResponse(List<String> cols, List<List<String>> rows) throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("query");

        w.writeStartElement("cols");
        for (int i=0; i<cols.size(); i++) {
            w.writeStartElement("colname"+(i+1));
            w.writeCharacters(cols.get(i));
            w.writeEndElement();
        }
        w.writeEndElement();

        w.writeStartElement("rows");
        for (int r=0; r<rows.size(); r++) {
            w.writeStartElement("row"+(r+1));
            List<String> row = rows.get(r);
            for (int c=0; c<row.size(); c++) {
                w.writeStartElement("col"+(c+1));
                w.writeCharacters(row.get(c)==null?"":row.get(c));
                w.writeEndElement();
            }
            w.writeEndElement();
        }
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.flush(); w.close();
        return sw.toString();
    }
}
