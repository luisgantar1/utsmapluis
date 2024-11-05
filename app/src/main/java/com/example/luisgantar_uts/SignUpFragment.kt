package com.example.luisgantar_uts

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luisgantar_uts.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        binding.signUpButton.setOnClickListener {
            val nama = binding.namaEditText.text.toString()
            val nim = binding.nimEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Validasi input
            if (nama.isNotEmpty() && nim.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (nim.length > 11) {
                    Toast.makeText(context, "NIM tidak boleh lebih dari 11 angka", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Buat akun di Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val user = User(nama, nim, email, password)

                            if (userId != null) {
                                database.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            firestore.collection("users").document(userId).set(user)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Account created successfully. Please login.", Toast.LENGTH_SHORT).show()
                                                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to save user data in Firestore", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Failed to save user data in Realtime Database", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        val loginClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
        }
        val loginText = "Already have an account? Login"
        val spannableString = SpannableString(loginText)
        val loginStart = loginText.indexOf("Login")
        val loginEnd = loginStart + "Login".length
        spannableString.setSpan(
            loginClickableSpan,
            loginStart,
            loginEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.loginTextView.text = spannableString
        binding.loginTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}