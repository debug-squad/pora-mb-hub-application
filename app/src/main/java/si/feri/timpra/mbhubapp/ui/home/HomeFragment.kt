package si.feri.timpra.mbhubapp.ui.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.feri.timpra.mbhubapp.MainActivity
import si.feri.timpra.mbhubapp.MyApplication
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.databinding.FragmentHomeBinding
import si.feri.timpra.mbhubapp.dialog.CaptureSettingsFragment

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _app: MyApplication? = null
    private val app get() = _app!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _app = requireActivity().application as MyApplication
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //
        //
        //

        val activity = requireActivity() as MainActivity
        binding.btnTakePicture.setOnClickListener {
            activity.takePhoto()
        }


        val accInterval = binding.accInterval
        val accDuration = binding.accDuration
        val accEnabled = binding.accEnable
        app.settingsAccel.observe(viewLifecycleOwner) {
            accInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            accDuration.text = Html.fromHtml(
                getString(R.string.home_acc_duration, it.formatDuration()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            accEnabled.isChecked = it.enabled
        }
        accEnabled.setOnCheckedChangeListener { _, isChecked ->
            app.updateSettingsAccelerometer(app.settingsAccel.value!!.setEnabled(isChecked))
        }

        val soundInterval = binding.soundInterval
        val soundDuration = binding.soundDuration
        val soundEnabled = binding.accEnable
        app.settingsSound.observe(viewLifecycleOwner) {
            soundInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            soundDuration.text = Html.fromHtml(
                getString(R.string.home_acc_duration, it.formatDuration()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            soundEnabled.isChecked = it.enabled
        }
        soundEnabled.setOnCheckedChangeListener { _, isChecked ->
            app.updateSettingsSound(app.settingsSound.value!!.setEnabled(isChecked))
        }


        binding.cardSound.setOnLongClickListener {
            CaptureSettingsFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.settingsSound.value!!
            ) {
                app.updateSettingsSound(it)
            }
            false
        }
        binding.cardAccel.setOnLongClickListener {
            CaptureSettingsFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.settingsSound.value!!
            ) {
                app.updateSettingsAccelerometer(it)
            }
            false
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}