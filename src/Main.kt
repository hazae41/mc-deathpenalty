package fr.rhaz.minecraft

import fr.rhaz.minecraft.kotlin.bukkit.*
import fr.rhaz.minecraft.kotlin.catch
import fr.rhaz.minecraft.kotlin.lowerCase
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class DeathPenalty: BukkitPlugin(){

    fun err(ex: Exception){ severe(ex); logToFile(ex) }

    fun Player.has(perm: String) = hasPermission("deathpenalty.$perm")

    override fun onEnable() = catch(::err){
        init(Config)
        listen<PlayerDeathEvent> {
            if(!it.entity.has("bypass"))
            it.entity.withdraw()
        }
        command("deathpenalty"){ args ->
            fun arg(i: Int) = args.getOrNull(i)?.lowerCase
            fun help(){
                msg("&c------")
                msg("&c&lDeathPenalty &7${description.version}")
                msg("&c/deathpenalty reload")
                msg("&c------")
            }
            when(arg(0)){
                "r", "reload" -> {
                    Config.reload()
                    msg("&bConfig reloaded!")
                }
                else -> help()
            }
        }
    }

    object Config: ConfigFile("config"){
        val amount by double("amount")
        val message by string("message")
    }

    val eco get() = server.servicesManager.getRegistration(Economy::class.java).provider
    val Config.parsedMessage get() = message.replace("%amount%", eco.format(amount))

    fun Player.withdraw() = eco.withdrawPlayer(this, Config.amount).apply{
        if(transactionSuccess()) msg()
        errorMessage?.let(::warning)
    }

    fun Player.msg() = Config.parsedMessage.let {
        if(it.isNotEmpty()) msg(it)
    }
}