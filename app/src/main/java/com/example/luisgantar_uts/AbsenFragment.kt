package com.example.luisgantar_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luisgantar_uts.databinding.FragmentAbsenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.fragment.findNavController
import com.example.luisgantar_uts.AbsenFragmentDirections


class AbsenFragment : Fragment() {

    private var _binding: FragmentAbsenBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAbsenBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.absenMasukButton.setOnClickListener {
            checkAbsenceLimit("masuk")
        }

        binding.absenPulangButton.setOnClickListener {
            checkAbsenceLimit("pulang")
        }

        return binding.root
    }

    private fun checkAbsenceLimit(type: String) {
        val user = auth.currentUser
        user?.let {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            db.collection("absen").document(it.uid).collection("records")
                .whereEqualTo("date", today)
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        val action = AbsenFragmentDirections.actionAbsenFragmentToAbsenCameraFragment(type)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(context, "Absen $type hanya bisa dilakukan sekali sehari", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error checking absences: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
