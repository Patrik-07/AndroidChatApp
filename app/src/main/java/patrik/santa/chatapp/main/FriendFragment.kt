package patrik.santa.chatapp.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import patrik.santa.chatapp.R
import patrik.santa.chatapp.adapter.FriendAdapter
import patrik.santa.chatapp.adapter.FriendRequestAdapter
import patrik.santa.chatapp.databinding.MainBinding
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.User

class FriendFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: MainBinding
    lateinit var adapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding = MainBinding.inflate(inflater, container, false)
        binding.noFriendsText.text = getText(R.string.noFriends)
        binding.noFriendsImage.setImageResource(R.drawable.ic_baseline_emoji_emotions_24)
        binding.mainText.text = getString(R.string.friendsLabel)
        binding.mainButton.setImageResource(R.drawable.ic_baseline_person_add_24)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FriendAdapter(CurrentUser.friends)
        binding.recyclerView.adapter = adapter
        CurrentUser.friendFragment = this

        val user = db.collection("users").document(CurrentUser.id)
        user.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CurrentUser.onFriendRequestsChanged(task.result)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mainButton.setOnClickListener {
                findNavController().navigate(R.id.action_friendFragment_to_friendAddFragment)
            }

            searchBar.setOnQueryTextListener(
                object: SearchView.OnQueryTextListener, View.OnFocusChangeListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        val list = ArrayList<User>()
                        for (f in CurrentUser.friends) {
                            if (f.username.startsWith(p0.toString())) {
                                list.add(f)
                            }
                        }
                        adapter.update(list)
                        return true
                    }

                    override fun onFocusChange(p0: View?, p1: Boolean) { }
                }
            )

            chatButton.setOnClickListener {
                findNavController().navigate(R.id.action_friendFragment_to_chatFragment)
            }

            logoutButton.setOnClickListener {
                auth.signOut()
                findNavController().navigate(R.id.action_friendFragment_to_loginFragment)
            }
        }
    }

    fun updateUI() {
        val user = db.collection("users").document(CurrentUser.id)
        user.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CurrentUser.friends = CurrentUser.getFieldAsStringArray(task.result, "friends")
                adapter.update(CurrentUser.friends)
            }
        }
        with(binding) {
            if (CurrentUser.friends.isEmpty()) {
                recyclerView.visibility = View.GONE
                noFriends.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                noFriends.visibility = View.GONE
            }

            if (CurrentUser.friendRequests.isEmpty()) {
                mainButton.backgroundTintList = ColorStateList.valueOf(
                    resources.getColor(R.color.gray)
                )
            } else {
                mainButton.backgroundTintList = ColorStateList.valueOf(
                    resources.getColor(R.color.holo_green)
                )
            }
        }
    }
}