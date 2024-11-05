package com.example.luisgantar_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luisgantar_uts.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var historyAdapter: HistoryAdapter
    private var absences = mutableListOf<Absen>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        historyAdapter = HistoryAdapter(absences)
        binding.recyclerView.adapter = historyAdapter
        loadAbsenHistory("masuk")
        binding.absenMasukButton.setOnClickListener {
            loadAbsenHistory("masuk")
        }
        binding.absenPulangButton.setOnClickListener {
            loadAbsenHistory("pulang")
        }

        return binding.root
    }

    // Load History (masuk/pulang)
    private fun loadAbsenHistory(type: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("absen").document(it.uid).collection("records")
                .whereEqualTo("type", type)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    absences.clear()
                    for (document in documents) {
                        val photoUri = document.getString("photoUri") ?: ""
                        val timestamp = document.getDate("timestamp")
                        if (timestamp != null) {
                            absences.add(Absen(type, timestamp, photoUri))
                        }
                    }
                    historyAdapter.updateData(absences)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load history: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}