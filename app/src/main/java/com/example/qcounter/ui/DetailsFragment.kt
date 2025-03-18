package com.example.qcounter.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.example.qcounter.GenericViewModelFactory
import com.example.qcounter.R
import com.example.qcounter.databinding.DetailsFragmentBinding
import com.example.qcounter.dataclass.FileURL
import com.example.qcounter.viewmodel.CounterDetailsViewModel
import com.example.qcounter.viewmodel.DeviceConfigurationViewModel
import com.example.qcounter.viewmodel.ImagesAndVideosViewModel
import com.example.qcounter.viewmodel.TicketDetailsViewModel
import com.example.slaughterhousescreen.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.NetworkInterface

class DetailsFragment : Fragment() {

    private lateinit var binding : DetailsFragmentBinding
    private lateinit var counterDetailsViewModel: CounterDetailsViewModel
    private lateinit var ticketDetailsViewModel : TicketDetailsViewModel
    private lateinit var deviceConfigurationViewModel: DeviceConfigurationViewModel

    private lateinit var imagesAndVideosViewModel: ImagesAndVideosViewModel

    private var enabledAds : String ?=null
    private var filesList : List<FileURL> ?=null
    private var storeTicketNumber : String ?=null
    private var isDeviceConfigurationApiCalled = false  // Flag to track if API has been called

    private lateinit var arabicTextView: TextView

    private var previousTicketNo: String? = null

    private var counterNumber : String ?= null
    private var branchCode : String ?= null
    var deviceId : String ?=null
    var baseUrl :String ?=null
    private var isFirstOpen = true // Flag to track if it's the first time the screen is opened

    private lateinit var mediaAdapter: MediaAdapter
    private val mediaList = mutableListOf<FileURL>()

    private val handler = Handler()
    private val refreshInterval = 5000L // 5 seconds

    private val runnable = object : Runnable {
        override fun run() {
            calCounterDetailsAPI()
            callTicketDetailsAPI()
            handler.postDelayed(this, refreshInterval)
        }
    }


    private val configurationHandler = Handler()
    private val refreshConfigurationInterval = 20000L

    private val configurationRunnable = object : Runnable {
        override fun run() {
           callDeviceConfigurationAPI()
            configurationHandler.postDelayed(this, refreshConfigurationInterval)
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

        deviceId = "1"
        arabicTextView = binding.arabicText



        val getCounterFactory = GenericViewModelFactory(CounterDetailsViewModel::class) {
            CounterDetailsViewModel(requireContext())
        }
        counterDetailsViewModel =
            ViewModelProvider(this, getCounterFactory).get(CounterDetailsViewModel::class.java)


        val getTicketFactory = GenericViewModelFactory(TicketDetailsViewModel::class){
            TicketDetailsViewModel(requireContext())
        }

        ticketDetailsViewModel = ViewModelProvider(this,getTicketFactory).get(TicketDetailsViewModel::class.java)


        val getDeviceConfigurationFactory = GenericViewModelFactory(DeviceConfigurationViewModel::class){
            DeviceConfigurationViewModel(requireContext())
        }

        deviceConfigurationViewModel = ViewModelProvider(this,getDeviceConfigurationFactory).get(DeviceConfigurationViewModel::class.java)


        val getFilesFactory = GenericViewModelFactory(ImagesAndVideosViewModel::class) {
            ImagesAndVideosViewModel(requireContext())
        }

        imagesAndVideosViewModel =
            ViewModelProvider(this, getFilesFactory).get(ImagesAndVideosViewModel::class.java)

        calCounterDetailsAPI()
        observerCounterDetailsAPI()

        binding.layoutTitle.setOnClickListener {
            PreferenceManager.clearUrl(requireContext())
            PreferenceManager.setURl(requireContext(), false)
            findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToHomeFragment())
        }

        handler.post(runnable)
        configurationHandler.post(configurationRunnable)

        isFirstOpen = false


    }

