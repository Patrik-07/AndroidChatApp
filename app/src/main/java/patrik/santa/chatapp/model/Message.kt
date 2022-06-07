package patrik.santa.chatapp.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Message(
    val sender: String,
    val data: String,
    val datetime: Timestamp
) : Serializable