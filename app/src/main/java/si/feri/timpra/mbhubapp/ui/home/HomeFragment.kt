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

        //
        //
        //

        app.settingsAcc.observe(viewLifecycleOwner) {
            binding.accInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.accDuration.text = Html.fromHtml(
                getString(R.string.home_acc_duration, it.formatDuration()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.enableAcc.isChecked = it.enabled
        }
        binding.enableAcc.setOnCheckedChangeListener { _, isChecked ->
            app.updateSettingsAccelerometer(app.settingsAcc.value!!.setEnabled(isChecked))
        }
        binding.cardAcc.setOnLongClickListener {
            CaptureSettingsFragment.startDialog(
                requireActivity(), viewLifecycleOwner, app.settingsAcc.value!!
            ) {
                app.updateSettingsAccelerometer(it)
            }
            false
        }

        //
        //
        //

        app.settingsSound.observe(viewLifecycleOwner) {
            binding.soundInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.soundDuration.text = Html.fromHtml(
                getString(R.string.home_acc_duration, it.formatDuration()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.enableSound.isChecked = it.enabled
        }
        binding.enableSound.setOnCheckedChangeListener { _, isChecked ->
            app.updateSettingsSound(app.settingsSound.value!!.setEnabled(isChecked))
        }
        binding.cardSound.setOnLongClickListener {
            CaptureSettingsFragment.startDialog(
                requireActivity(), viewLifecycleOwner, app.settingsSound.value!!
            ) {
                app.updateSettingsSound(it)
            }
            false
        }

        //
        //
        //

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}