package ch.unibas.dmi.dbis.vrem.kotlin.importer

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.system.exitProcess

class ExhibitionFolderImporter : CliktCommand(name="import-folder", help="Imports a folder-based exhibition"){

    private val LOGGER = LogManager.getLogger(ExhibitionFolderImporter::class.java)

    val exhibitionPath by option("-p", "--path", help = "Path to the exhibition root folder").required()
    val config by option("-c", "--config", help="Relative of full path to the config file to be used").required()
    val clean by option("--clean", help="Remove old exhibitions with the same name").flag("--keep", default = false)
    val exhibitionDescription by option("-d", "--description", help="Description of the exhibition").default("")
    val name by option("-n", "--name", help="The name of the exhibition. Shall be unique").default("default-name")

    override fun run() {
        val config = APIEndpoint.readConfig(this.config)
        val (reader, writer) = APIEndpoint.getDAOs(config.database)
        val exhibitionFolder = File(exhibitionPath)
        if(!exhibitionFolder.exists() and !exhibitionFolder.isDirectory){
            LOGGER.error("--path argument has to" + " point to an existing directory")
            exitProcess(-1)
       }
        if(reader.existsExhibition(name)){
            if(!clean){
                LOGGER.error("An exhibition with $name already exists. ")
                exitProcess(-2)
            }
            writer.deleteExhibition(name)
            LOGGER.info("Successfully removed previously existing exhibiiton '$name' ")
        }

        val exhibition = Exhibition(name= name, description = exhibitionDescription)

    }
}