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
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.data.CaptureSettings
import si.feri.timpra.mbhubapp.data.SoundSettings
import si.feri.timpra.mbhubapp.databinding.FragmentSoundSettingsBinding

private const val ARG_SETTINGS = "ARG_SETTINGS"
private const val REQUEST_KEY = "REQUEST_KEY"
private const val TAG = "SoundSettingsFragment"


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class SoundSettingsFragment : DialogFragment() {
    private var _binding: FragmentSoundSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var value: SoundSettings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.value = it.getSerializable(ARG_SETTINGS) as SoundSettings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoundSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        //
        //

        binding.inputInterval.setText(value.formatInterval())
        binding.typeGroup.check(
            when (value.file) {
                R.raw.quiet -> R.id.optionQuiet
                R.raw.noisy -> R.id.optionNoisy
                else -> R.raw.active
            }
        )

        binding.swEnabled.isChecked = value.enabled

        //
        //
        //

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

            val file = when (binding.typeGroup.checkedRadioButtonId) {
                R.id.optionQuiet -> R.raw.quiet
                R.id.optionNoisy -> R.raw.noisy
                else -> return@setOnClickListener
            }

            setFragmentResult(
                REQUEST_KEY, resultBundle(
                    SoundSettings(
                        enabled = enabled,
                        interval = interval,
                        file = file,
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
        fun newInstance(settings: SoundSettings) = SoundSettingsFragment().apply {
            arguments = newBundle(settings)
        }

        private fun newBundle(settings: SoundSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun resultBundle(settings: SoundSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun startDialog(
            activity: FragmentActivity,
            viewLifecycleOwner: LifecycleOwner,
            settings: SoundSettings,
            callback: (SoundSettings) -> Unit
        ) {
            val frag = newInstance(settings)
            val supportFragmentManager = activity.supportFragmentManager
            // we have to implement setFragmentResultListener
            supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY, viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == REQUEST_KEY) {
                    val resSettings = bundle.getSerializable(ARG_SETTINGS) as SoundSettings
                    callback(resSettings)
                }
            }
            // show
            frag.show(supportFragmentManager, TAG)
        }
    }
}