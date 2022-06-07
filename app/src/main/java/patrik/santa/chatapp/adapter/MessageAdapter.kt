package patrik.santa.chatapp.adapter

import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.Shader
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.ChatItemBinding
import patrik.santa.chatapp.databinding.MessageItemBinding
import patrik.santa.chatapp.dialog.ChatDeleteDialogFragment
import patrik.santa.chatapp.model.Chat
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.Message

class MessageAdapter(messageList: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private var messages = messageList
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = messages[position]
        with(viewHolder.binding) {
            val m = SpannableString(message.data)
            if (message.sender == CurrentUser.id) {
                m.setSpan(BackgroundColorSpan(Color.parseColor("#0000ff")), 0, message.data.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                messageText.gravity = Gravity.RIGHT
            } else {
                m.setSpan(BackgroundColorSpan(Color.parseColor("#ff0000")), 0, message.data.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                messageText.gravity = Gravity.LEFT
            }
            messageText.text = m
        }
    }

    fun update(messageList: List<Message>) {
        messages.clear()
        messages.addAll(messageList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}