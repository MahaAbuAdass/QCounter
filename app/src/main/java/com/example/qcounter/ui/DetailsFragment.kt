package com.example.qcounter.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.qcounter.GenericViewModelFactory
import com.example.qcounter.databinding.DetailsFragmentBinding
import com.example.qcounter.viewmodel.CounterDetailsViewModel
import com.example.qcounter.viewmodel.TicketDetailsViewModel
import com.example.slaughterhousescreen.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean

class DetailsFragment : Fragment() {

    private lateinit var binding : DetailsFragmentBinding
    private lateinit var counterDetailsViewModel: CounterDetailsViewModel
    private lateinit var ticketDetailsViewModel : TicketDetailsViewModel
    private var counterNumber : String ?= null
    private var branchCode : String ?= null


    private val handler = Handler()
    private val refreshInterval = 10000L // 10 seconds

    private val runnable = object : Runnable {
        override fun run() {
            calCounterDetailsAPI()
            callTicketDetailsAPI()
            handler.postDelayed(this, refreshInterval)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailsFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getCounterFactory = GenericViewModelFactory(CounterDetailsViewModel::class) {
            CounterDetailsViewModel(requireContext())
        }
        counterDetailsViewModel =
            ViewModelProvider(this, getCounterFactory).get(CounterDetailsViewModel::class.java)


        val getTicketFactory = GenericViewModelFactory(TicketDetailsViewModel::class){
            TicketDetailsViewModel(requireContext())
        }

        ticketDetailsViewModel = ViewModelProvider(this,getTicketFactory).get(TicketDetailsViewModel::class.java)


        calCounterDetailsAPI()
        observerCounterDetailsAPI()


        binding.layoutTitle.setOnClickListener {
            PreferenceManager.clearUrl(requireContext())
            PreferenceManager.setURl(requireContext(), false)
            findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToHomeFragment())
        }


        handler.post(runnable)

    }

    override fun onStart() {
        super.onStart()
        handler.post(runnable)
    }

        override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }



    private fun observerTicketdetailsAPI() {
        ticketDetailsViewModel.getTicketDetailsResponse.observe(viewLifecycleOwner){ ticketResponse->

            binding.tvDoctor.text = ticketResponse.ResourceDisplay
            binding.ticketNumber.text = ticketResponse.ticketno
          //  Toast.makeText(requireContext(), "Ticket Number: ${ticketResponse.ticketno}", Toast.LENGTH_SHORT).show()

        }

        ticketDetailsViewModel.errorResponse.observe(viewLifecycleOwner){
            Log.v("error","error")
        }

    }

    private fun callTicketDetailsAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            ticketDetailsViewModel.getTicketDetails(counterNumber ?:"" , branchCode ?:"")
        }


    }

    private fun observerCounterDetailsAPI() {
        counterDetailsViewModel.getCounterDetailsResponse.observe(viewLifecycleOwner){counterResponse->

            binding.counterNumber.text = counterResponse.counterno
            counterNumber = counterResponse.counterno.toString()
            branchCode = counterResponse.BranchCode.toString()

            if (counterResponse.CounterTypeID == "1"){
                binding.tvTitle.text = "شباك"
            }
            else {
                binding.tvTitle.text = "عيادة"
            }

            callTicketDetailsAPI()
            observerTicketdetailsAPI()

        }

        counterDetailsViewModel.errorResponse.observe(viewLifecycleOwner){
            Log.v("error","error")
        }

    }

    private fun calCounterDetailsAPI() {
        // Get IPv4 address
        val ipv4Address = getIPAddress(true)
        println("IPv4 Address: $ipv4Address")
     //   Toast.makeText(requireContext(), "ip address: $ipv4Address", Toast.LENGTH_SHORT).show()


            // Get IPv6 address
        val ipv6Address = getIPAddress(false)
        println("IPv6 Address: $ipv6Address")
    //    Toast.makeText(requireContext(), "ip address: $ipv6Address", Toast.LENGTH_SHORT).show()



        CoroutineScope(Dispatchers.IO).launch {
            counterDetailsViewModel.getCounterDetails(ipv4Address?:"")
        }

    }

    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress
                        val isIPv4 = hostAddress.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return hostAddress
                        } else {
                            if (!isIPv4) {
                                val index = hostAddress.indexOf('%') // drop IPv6 zone suffix
                                return if (index < 0) hostAddress else hostAddress.substring(0, index)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}