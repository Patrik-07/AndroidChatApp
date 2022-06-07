package patrik.santa.chatapp.main

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import patrik.santa.chatapp.R
import patrik.santa.chatapp.adapter.FriendRequestAdapter
import patrik.santa.chatapp.databinding.FriendRequestBinding
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.User

class FriendRequestFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FriendRequestBinding
    lateinit var adapter: FriendRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding = FriendRequestBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FriendRequestAdapter(CurrentUser.friendRequests)
        binding.recyclerView.adapter = adapter
        CurrentUser.friendRequestFragment = this

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

        updateUI()
        with(binding) {
            friendRequestButton.setOnClickListener {
                onFriendRequest()
            }

            backButton.setOnClickListener {
                findNavController().navigate(R.id.action_friendAddFragment_to_friendFragment)
            }
        }
    }

    private fun onFriendRequest() {
        with(binding) {
            val username = usernameText.text.toString()
            val userCode = userIDText.text.toString()

            if (validate(username, userCode)) {
                sendFriendRequest(username, userCode)
            }
        }
    }

    private fun validate(username: String, userCode: String): Boolean {
        if (TextUtils.isEmpty(username)) {
            binding.usernameText.error = "Please, add username."
            return false
        } else if (TextUtils.isEmpty(userCode) || userCode.length > 4) {
            binding.userIDText.error = "Please, add all four digits."
            return false
        } else if (username == CurrentUser.username && userCode == CurrentUser.code) {
            binding.usernameText.error = "You can't friend yourself."
            return false
        }
        return true
    }

    private fun sendFriendRequest(username: String, userCode: String) {
        val query = db.collection("users")
            .whereEqualTo("username", username)
            .whereEqualTo("code", userCode)

        query.get().addOnCompleteListener { t1 ->
            if (t1.isSuccessful) {
                val result = t1.result?.documents
                Log.d("ASD", result.toString())
                if (result?.isEmpty() == false) {
                    val userId = result[0].getString("id").toString()
                    var isFriend = false
                    for (friend in CurrentUser.friends)
                        if (friend.id == userId) {
                            isFriend = true
                            break
                        }
                    if (!isFriend) {
                        val newFriendRequest = User(
                            CurrentUser.username,
                            CurrentUser.id,
                            CurrentUser.code
                        )
                        val user = db.collection("users").document(userId)
                        user.update("friendRequests", FieldValue.arrayUnion(newFriendRequest))
                            .addOnCompleteListener { t2 ->
                                if (t2.isSuccessful) {
                                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(view?.windowToken, 0)
                                    binding.usernameText.text.clear()
                                    binding.userIDText.text.clear()
                                    Snackbar.make(binding.root, "Friend request sent.", Snackbar.LENGTH_LONG).show()
                                }
                            }
                    } else binding.usernameText.error = "You and $username are already friends."
                } else binding.usernameText.error = "There is no such user, check the 4 digit code."
            }
        }
    }

    fun updateUI() {
        val user = db.collection("users").document(CurrentUser.id)
        user.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CurrentUser.friendRequests = CurrentUser.getFieldAsStringArray(task.result, "friendRequests")
                adapter.update(CurrentUser.friendRequests)
            }
        }
        with(binding) {
            if (CurrentUser.friendRequests.isEmpty()) {
                recyclerView.visibility = View.GONE
                requestPendingText.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                requestPendingText.visibility = View.VISIBLE
            }
        }
    }
}