package patrik.santa.chatapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.UserProfileBinding
import patrik.santa.chatapp.model.CurrentUser

class UserProfileFragment : Fragment() {
    private lateinit var binding: UserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserProfileBinding.inflate(inflater, container, false)
        binding.usernameText.text = "${CurrentUser.username}#${CurrentUser.code}"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_chatFragment)
        }
    }
}