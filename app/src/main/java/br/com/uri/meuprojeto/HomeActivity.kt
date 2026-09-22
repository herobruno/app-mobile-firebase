package br.com.uri.meuprojeto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvWelcome: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        tvWelcome = findViewById(R.id.tvWelcome)
        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateWelcomeMessage()
    }

    private fun updateWelcomeMessage() {
        val user = auth.currentUser
        if (user != null) {
            val name = if (!user.displayName.isNullOrEmpty()) user.displayName else "Usuário"
            tvWelcome.text = "Bem-vindo, $name!"
        }
    }
}
