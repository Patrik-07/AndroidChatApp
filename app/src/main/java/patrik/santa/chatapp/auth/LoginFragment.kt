package patrik.santa.chatapp.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import patrik.santa.chatapp.R
import patrik.santa.chatapp.databinding.LoginBinding
import patrik.santa.chatapp.model.CurrentUser

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: LoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()

        binding = LoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            onRegisterButton()
        }

        binding.loginButton.setOnClickListener {
            onLoginButton()
        }
    }

    private fun onLoginButton() {
        with(binding) {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (validateForm(email, password)) {
                loginUser(email, password)
            }
        }
    }

    private fun onRegisterButton() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun validateForm(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(binding.root, "Please, fill all in the fields!", Snackbar.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val name = auth.currentUser?.displayName.toString()
                    val id = auth.currentUser?.uid.toString()
                    CurrentUser.init(name, id)

                    findNavController().navigate(R.id.action_loginFragment_to_chatFragment)
                } else {
                    try {
                        throw task.exception!!
                    } catch (exception: FirebaseAuthException) {
                        when (exception) {
                            is FirebaseAuthInvalidUserException ->
                                binding.emailText.error = exception.message

                            is FirebaseAuthInvalidCredentialsException -> {
                                if (exception.errorCode == "ERROR_INVALID_EMAIL")
                                    binding.emailText.error = exception.message
                                else binding.passwordText.error = exception.message
                            }
                        }
                    }
                }
            }
    }
}