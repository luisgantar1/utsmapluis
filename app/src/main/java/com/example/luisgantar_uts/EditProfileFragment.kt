package com.example.luisgantar_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luisgantar_uts.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            binding.editNamaEditText.setText(user.nama)
                            binding.editNimEditText.setText(user.nim)
                        }
                    }
                }

            // Event Listener untuk tombol Save
            binding.saveButton.setOnClickListener {
                val newName = binding.editNamaEditText.text.toString().trim()
                val newNim = binding.editNimEditText.text.toString().trim()

                if (newName.isEmpty() || newNim.isEmpty()) {
                    Toast.makeText(context, "Nama dan NIM tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedUser = mapOf("nama" to newName, "nim" to newNim)
                    firestore.collection("users").document(userId).update(updatedUser)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to update Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            binding.backButton.setOnClickListener {
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

