package br.com.uri.meuprojeto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etProfileEmail = findViewById<EditText>(R.id.etProfileEmail)
        val etProfileName = findViewById<EditText>(R.id.etProfileName)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        val user = auth.currentUser
        if (user == null) {
            finish()
            return
        }

        etProfileEmail.setText(user.email ?: "")

        // Tenta carregar o nome de exibição do Auth ou do Firestore
        if (!user.displayName.isNullOrEmpty()) {
            etProfileName.setText(user.displayName)
        } else {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        if (!name.isNullOrEmpty()) {
                            etProfileName.setText(name)
                        }
                    }
                }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSaveProfile.setOnClickListener {
            val newName = etProfileName.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Informe um nome válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveProfile.isEnabled = false

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        // Atualiza também no Firestore
                        val updates = mapOf<String, Any>(
                            "name" to newName,
                        )

                        db.collection("users").document(user.uid)
                            .update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao atualizar no banco: ${e.message}", Toast.LENGTH_LONG).show()
                                btnSaveProfile.isEnabled = true
                            }
                    } else {
                        Toast.makeText(this, "Erro ao atualizar perfil: ${profileTask.exception?.message}", Toast.LENGTH_LONG).show()
                        btnSaveProfile.isEnabled = false
                    }
                }
        }
    }
}
