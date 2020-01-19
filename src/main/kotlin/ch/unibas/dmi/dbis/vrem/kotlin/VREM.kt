package ch.unibas.dmi.dbis.vrem.kotlin

import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.ImplicitReflectionSerializer

class VREM():CliktCommand(name="vrem", help = "This is the virtual-exhibition-manager (VREM)"){
    /* just the top level command */
    override fun run() = Unit
}

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
fun main(args:Array<String>) = VREM().subcommands(APIEndpoint()).main(args)