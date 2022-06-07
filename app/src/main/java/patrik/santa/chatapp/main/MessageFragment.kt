package patrik.santa.chatapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import patrik.santa.chatapp.R
import patrik.santa.chatapp.adapter.MessageAdapter
import patrik.santa.chatapp.databinding.MessageBinding
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.Message

class MessageFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: MessageBinding
    lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding = MessageBinding.inflate(inflater, container, false)
        binding.missingFriendText.text = "You and ${CurrentUser.active.username} are no longer friends."
        binding.usernameText.text = CurrentUser.active.username
        val messages = ArrayList<Message>()
        val chats = CurrentUser.chats
        for (i in 0 until CurrentUser.chats.size) {
            if (chats[i].with == CurrentUser.active) {
                messages.addAll(chats[i].messages)
            }
        }
        val layout = LinearLayoutManager(context)
        layout.stackFromEnd = true
        binding.recyclerView.layoutManager = layout
        adapter = MessageAdapter(messages)
        binding.recyclerView.adapter = adapter
        CurrentUser.messageFragment = this

        updateUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            sendButton.setOnClickListener {
                if (!inputText.text.isNullOrEmpty()) {
                    CurrentUser.sendMessage(inputText.text.toString())
                    inputText.text.clear()
                }
            }

            binding.backButton.setOnClickListener {
                findNavController().navigate(R.id.action_messageFragment_to_chatFragment)
            }
        }
    }

    fun updateUI() {
        val user = db.collection("users").document(CurrentUser.id)
        user.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CurrentUser.friends = CurrentUser.getFieldAsStringArray(task.result, "friends")
            }
        }

        with(binding) {
            recyclerView.scrollToPosition(adapter.itemCount)
            if (CurrentUser.friends.contains(CurrentUser.active)) {
                sendInput.visibility = View.VISIBLE
                missingFriendText.visibility = View.GONE
            } else {
                sendInput.visibility = View.GONE
                missingFriendText.visibility = View.VISIBLE
            }
        }
    }
}