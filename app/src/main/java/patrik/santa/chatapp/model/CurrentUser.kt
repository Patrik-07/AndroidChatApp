package patrik.santa.chatapp.model

import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import patrik.santa.chatapp.main.ChatFragment
import patrik.santa.chatapp.main.FriendFragment
import patrik.santa.chatapp.main.FriendRequestFragment
import patrik.santa.chatapp.main.MessageFragment
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt
import kotlin.system.exitProcess

object CurrentUser {
    lateinit var username: String
    lateinit var id: String
    lateinit var code: String

    lateinit var active: User

    var friends = ArrayList<User>()
    var friendRequests = ArrayList<User>()
    var chats = ArrayList<Chat>()

    var friendFragment: FriendFragment? = null
    var friendRequestFragment: FriendRequestFragment? = null
    var chatFragment: ChatFragment? = null
    var messageFragment: MessageFragment? =  null

    fun init(name: String, id: String) {
        this.username = name
        this.id = id

        val db = Firebase.firestore
        db.collection("users").whereEqualTo("id", id)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == true) {
                        this.code = generateCode(id)
                        db.collection("users").document(id).set(
                            hashMapOf(
                                "id" to id,
                                "username" to name,
                                "code" to code,
                                "friends" to null,
                                "friendRequests" to null,
                            )
                        )
                    } else {
                        val user = db.collection("users").document(id)
                        user.get().addOnCompleteListener { t1 ->
                            if (task.isSuccessful) {
                                this.code = t1.result?.getString("code").toString()
                                friends = getFieldAsStringArray(t1.result, "friends")
                                friendRequests = getFieldAsStringArray(t1.result, "friendsRequests")
                                val chat = user.collection("chats")
                                chat.get().addOnSuccessListener { documents ->
                                    chats = getChats(documents)
                                }
                            }
                        }
                    }
                }
            }

        db.collection("users").document(id).addSnapshotListener { value, _ ->
            onFriendRequestsChanged(value)
            onFriendsChanged(value)
        }

        val user = db.collection("users").document(id)
        user.collection("chats").addSnapshotListener { value, _ ->
            value?.let { onChatsChanged(it) }
        }
    }

    fun deleteChat(chat: Chat) {
        val query = Firebase.firestore.collection("users").document(id)
        query.collection("chats").document(chat.with.id).delete()

        chats.remove(chat)
    }

    fun sendMessage(data: String) {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Message(
                id,
                data,
                Timestamp.now()
            )
        } else exitProcess(0)

        val current = User(username, id, code)
        setMessage(current, active, message)
        setMessage(active, current, message)

        for (i in 0 until chats.size) {
            if (chats[i].with.id == active.id) {
                chats[i].messages.add(message)
                return
            }
        }
    }

    private fun setMessage(from: User, to: User, message: Message) {
        val user = Firebase.firestore.collection("users").document(from.id)
        val chat = user.collection("chats").document(to.id)
        chat.get().addOnCompleteListener { t1 ->
            if (t1.isSuccessful) {
                if (t1.result?.exists() == false)
                    chat.set(
                        hashMapOf(
                            "with" to to,
                            "message" to null
                        )
                    )
                chat.update("message", FieldValue.arrayUnion(message))
            }
        }
    }

    fun acceptFriendRequest(user: User) {
        var query = Firebase.firestore.collection("users").document(id)
        query.update("friends", FieldValue.arrayUnion(user))
        query.update("friendRequests", FieldValue.arrayRemove(user))

        val currentUser = User(username, id, code)
        query = Firebase.firestore.collection("users").document(user.id)
        query.update("friends", FieldValue.arrayUnion(currentUser))
        query.update("friendRequests", FieldValue.arrayRemove(currentUser))

        friends.add(user)
        friendRequests.remove(user)
    }

    fun denyFriendRequest(user: User) {
        val query = Firebase.firestore.collection("users").document(id)
        query.update("friendRequests", FieldValue.arrayRemove(user))
        friendRequests.remove(user)
    }

    fun removeFriend(user: User) {
        var query = Firebase.firestore.collection("users").document(id)
        query.update("friends", FieldValue.arrayRemove(user))

        val currentUser = User(username, id, code)
        query = Firebase.firestore.collection("users").document(user.id)
        query.update("friends", FieldValue.arrayRemove(currentUser))

        friends.remove(user)
    }

    fun onChatsChanged(value: QuerySnapshot) {
        chats = getChats(value)
        var ok = true
        for (c in chats)
            if (c.messages.isEmpty())
                ok = false
        if (ok) {
            if (chatFragment?.isVisible == true) {
                chats.sortBy {
                    it.lastModifyDate()
                }
                chatFragment?.updateUI()
                chatFragment?.adapter?.update(chats.reversed())
            }
            if (messageFragment?.isVisible == true) {
                val messages = ArrayList<Message>()
                val chats = CurrentUser.chats
                for (i in 0 until CurrentUser.chats.size) {
                    if (chats[i].with == active) {
                        messages.addAll(chats[i].messages)
                    }
                }
                messageFragment?.updateUI()
                messageFragment?.adapter?.update(messages)
            }
        }
    }

    fun onFriendRequestsChanged(value: DocumentSnapshot?) {
        friendRequests = getFieldAsStringArray(value, "friendRequests")
        if (friendFragment?.isVisible == true) {
            friendFragment?.updateUI()
        }
        if (friendRequestFragment?.isVisible == true) {
            friendRequestFragment?.updateUI()
            friendRequestFragment?.adapter?.update(friendRequests)
        }
    }

    fun onFriendsChanged(value: DocumentSnapshot?) {
        friends = getFieldAsStringArray(value, "friends")
        if (friendFragment?.isVisible == true) {
            friendFragment?.updateUI()
            friendFragment?.adapter?.update(friends)
        }
        if (messageFragment?.isVisible == true) {
            messageFragment?.updateUI()
        }
    }

    fun getChats(value: QuerySnapshot): ArrayList<Chat> {
        val chats = ArrayList<Chat>()
        for (document in value) {
            val messages = ArrayList<Message>()
            val list = document?.get("message")
            if (list != null) {
                val messageList = list as ArrayList<HashMap<String, String>>
                for (m in messageList) {
                    messages.add(
                        Message(
                            m["sender"].toString(),
                            m["data"].toString(),
                            m["datetime"] as Timestamp
                        )
                    )
                }
            }
            val u = document?.get("with") as HashMap<String, String>
            val c = Chat()
            c.with = User(
                u["username"].toString(),
                u["id"].toString(),
                u["code"].toString()
            )
            c.messages.addAll(messages)
            chats.add(c)
        }
        return chats
    }

    fun getFieldAsStringArray(snapshot: DocumentSnapshot?, field: String): ArrayList<User> {
        val list = snapshot?.get(field)
        return if (list != null) {
            val returnUserList = ArrayList<User>()
            val users = list as ArrayList<HashMap<String, String>>
            for (u in users) {
                returnUserList.add(
                    User(
                        u["username"].toString(),
                        u["id"].toString(),
                        u["code"].toString(),
                    )
                )
            }
            returnUserList
        } else ArrayList()
    }

    private fun generateCode(id: String): String {
        val digits: MutableList<String> = ArrayList()
        var start = 0
        while (start < id.length) {
            val s = id.substring(start, id.length.coerceAtMost(start + 7))
            var digit = 0.0
            for (c in s) {
                var i = c.code.toDouble()
                if (i >= 100) {
                    i /= 100
                } else if (i >= 10) {
                    i /= 10
                }
                i /= 7
                digit += i
            }

            digits.add(digit.roundToInt().toString())
            start += 7
        }

        var r = ""
        for (s in digits)
            r += s

        return r
    }
}