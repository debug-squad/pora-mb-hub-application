package si.feri.timpra.mbhubapp.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import si.feri.timpra.mbhubapp.data.CaptureSettings
import si.feri.timpra.mbhubapp.databinding.FragmentCaptureSettingsBinding

private const val ARG_SETTINGS = "ARG_SETTINGS"
private const val REQUEST_KEY = "REQUEST_KEY"
private const val TAG = "CaptureSettingsFragment"


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class CaptureSettingsFragment : DialogFragment() {
    private var _binding: FragmentCaptureSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var value: CaptureSettings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.value = it.getSerializable(ARG_SETTINGS) as CaptureSettings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        //
        //

        binding.inputDuration.setText(value.formatDuration())
        binding.inputInterval.setText(value.formatInterval())
        binding.swEnabled.isChecked = value.enabled

        //
        //
        //

        binding.inputDuration.setOnClickListener {
            binding.inputDuration.error = null
        }
        binding.inputInterval.setOnClickListener {
            binding.inputInterval.error = null
        }

        binding.btnApply.setOnClickListener {
            val enabled = binding.swEnabled.isChecked

            val interval = CaptureSettings.parse(binding.inputInterval.text.toString())
            if (interval == null) {
                binding.inputInterval.error = "Invalid format"
                return@setOnClickListener
            }

            val duration = CaptureSettings.parse(binding.inputDuration.text.toString())
            if (duration == null) {
                binding.inputDuration.error = "Invalid format"
                return@setOnClickListener
            }

            setFragmentResult(
                REQUEST_KEY, resultBundle(
                    CaptureSettings(
                        enabled = enabled,
                        duration = duration,
                        interval = interval,
                    )
                )
            )
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param settings Position or null.
         * @return A new instance of fragment InputFragment.
         */
        @JvmStatic
        fun newInstance(settings: CaptureSettings) = CaptureSettingsFragment().apply {
            arguments = newBundle(settings)
        }

        private fun newBundle(settings: CaptureSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun resultBundle(settings: CaptureSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun startDialog(
            activity: FragmentActivity,
            viewLifecycleOwner: LifecycleOwner,
            settings: CaptureSettings,
            callback: (CaptureSettings) -> Unit
        ) {
            val frag = newInstance(settings)
            val supportFragmentManager = activity.supportFragmentManager
            // we have to implement setFragmentResultListener
            supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY, viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == REQUEST_KEY) {
                    val resSettings = bundle.getSerializable(ARG_SETTINGS) as CaptureSettings
                    callback(resSettings)
                }
            }
            // show
            frag.show(supportFragmentManager, TAG)
        }
    }
}