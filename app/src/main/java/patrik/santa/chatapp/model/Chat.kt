package patrik.santa.chatapp.model

import java.util.*
import kotlin.collections.ArrayList

class Chat {
    lateinit var with: User
    val messages = ArrayList<Message>()

    fun lastModifyDate(): Date {
        return messages.last().datetime.toDate()
    }
}