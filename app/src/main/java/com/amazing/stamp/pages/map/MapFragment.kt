package com.amazing.stamp.pages.map

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.values
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentFragment
import com.devs.vectorchildfinder.VectorChildFinder
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding
import com.example.stamp.databinding.FragmentMapBinding
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.richpath.RichPathView


class MapFragment : ParentFragment() {
    private lateinit var binding: FragmentMapBinding
    private var currentLocation: String? = null
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)


        val vector =
            VectorChildFinder(requireContext(), R.drawable.ic_korea_map_merged, binding.ivKorea)
        val cheonan = vector.findPathByName("충청남도 천안시")

        cheonan.fillColor = requireActivity().getColor(R.color.main_color_100)

        binding.ivKorea.invalidate()

        binding.run {
            btnMapLocation.setOnClickListener {
                currentLocationSet()
            }
        }

        return binding.root
    }


    private fun showLocationPopup() {

        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_current_location)

        dialog.findViewById<TextView>(R.id.tv_dialog_current_location).text = currentLocation

        dialog.show()
        dialog.findViewById<Button>(R.id.btn_register_current_location).setOnClickListener {

            if(currentLocation == null || currentLocation!!.isEmpty()) return@setOnClickListener

            val currentLocationSplit = currentLocation?.split(" ")
            fireStore.collection(FirebaseConstants.COLLECTION_TRIP_LOCATION)
                .document(auth.currentUser!!.uid)
                .update(
                    FirebaseConstants.TRIP_LOCATION_FIELD_VISITED,
                    FieldValue.arrayUnion(
                        "${currentLocationSplit?.get(0)} ${currentLocationSplit?.get(1)}"
                    )
                )

            dialog.dismiss()
        }
    }


    private fun currentLocationSet() {

        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            val geocoder = Geocoder(requireContext())
            val address = geocoder.getFromLocation(it.latitude, it.longitude, 10)

            if (address.size == 0) showShortToast("주소 찾기 오류")
            else {
                // 반환 예시) 대한민국 충청남도 천안시 서북구 두정역동0길 00
                currentLocation = address[0].getAddressLine(0).replaceFirst("대한민국 ", "")
                showLocationPopup()
            }
        }
    }
}