package patrik.santa.chatapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.MainBinding
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import patrik.santa.chatapp.adapter.ChatAdapter
import patrik.santa.chatapp.model.Chat
import patrik.santa.chatapp.model.CurrentUser

class ChatFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: MainBinding
    lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding = MainBinding.inflate(inflater, container, false)
        binding.mainText.text = getString(R.string.chatLabel)
        binding.mainButton.setImageResource(R.drawable.ic_baseline_account_circle_24)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ChatAdapter(CurrentUser.chats)
        binding.recyclerView.adapter = adapter
        CurrentUser.chatFragment = this

        val user = db.collection("users").document(CurrentUser.id)
        val chat = user.collection("chats")
        chat.get().addOnSuccessListener { documents ->
            CurrentUser.onChatsChanged(documents)
        }

        updateUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mainButton.setOnClickListener {
                findNavController().navigate(R.id.action_chatFragment_to_userProfileFragment)
            }

            searchBar.setOnQueryTextListener(
                object: SearchView.OnQueryTextListener, View.OnFocusChangeListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        val list = ArrayList<Chat>()
                        for (c in CurrentUser.chats) {
                            if (c.with.username.startsWith(p0.toString())) {
                                list.add(c)
                            }
                        }
                        adapter.update(list)
                        return true
                    }

                    override fun onFocusChange(p0: View?, p1: Boolean) { }
                }
            )

            friendsButton.setOnClickListener {
                findNavController().navigate(R.id.action_chatFragment_to_friendFragment)
            }

            logoutButton.setOnClickListener {
                auth.signOut()
                findNavController().navigate(R.id.action_chatFragment_to_loginFragment)
            }
        }
    }

    fun updateUI() {
        with(binding) {
            if (CurrentUser.chats.isEmpty()) {
                recyclerView.visibility = View.GONE
                noFriends.visibility = View.VISIBLE
                if (CurrentUser.friends.isEmpty()) {
                    noFriendsImage.setImageResource(R.drawable.ic_baseline_emoji_emotions_24)
                    noFriendsText.text = getString(R.string.noFriends)
                } else {
                    noFriendsImage.setImageResource(R.drawable.ic_baseline_draw_24)
                    noFriendsText.text = getString(R.string.noChats)
                }
            } else {
                recyclerView.visibility = View.VISIBLE
                noFriends.visibility = View.GONE
            }
        }
    }
}