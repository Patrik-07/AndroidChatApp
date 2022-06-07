package patrik.santa.chatapp.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.RegisterBinding
import patrik.santa.chatapp.model.CurrentUser

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: RegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }

        binding = RegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            registerButton.setOnClickListener {
                onRegisterButton()
            }

            cancelButton.setOnClickListener {
                onCancelButton()
            }
        }
    }

    private fun onRegisterButton() {
        with(binding) {
            val username = usernameText.text.toString()
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val passwordAgain = passwordAgainText.text.toString()

            if (validateForm(username, email, password, passwordAgain)) {
                createUser(email, password)
            }
        }
    }

    private fun onCancelButton() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }

    private fun validateForm(username: String, email: String, password: String, passwordAgain: String): Boolean {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordAgain)) {
                Snackbar.make(binding.root, "Please, fill all in the fields!", Snackbar.LENGTH_LONG).show()
                return false
        } else if (password != passwordAgain) {
            binding.passwordAgainText.error = "Passwords does not match."
            return false
        }
        return true
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = binding.usernameText.text.toString()
                    }
                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            val name = auth.currentUser?.displayName.toString()
                            val id = auth.currentUser?.uid.toString()
                            CurrentUser.init(name, id)

                            findNavController().navigate(R.id.action_registerFragment_to_chatFragment)
                        }
                    }
                } else {
                    try {
                        throw task.exception!!
                    } catch (exception: FirebaseAuthException) {
                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                binding.passwordText.error = exception.message
                            }

                            is FirebaseAuthInvalidCredentialsException,
                            is FirebaseAuthUserCollisionException -> {
                                binding.emailText.error = exception.message
                            }
                        }
                    }
                }
            }
    }
}