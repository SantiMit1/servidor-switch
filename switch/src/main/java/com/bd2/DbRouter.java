package com.bd2;

import com.zaxxer.hikari.*;
import java.sql.*;
import java.util.*;

public class DbRouter {
    private final Map<String, HikariDataSource> ds = new HashMap<>();

    public DbRouter(Properties props) {
        int poolSize = Integer.parseInt(props.getProperty("pool.size"));

        HikariConfig pg = new HikariConfig();
        pg.setJdbcUrl(props.getProperty("postgres.url"));
        pg.setUsername(props.getProperty("postgres.user"));
        pg.setPassword(props.getProperty("postgres.pass"));
        pg.setMaximumPoolSize(poolSize);
        ds.put("postgres", new HikariDataSource(pg));

        HikariConfig fb = new HikariConfig();
        fb.setJdbcUrl(props.getProperty("firebird.url"));
        fb.setUsername(props.getProperty("firebird.user"));
        fb.setPassword(props.getProperty("firebird.pass"));
        fb.setMaximumPoolSize(poolSize);
        ds.put("firebird", new HikariDataSource(fb));

        ds.put("personal", ds.get("postgres"));
        ds.put("facturacion", ds.get("firebird"));
    }

    public void close() {
        ds.values().forEach(HikariDataSource::close);
    }

    public static class QueryResult {
        public final List<String> cols;
        public final List<List<String>> rows;
        public QueryResult(List<String> c, List<List<String>> r){ cols=c; rows=r; }
    }

    public QueryResult execute(String dbName, String sql) throws SQLException {
        HikariDataSource source = ds.get(dbName.toLowerCase());
        if (source == null) throw new SQLException("DB no mapeada: " + dbName);
        try (Connection conn = source.getConnection();
             Statement st = conn.createStatement()) {
            st.setQueryTimeout(30);
            boolean hasResult = st.execute(sql);
            if (!hasResult) return new QueryResult(List.of(), List.of());
            try (ResultSet rs = st.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                List<String> cols = new ArrayList<>();
                for (int i = 1; i <= n; i++) cols.add(md.getColumnLabel(i));
                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= n; i++) row.add(rs.getString(i));
                    rows.add(row);
                }
                return new QueryResult(cols, rows);
            }
        }
    }
}
