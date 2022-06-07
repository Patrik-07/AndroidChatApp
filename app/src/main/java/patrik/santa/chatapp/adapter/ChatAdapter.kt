package patrik.santa.chatapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.ChatItemBinding
import patrik.santa.chatapp.dialog.ChatDeleteDialogFragment
import patrik.santa.chatapp.model.Chat
import patrik.santa.chatapp.model.CurrentUser

class ChatAdapter(chatList: ArrayList<Chat>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private var chats = chatList
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val chat = chats[position]
        with(viewHolder.binding) {
            val lastMessage = chat.messages.last()
            usernameText.text = chat.with.username
            if (lastMessage.sender == chat.with.id)
                userMessageText.text = lastMessage.data
            else userMessageText.text = "You: ${lastMessage.data}"

            deleteChatButton.setOnClickListener {
                CurrentUser.chatFragment?.childFragmentManager?.let { it1 ->
                    ChatDeleteDialogFragment(chat).show(
                        it1, ""
                    )
                }
            }
        }

        viewHolder.itemView.setOnClickListener {
            CurrentUser.active = chat.with
            CurrentUser.chatFragment?.findNavController()?.navigate(R.id.action_chatFragment_to_messageFragment)
        }
    }

    fun update(chatList: List<Chat>) {
        chats.clear()
        chats.addAll(chatList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}