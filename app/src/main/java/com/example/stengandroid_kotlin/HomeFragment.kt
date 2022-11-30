package com.example.stengandroid_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var locationClient: LocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val timeStampData: TextView? = view?.findViewById(R.id.textView_timeStamp)
        val latData: TextView? = view?.findViewById(R.id.textView_Latitude)
        val longData: TextView? = view?.findViewById(R.id.textView_Longitude)
        val altData: TextView? = view?.findViewById(R.id.textView_Altitude)
        val snrData: TextView? = view?.findViewById(R.id.textView_SNR)
        val cellID: TextView? = view?.findViewById(R.id.textView_CELLID)
        val ueID: TextView? = view?.findViewById(R.id.textView_UEID)
        val accuracyData: TextView? = view?.findViewById(R.id.textView_Accuracy)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton: Button? = view?.findViewById(R.id.Button_Start)
        val stopButton: Button? = view?.findViewById(R.id.Button_Stop)

        if (startButton!= null) {
            startButton?.setOnClickListener {
                Intent(requireContext().applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    requireActivity().startService(this)
                }
                Toast.makeText(requireContext(), "start button pressed", Toast.LENGTH_SHORT).show()
            }
        }

        if (stopButton!= null) {
            stopButton?.setOnClickListener {
                Intent(requireActivity().applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    requireActivity().startService(this)
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}