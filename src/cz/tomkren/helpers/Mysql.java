package cz.tomkren.helpers;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class Mysql {

    public static final Opts DEFAULT = new Mysql.Opts("omega4","root","");

    public static String fromDate(java.util.Date d) {
        if (d == null) {return "NULL";}
        return "'"+Log.date(d)+"'";
    }

    public static String fromBoolean(Boolean b) {
        if (b == null) {return "NULL";}
        return b ? "'1'" : "'0'";
    }

    public static class Opts {
        private String dbName;
        private String username;
        private String password;

        public Opts(String dbName, String username, String password) {
            this.dbName = dbName;
            this.username = username;
            this.password = password;
        }

        public String getDbName() {
            return dbName;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    public static void testDriver() {
        try {
            Log.it("Loading driver...");
            Class.forName("com.mysql.jdbc.Driver");
            Log.it("Driver loaded!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find the driver in the classpath!", e);
        }
    }

    public static void exes(List<String> queries) {
        executes(DEFAULT, queries);
    }

    //TODO možná je lepší je dělat v jednom spojení a ne takhle
    public static void executes(Opts opts, List<String> queries) {
        for (String query : queries) {
            execute(opts, query);
        }
    }

    public static void execute(Opts opts, String sql) {
        String url = "jdbc:mysql://localhost:3306/"+opts.getDbName();
        Connection connection = null;
        try {
            //Log.it("Connecting database...");
            connection = DriverManager.getConnection(url, opts.getUsername(), opts.getPassword());
            //Log.it("Database connected!");
            Statement statement = connection.createStatement();
            statement.execute( sql );
            //Log.it( "\n"+ sql );
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect the database!", e);
        } finally {
            //Log.it("Closing the connection.");
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException ignore) {}
        }
    }

    public static Object exe(String sql, Function<ResultSet,Object> callback) {
        return execute2(DEFAULT, sql, callback);
    }

    public static Object execute2(Opts opts, String sql, Function<ResultSet,Object> callback) {
        Object ret = null;

        String url = "jdbc:mysql://localhost:3306/"+opts.getDbName();
        Connection connection = null;
        try {
            //Log.it("Connecting database...");
            connection = DriverManager.getConnection(url, opts.getUsername(), opts.getPassword());
            //Log.it("Database connected!");
            Statement statement = connection.createStatement();

            //statement.execute( sql );
            ResultSet rs = statement.executeQuery(sql);

            ret = callback.apply(rs);

            //Log.it( "\n"+ sql );
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect the database!", e);
        } finally {
            //Log.it("Closing the connection.");
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException ignore) {}
        }

        return ret;
    }


}
