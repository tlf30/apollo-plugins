import org.apollo.game.message.handler.ItemVerificationHandler
import org.apollo.game.message.impl.*
import org.apollo.game.model.entity.Player
import org.apollo.game.model.inv.Inventory
import org.apollo.game.scheduling.ScheduledTask

start {
    ItemVerificationHandler.addInventory(Interface.COLUMN_0, PlayerInventorySupplier(Interface.COLUMN_0))
    ItemVerificationHandler.addInventory(Interface.COLUMN_1, PlayerInventorySupplier(Interface.COLUMN_1))
    ItemVerificationHandler.addInventory(Interface.COLUMN_2, PlayerInventorySupplier(Interface.COLUMN_2))
    ItemVerificationHandler.addInventory(Interface.COLUMN_3, PlayerInventorySupplier(Interface.COLUMN_3))
    ItemVerificationHandler.addInventory(Interface.COLUMN_4, PlayerInventorySupplier(Interface.COLUMN_4))
}

//Listeners for smelting
data class SmeltingWrapper(val player: Player, val bar: Bar)

on { ObjectActionMessage::class }
        .where { furnaces.contains(id) }
        .then {
            //println("Furnace interaction")
            it.startAction(OpenFurnaceAction(it, position))
            terminate()
        }

on { ButtonMessage::class }
        .where { FurnaceSelection.values().any { it.widget == widgetId } }
        .then {
            //println("Furnace select interaction")
            println(widgetId)
            //close ui for bar selection
            it.send(CloseInterfaceMessage())
            //Open ui for quantaty
            it.send(EnterAmountMessage())
            val bar = FurnaceSelection.values().first { it.widget == widgetId }.bar
            waitingForAmount.add(SmeltingWrapper(it, bar))
            //
            terminate()
        }

val waitingForAmount = HashSet<SmeltingWrapper>()

on { EnteredAmountMessage::class }
        .then {
            val player = it
            if (waitingForAmount.any {it.player == player } ) {
                //println("Got amount from player: " + amount)
                val wrapper = waitingForAmount.first { it.player == player }
                waitingForAmount.remove(wrapper)
                //Run smelt action
                it.startAction(SmeltingAction(it, wrapper.bar, amount))
                //
                terminate()
            }
        }

//Listeners for smithing
on { ItemOnObjectMessage::class }
        .where { Smithable.values().any { it.bar.id == id } && anvils.any{ it == objectId } }
        .then {
            //println("Anvil interaction" + objectId)
            val bar = Bar.values().first { it.id == id }
            it.startAction(OpenSmithingAction(it, position, bar))
            terminate()
        }

on { ItemActionMessage::class }
        .where { interfaceId == Interface.COLUMN_0 || interfaceId == Interface.COLUMN_1 ||interfaceId == Interface.COLUMN_2 ||interfaceId == Interface.COLUMN_3 ||interfaceId == Interface.COLUMN_4}
        .then {
            val amount = amountFromOption(option)
            val item = getSmithingItem(id)
            if (item != null) {
                it.interfaceSet.close()
                it.startAction(SmithingAction(it, item, amount))
                terminate()
            }

        }
