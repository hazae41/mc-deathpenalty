package fr.rhaz.minecraft

import fr.rhaz.minecraft.kotlin.bukkit.*
import fr.rhaz.minecraft.kotlin.catch
import fr.rhaz.minecraft.kotlin.lowerCase
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class DeathPenalty: BukkitPlugin(){

    fun err(ex: Exception){ severe(ex); logToFile(ex) }

    fun Player.has(perm: String) = hasPermission("deathpenalty.$perm")

    override fun onEnable() = catch(::err){
        update(62767)
        init(Config)
        listen<PlayerDeathEvent> {
            val player = it.entity
            if(!player.has("bypass"))
            player.withdraw()
        }
        command("deathpenalty"){ args ->
            fun arg(i: Int) = args.getOrNull(i)?.lowerCase
            fun help(){
                msg("&c------")
                msg("&c&lDeathPenalty &7${description.version}")
                msg("&c/deathpenalty reload")
                msg("&c/deathpenalty set amount <amount>[%]")
                msg("&c/deathpenalty set message <message>")
                msg("&c------")
            }
            when(arg(0)){
                "r", "reload" -> {
                    Config.reload()
                    msg("&bConfig reloaded!")
                }
                "set" -> when(arg(1)){
                    "amount" -> {
                        Config.amount = arg(2) ?: "0"
                        msg("&bSet amount to: ${Config.amount}")
                    }
                    "message" -> {
                        Config.message = args.drop(2).joinToString(" ")
                        msg("&bSet message to: ${Config.message}")
                    }
                    else -> help()
                }
                else -> help()
            }
        }
    }

    object Config: ConfigFile("config"){
        var amount by string("amount")
        var message by string("message")
    }

    fun parse(amount: String) =
        if(amount.endsWith("%"))
            Pair(amount.dropLast(1).toDouble(), "percent")
        else
            Pair(amount.toDouble(), "fixed")

    val eco get() = server.servicesManager.getRegistration(Economy::class.java).provider

    fun Player.withdraw() = eco.withdrawPlayer(this, amount).apply{
        if(transactionSuccess()) msg(amount)
    }

    fun Player.msg(amount: Double) = Config.message.let {
        if(it.isNotEmpty()) msg(it.replace(amount))
    }

    val Player.amount: Double get() {
        val (amount, type) = parse(Config.amount)
        return when(type) {
            "fixed" -> amount
            "percent" -> amount * eco.getBalance(this) / 100
            else -> 0.0
        }
    }

    fun String.replace(amount: Double) = replace("%amount%", eco.format(amount))
}