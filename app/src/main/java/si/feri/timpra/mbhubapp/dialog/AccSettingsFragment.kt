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
import si.feri.timpra.mbhubapp.data.AccSettings
import si.feri.timpra.mbhubapp.data.CaptureSettings
import si.feri.timpra.mbhubapp.databinding.FragmentAccSettingsBinding

private const val ARG_SETTINGS = "ARG_SETTINGS"
private const val REQUEST_KEY = "REQUEST_KEY"
private const val TAG = "AccSettingsFragment"


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class AccSettingsFragment : DialogFragment() {
    private var _binding: FragmentAccSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var value: AccSettings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.value = it.getSerializable(ARG_SETTINGS) as AccSettings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccSettingsBinding.inflate(inflater, container, false)
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
                R.raw.active -> R.id.optionActive
                R.raw.calm -> R.id.optionCalm
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
                R.id.optionActive -> R.raw.active
                R.id.optionCalm -> R.raw.calm
                else -> return@setOnClickListener
            }

            setFragmentResult(
                REQUEST_KEY, resultBundle(
                    AccSettings(
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
        fun newInstance(settings: AccSettings) = AccSettingsFragment().apply {
            arguments = newBundle(settings)
        }

        private fun newBundle(settings: AccSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun resultBundle(settings: AccSettings) = Bundle().apply {
            putSerializable(ARG_SETTINGS, settings)
        }

        fun startDialog(
            activity: FragmentActivity,
            viewLifecycleOwner: LifecycleOwner,
            settings: AccSettings,
            callback: (AccSettings) -> Unit
        ) {
            val frag = newInstance(settings)
            val supportFragmentManager = activity.supportFragmentManager
            // we have to implement setFragmentResultListener
            supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY, viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == REQUEST_KEY) {
                    val resSettings = bundle.getSerializable(ARG_SETTINGS) as AccSettings
                    callback(resSettings)
                }
            }
            // show
            frag.show(supportFragmentManager, TAG)
        }
    }
}