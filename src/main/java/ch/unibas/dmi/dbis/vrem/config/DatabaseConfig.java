package ch.unibas.dmi.dbis.vrem.config;

import com.mongodb.ConnectionString;

public class DatabaseConfig {


    public String host;

    public short port;

    public String database;


    /**
     * Generates a MongoDB connection string.
     */
    public ConnectionString getConnectionString() {
        return new ConnectionString(String.format("mongodb://%s:%d", this.host, this.port));
    }
}
