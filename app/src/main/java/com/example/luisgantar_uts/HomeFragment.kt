package com.example.luisgantar_uts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.luisgantar_uts.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        updateDateTimeAndGreeting()

        return binding.root
    }

    private fun updateDateTimeAndGreeting() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

        val timeText = timeFormat.format(currentTime)
        val dateText = dateFormat.format(currentTime)
        val currentUser = auth.currentUser
        currentUser?.let {
            val email = it.email
            if (email != null) {
                firestore.collection("users").whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            binding.greetingTextView.text = "User data not found"
                        } else {
                            for (document in documents) {
                                val nama = document.getString("nama") ?: "User"
                                val greetingText = getGreetingMessage(currentTime, nama)
                                binding.timeTextView.text = timeText
                                binding.dateTextView.text = dateText
                                binding.greetingTextView.text = greetingText
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("HomeFragment", "Error getting documents: ", exception)
                        binding.greetingTextView.text = "Error loading user data"
                    }
            }
        }
    }

    private fun getGreetingMessage(date: Date, nama: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 6..11 -> "Selamat Pagi, $nama"
            in 12..15 -> "Selamat Siang, $nama"
            in 16..18 -> "Selamat Sore, $nama"
            else -> "Selamat Malam, $nama"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