    private fun observerDeviceConfigurationAPI() {
        deviceConfigurationViewModel.getDeviceConfigurationResponse.observe(viewLifecycleOwner) { deviceConfiguration ->

            enabledAds = deviceConfiguration.EnableAds
            Log.v("returned ads",enabledAds?:"")

            callTicketDetailsAPI()
            observerTicketdetailsAPI()


            val headerBackgroundColor = Color.parseColor(deviceConfiguration.ButtonColor)
            binding.layoutTitle.setBackgroundColor(headerBackgroundColor)

            val fontName = deviceConfiguration.FontType ?:"Arial"
            val boldFont = "${fontName}bold"
            Log.v("fonttttt", boldFont)

//            val typeface = Typeface.create(fontName, Typeface.NORMAL)
//            val boldTypeface = Typeface.create(fontName, Typeface.BOLD)
//
//            binding.tvCounterTitle.typeface = typeface
//            binding.tvCounterTitleAr.typeface = typeface
//            binding.tvCounterNo.typeface = boldTypeface
//            binding.ticketNumber.typeface = boldTypeface
//            Log.v("fonttttt", typeface.toString())
//            Log.v("fonttttt", fontName)
//

            try {

                val fontResId = resources.getIdentifier(fontName, "font", requireContext().packageName)
                val fontResIdBold = resources.getIdentifier(boldFont, "font", requireContext().packageName)


                if (fontResId != 0) {
                    // Load the font and set it to the TextView
                    val typeface = ResourcesCompat.getFont(requireContext(), fontResId)
                    val boldTypeFace = ResourcesCompat.getFont(requireContext(),fontResIdBold)
                    binding.tvCounterTitle.typeface = typeface
                    binding.tvCounterTitleAr.typeface = typeface
                    binding.ticketNumber.typeface = boldTypeFace
                    binding.tvCounterNo.typeface = boldTypeFace


                } else {
                    // Fallback if the font is not found
                    binding.tvCounterTitle.typeface = Typeface.DEFAULT
                    binding.tvCounterTitleAr.typeface = Typeface.DEFAULT
                    binding.ticketNumber.typeface = Typeface.DEFAULT_BOLD
                    binding.tvCounterNo.typeface = Typeface.DEFAULT_BOLD

                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors gracefully, and fallback to a default typeface
                binding.tvCounterTitle.typeface = Typeface.DEFAULT
                binding.tvCounterTitleAr.typeface = Typeface.DEFAULT
                binding.ticketNumber.typeface = Typeface.DEFAULT_BOLD
                binding.tvCounterNo.typeface = Typeface.DEFAULT_BOLD
            }

            val fontColor = try {
                Color.parseColor(deviceConfiguration.FontColor ?: "#000000") // Default to black
            } catch (e: Exception) {
                Color.BLACK
            }
            binding.tvCounterTitle.setTextColor(fontColor)
            binding.tvCounterTitleAr.setTextColor(fontColor)
//            binding.ticketNo.setTextColor(fontColor)
//            binding.ticketNoAr.setTextColor(fontColor)

            val bgFlag = deviceConfiguration.ysnBGColor

            if (bgFlag == "True") {
                val backgroundColor = Color.parseColor(deviceConfiguration.BGColor)
                binding.layoutBackground.setBackgroundColor(backgroundColor)

            } else {
                Glide.with(requireContext())
                    .asDrawable() // Load the image as a Drawable
                    .load(deviceConfiguration.BGImage) // Load the BGImage from the API
                    .skipMemoryCache(true) // Skip memory caching
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk caching
                    .signature(ObjectKey(System.currentTimeMillis().toString())) // Force reload
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            // Set the loaded image as the background for the ConstraintLayout
                            binding.layoutBackground.background = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Optional: Handle cleanup if needed
                        }
                    })
            }




            Glide.with(requireContext())
                .load(deviceConfiguration.LogoImage)
                .skipMemoryCache(true) // Skip memory caching
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk caching
                .signature(ObjectKey(System.currentTimeMillis().toString())) // Force reload
                .load(deviceConfiguration.LogoImage)
                .into(binding.logo)



            binding.arabicText.text = deviceConfiguration.ScrollMessageAr
            setupMarquee(
                arabicTextView, true, 15000L)
         //   Toast.makeText(requireContext(), deviceConfiguration.ScrollMessageAr, Toast.LENGTH_SHORT).show()
            // Arabic text (right to left)


