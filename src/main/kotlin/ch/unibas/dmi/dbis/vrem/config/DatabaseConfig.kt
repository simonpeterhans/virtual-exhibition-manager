package ch.unibas.dmi.dbis.vrem.config

import com.mongodb.ConnectionString
import kotlinx.serialization.Serializable

/**
 * Configuration of the (mongodb) database
 *
 * @property host The host address of the database
 * @property port The port of the database
 * @property database The name of the database
 *
 * @author loris.sauter
 */
@Serializable
data class DatabaseConfig (
    val host:String,
    val port:Short,
    val database:String
){
    /**
     * Returns the connection string for the mongodb client
     */
    fun getConnectionString():ConnectionString{
        return ConnectionString("mongodb://$host:$port");
    }
}