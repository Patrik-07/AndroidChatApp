package patrik.santa.chatapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import patrik.santa.chatapp.databinding.FriendRequestItemBinding
import patrik.santa.chatapp.model.CurrentUser
import patrik.santa.chatapp.model.User

class FriendRequestAdapter(users: ArrayList<User>) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FriendRequestItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FriendRequestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private var friendRequests = users
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = friendRequests[position]
        with(viewHolder.binding) {
            usernameText.text = user.username

            acceptButton.setOnClickListener {
                CurrentUser.acceptFriendRequest(user)
                notifyItemRemoved(position)
            }

            denyButton.setOnClickListener {
                CurrentUser.denyFriendRequest(user)
                notifyItemRemoved(position)
            }
        }
    }

    fun update(users: List<User>) {
        friendRequests.clear()
        friendRequests.addAll(users)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }
}