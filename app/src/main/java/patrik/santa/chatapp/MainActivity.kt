package patrik.santa.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import patrik.santa.chatapp.databinding.ActivityMainBinding
import patrik.santa.chatapp.model.CurrentUser

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        auth = Firebase.auth

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        val db = Firebase.firestore

        if (auth.currentUser != null) {
            val name = auth.currentUser?.displayName.toString()
            val id = auth.currentUser?.uid.toString()
            CurrentUser.init(name, id)

            val navController = findNavController(R.id.nav_host_fragment)
            val navGraph = navController.navInflater.inflate(R.navigation.navigation)
            navGraph.startDestination = R.id.chatFragment
            navController.graph = navGraph
        }
    }

    private fun documentReference(current: DocumentReference?, path: String): DocumentReference? {
        var document: DocumentReference? = null
        current?.get()?.continueWith { task ->
            if (task.isSuccessful) {
                document = task.result?.getDocumentReference(path)!!
            }
        }
        return document
    }
}