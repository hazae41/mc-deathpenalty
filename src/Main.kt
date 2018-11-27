package fr.rhaz.minecraft

import fr.rhaz.minecraft.kotlin.bukkit.*
import fr.rhaz.minecraft.kotlin.catch
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class DeathPenalty: BukkitPlugin(){

    fun err(ex: Exception){ severe(ex); logToFile(ex) }

    override fun onEnable() = catch(::err){
        init(Config)
        listen<PlayerDeathEvent> {
            it.entity.withdraw()
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