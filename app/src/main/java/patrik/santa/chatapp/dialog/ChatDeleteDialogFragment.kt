package patrik.santa.chatapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import patrik.santa.chatapp.R
import patrik.santa.chatapp.model.Chat
import patrik.santa.chatapp.model.CurrentUser

class ChatDeleteDialogFragment(private val chat: Chat) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Delete all message with ${chat.with.username} ?")
                .setPositiveButton(R.string.yes) { dialog, id ->
                    CurrentUser.deleteChat(chat)
                }
                .setNegativeButton(R.string.no) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}