            PreferenceManager.setDeviceConfigurationApiFlag(true, requireContext())



        }
        deviceConfigurationViewModel.errorResponse.observe(viewLifecycleOwner){
            Log.v("error","error")
        }
    }

    private fun setupMarquee(textView: TextView, isArabic: Boolean, durationMarquee: Long) {
        textView.post {
            val textWidth = textView.paint.measureText(textView.text.toString())
            val viewWidth = textView.width

            Log.d("MarqueeSetup", "Text Width: $textWidth, View Width: $viewWidth")

            // Adjust the start and end positions based on the language
            val startX =
                if (isArabic) -textWidth else viewWidth.toFloat() // Arabic starts from left (-textWidth)
            val endX =
                if (isArabic) viewWidth.toFloat() else -textWidth // English starts from right (viewWidth)

            val animator = ObjectAnimator.ofFloat(textView, "translationX", startX, endX).apply {
                duration = durationMarquee // Adjust duration for slower animation
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                interpolator = LinearInterpolator()

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationRepeat(animation: Animator) {
                        // Reset translation based on the direction
                        textView.translationX = if (isArabic) -textWidth else viewWidth.toFloat()
                    }
                })
            }

            animator.start()
        }
    }


    private fun callDeviceConfigurationAPI() {
     //  val baseUrl = context?.let { PreferenceManager.getBaseUrl(it) }
        val baseUrl = PreferenceManager.getBaseUrl(requireContext())

        //    baseUrl  = "http://192.168.30.50/APIPub2509/"


        CoroutineScope(Dispatchers.IO).launch {
            deviceConfigurationViewModel.getDeviceConfiguration(branchCode ?:"",deviceId?:"" , baseUrl ?:"")
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(runnable)
        configurationHandler.post(configurationRunnable)
    }

        override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
        configurationHandler.removeCallbacks(configurationRunnable)
    }


    private fun callTicketDetailsAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            ticketDetailsViewModel.getTicketDetails(counterNumber ?:"" , branchCode ?:"")
        }
    }

    private fun observerTicketdetailsAPI() {
        ticketDetailsViewModel.getTicketDetailsResponse.observe(viewLifecycleOwner) { ticketResponse ->

            if ((ticketResponse.ticketno == "----") ){

                Log.v("enabled ads",enabledAds?:"")
                if (enabledAds != "False") {

                    // Only call the API if the previous ticket number was not "----"
                    if (previousTicketNo != "----") {

                        Log.v("ads", "true")

                        // This ensures that you don't call the API again if the ticket number was already "----" previously.
                        callGetImagesAndVideosApi()
                        observerImagesAndVideosViewModel()
                    }
                }

                if (filesList.isNullOrEmpty()) {
                    binding.layoutBackground.visibility = View.VISIBLE
                    binding.detailsLayout.visibility = View.VISIBLE

                    binding.viewPager.visibility = View.GONE
                    binding.viewPagerLayout.visibility=View.GONE

                    binding.ticketNumber.text = ticketResponse.ticketno
                    storeTicketNumber = ticketResponse.ticketno
                }
                else{
                    binding.layoutBackground.visibility = View.GONE
                    binding.detailsLayout.visibility = View.GONE

                    binding.viewPager.visibility = View.VISIBLE
                    binding.viewPagerLayout.visibility=View.VISIBLE
                }


            } else {
                // Otherwise, display the ticket number
                binding.layoutBackground.visibility = View.VISIBLE
                binding.detailsLayout.visibility = View.VISIBLE

                binding.viewPager.visibility = View.GONE
                binding.viewPagerLayout.visibility=View.GONE

                binding.ticketNumber.text = ticketResponse.ticketno
                storeTicketNumber = ticketResponse.ticketno
            }



            // Update the previous ticket number for future comparisons
            previousTicketNo = ticketResponse.ticketno

            // Uncomment this line if you want to show a toast with the ticket number
            // Toast.makeText(requireContext(), "Ticket Number: ${ticketResponse.ticketno}", Toast.LENGTH_SHORT).show()

        }

        ticketDetailsViewModel.errorResponse.observe(viewLifecycleOwner) {
            Log.v("error", "error")
        }
    }

    private fun callGetImagesAndVideosApi() {

       //val newbranchCode = "3"
        val baseUrl = PreferenceManager.getBaseUrl(requireContext())
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            imagesAndVideosViewModel.getImagesAndVideos(baseUrl?:"",branchCode?:"")
        }
    }

    private fun observerImagesAndVideosViewModel()
    {

            imagesAndVideosViewModel.urlsResponse.observe(viewLifecycleOwner) { fileList ->
                filesList = fileList

                if (!fileList.isNullOrEmpty()) {

                    setupViewPager(fileList)
                } else {
                    Log.v("observeMediaList", "Empty or null media list")
                }
            }

            imagesAndVideosViewModel.errorResponse.observe(viewLifecycleOwner) {
                Log.e("observeMediaList", "Error fetching media list: $it")
                binding.layoutBackground.visibility = View.VISIBLE
                binding.detailsLayout.visibility = View.VISIBLE

                binding.viewPager.visibility = View.GONE
                binding.viewPagerLayout.visibility=View.GONE

            }



    }

    private var imagesVideosHandler: Handler? = null
    private var imagesVideosRunnable: Runnable? = null

    private fun setupViewPager(fileList: List<FileURL>) {
        if (fileList.isEmpty()) {
            // Handle empty list case
            binding.layoutBackground.visibility = View.VISIBLE
            binding.detailsLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
            binding.viewPagerLayout.visibility = View.GONE
            binding.ticketNumber.text = storeTicketNumber
        } else {
            // Step 1: Sort files by the `OrderNo` field
            val orderedMediaList = fileList
                .filter { it.fileName != null && it.OrderNo != null }
                .sortedBy { it.OrderNo }

            Log.d("OrderedMediaList", "Ordered media list: ${orderedMediaList.joinToString { it.fileName ?: "null" }}")

            // Step 2: Set up ViewPager adapter
            val mediaAdapter = MediaAdapter(
                requireContext(),
                orderedMediaList,
                binding.viewPager
            )
            binding.viewPager.adapter = mediaAdapter

            // Step 3: Clear previous Handler and Runnable
            imagesVideosHandler?.removeCallbacksAndMessages(null)
            imagesVideosHandler = Handler(Looper.getMainLooper())

            // Step 4: Use OnPageChangeListener to start the duration count after the item is fully displayed
            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // Get the current media item
                    val currentMedia = orderedMediaList[position]
                    val duration = calculateDuration(currentMedia)

                    Log.d(
                        "PageChangeListener",
                        "Item fully displayed: $position, Duration: $duration ms, File: ${currentMedia.fileName}"
                    )

                    // Remove any pending callbacks to avoid overlapping
                    imagesVideosHandler?.removeCallbacksAndMessages(null)

                    // Schedule the next item after the current item's duration
                    imagesVideosHandler?.postDelayed({
                        // Move to the next item
                        val nextItem = (position + 1) % orderedMediaList.size
                        binding.viewPager.setCurrentItem(nextItem, true)
                    }, duration)
                }
            })

            // Step 5: Display the first item immediately
            binding.viewPager.setCurrentItem(0, true)
        }
    }
    private fun calculateDuration(file: FileURL): Long {
        return try {
            // Use the duration from the API (must be a valid positive number)
            file.Duration?.toLongOrNull()?.takeIf { it > 0 }
                ?: throw IllegalArgumentException("Invalid duration for file: ${file.fileName}")
        } catch (e: Exception) {
            Log.e("MediaAdapter", "Error calculating duration for file: ${file.fileName}.", e)
            throw e // Re-throw the exception to ensure the issue is not ignored
        }
    }

    // Utility function to determine if the file is a video
    private fun isVideo(fileName: String?): Boolean {
        val videoExtensions = listOf(".mp4", ".mov", ".avi", ".mkv")
        return videoExtensions.any { fileName?.endsWith(it, ignoreCase = true) == true }
    }



    private fun calCounterDetailsAPI() {
        // Get IPv4 address
        val ipv4Address = getIPAddress(true)
        println("IPv4 Address: $ipv4Address")

       //    Toast.makeText(requireContext(), "ip address: $ipv4Address", Toast.LENGTH_SHORT).show()

        // Get IPv6 address
        val ipv6Address = getIPAddress(false)
        println("IPv6 Address: $ipv6Address")
        //    Toast.makeText(requireContext(), "ip address: $ipv6Address", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            counterDetailsViewModel.getCounterDetails(ipv4Address?:"")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imagesVideosHandler?.removeCallbacksAndMessages(null)
        imagesVideosHandler = null
        imagesVideosRunnable = null
        handler.removeCallbacksAndMessages(null)
        configurationHandler.removeCallbacksAndMessages(null)
    }

    private fun observerCounterDetailsAPI() {
        counterDetailsViewModel.getCounterDetailsResponse.observe(viewLifecycleOwner){counterResponse->

      //      binding.counterNumber.text = counterResponse.counterno
            counterNumber = counterResponse.counterno.toString()
            branchCode = counterResponse.BranchCode.toString()


            binding.tvCounterNo.text = counterNumber
//            if (counterResponse.CounterTypeID == "1"){
//                binding.ticketNoAr.text = "شباك"
//            }
//            else {
//                binding.ticketNoAr.text = "عيادة"
//            }

            // Log the flag status and check the flow

            callTicketDetailsAPI()
            observerTicketdetailsAPI()


            if (!isDeviceConfigurationApiCalled) {
                // Call Device Configuration API if it hasn't been called yet
                callDeviceConfigurationAPI()
                observerDeviceConfigurationAPI()

                // Set the flag to true to prevent further calls
                isDeviceConfigurationApiCalled = true
            }

        }

        counterDetailsViewModel.errorResponse.observe(viewLifecycleOwner){
            Log.v("error","error")
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