package patrik.santa.chatapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import patrik.santa.chatapp.R
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.User

class FriendDeleteDialogFragment(private val user: User) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Remove ${user.username} from your friends?")
                .setPositiveButton(R.string.yes) { dialog, id ->
                    CurrentUser.removeFriend(user)
                }
                .setNegativeButton(R.string.no) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}