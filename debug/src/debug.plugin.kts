import org.apollo.game.message.impl.NpcActionMessage
import org.apollo.game.message.impl.ObjectActionMessage

on { ObjectActionMessage::class }
        .then { println("Player interacted with object $id") }

on { NpcActionMessage::class }
        .then {
            val npc = it.world.npcRepository[index]
            println("Player interacted with npc " + npc.id)

        }