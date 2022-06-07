package patrik.santa.chatapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.FriendItemBinding
import patrik.santa.chatapp.dialog.FriendDeleteDialogFragment
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.User

class FriendAdapter(users: ArrayList<User>) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FriendItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private var friends = users
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = friends[position]
        with(viewHolder.binding) {
            usernameText.text = user.username

            deleteFriendButton.setOnClickListener {
                CurrentUser.friendFragment?.childFragmentManager?.let { it1 ->
                    FriendDeleteDialogFragment(user).show(
                        it1, ""
                    )
                }
            }
        }

        viewHolder.itemView.setOnClickListener {
            CurrentUser.active = user
            CurrentUser.friendFragment?.findNavController()?.navigate(R.id.action_friendFragment_to_messageFragment)
        }
    }

    fun update(users: List<User>) {
        friends.clear()
        friends.addAll(users)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}