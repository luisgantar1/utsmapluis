package com.example.luisgantar_uts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.luisgantar_uts.databinding.FragmentAbsenCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AbsenCameraFragment : Fragment() {

    private var _binding: FragmentAbsenCameraBinding? = null
    private val binding get() = _binding!!
    private val CAMERA_REQUEST_CODE = 100
    private var photoUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private val args: AbsenCameraFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAbsenCameraBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.getReferenceFromUrl("gs://luisgantar-uts.appspot.com")

        // Tombol untuk mengambil foto
        binding.takePhotoButton.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }

        // Tombol untuk menyimpan absen
        binding.saveAbsenButton.setOnClickListener {
            if (photoUri != null) {
                saveAbsen()
            } else {
                Toast.makeText(context, "Photo not taken", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    // Memeriksa izin kamera
    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            takePhoto()
        }
    }

    // Fungsi untuk membuka kamera
    private fun takePhoto() {
        val photoFile: File = createImageFile()
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    // Membuat file untuk menyimpan gambar
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        )
    }

    // Fungsi setelah kamera diakses
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            binding.photoImageView.setImageURI(photoUri)
            binding.photoImageView.visibility = View.VISIBLE

            val currentTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            binding.timestampTextView.text = dateFormat.format(currentTime)
            binding.timestampTextView.visibility = View.VISIBLE

            uploadImageToFirebase(photoUri!!)

            binding.saveAbsenButton.visibility = View.VISIBLE
        }
    }

    // Mengunggah gambar ke Firebase Storage
    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        val timestamp = System.currentTimeMillis()
        val imageRef = storageRef.child("absen/$userId/$timestamp.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                photoUri = uri
                Log.d("AbsenCameraFragment", "Image URL: $photoUri")
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menyimpan absen
    private fun saveAbsen() {
        val user = auth.currentUser
        user?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Calendar.getInstance().time)
            val absenType = args.type

            // Membuat data absensi
            val absenData = hashMapOf(
                "type" to absenType,
                "timestamp" to Calendar.getInstance().time,
                "date" to today,
                "photoUri" to photoUri.toString()
            )
            db.collection("absen").document(it.uid).collection("records").add(absenData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Absen saved successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_absenCameraFragment_to_absenFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to save absen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
