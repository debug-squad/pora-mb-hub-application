package si.feri.timpra.mbhubapp.ui.simulate

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.feri.timpra.mbhubapp.MyApplication
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.databinding.FragmentSimulateBinding
import si.feri.timpra.mbhubapp.dialog.AccSettingsFragment
import si.feri.timpra.mbhubapp.dialog.ImgSettingsFragment
import si.feri.timpra.mbhubapp.dialog.MapPickerFragment
import si.feri.timpra.mbhubapp.dialog.SoundSettingsFragment

class SimulateFragment : Fragment() {
    private var _binding: FragmentSimulateBinding? = null
    private val binding get() = _binding!!

    private var _app: MyApplication? = null
    private val app get() = _app!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val simulateViewModel = ViewModelProvider(this)[SimulateViewModel::class.java]
        _binding = FragmentSimulateBinding.inflate(inflater, container, false)
        _app = requireActivity().application as MyApplication
        val root: View = binding.root

        //
        //
        //

        binding.btnSetLocation.setOnClickListener {
            MapPickerFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.simulationPosition.value!!
            ) {
                app.updateSimulationPosition(it)
            }
        }
        app.simulationPosition.observe(viewLifecycleOwner) {
            binding.textLatitude.text = Html.fromHtml(
                getString(R.string.sim_latitude, it.latitude),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.textLongitude.text = Html.fromHtml(
                getString(R.string.sim_longitude, it.longitude),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }

        //
        //
        //

        app.simImgSettings.observe(viewLifecycleOwner) {
            binding.imgInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.txtPathImg.text = Html.fromHtml(
                getString(R.string.sim_file_path, it.text()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.enableImg.isChecked = it.enabled
        }
        binding.enableImg.setOnCheckedChangeListener { _, isChecked ->
            app.updateSimImgSettings(app.simImgSettings.value!!.setEnabled(isChecked))
        }
        binding.cardImg.setOnLongClickListener {
            ImgSettingsFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.simImgSettings.value!!,
            ) {
                app.updateSimImgSettings(it)
            }
            false
        }

        //
        //
        //

        app.simSoundSettings.observe(viewLifecycleOwner) {
            binding.soundInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.txtPathSound.text = Html.fromHtml(
                getString(R.string.sim_file_path, it.text()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.enableSound.isChecked = it.enabled
        }
        binding.enableSound.setOnCheckedChangeListener { _, isChecked ->
            app.updateSimSoundSettings(app.simSoundSettings.value!!.setEnabled(isChecked))
        }
        binding.cardSound.setOnLongClickListener {
            SoundSettingsFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.simSoundSettings.value!!,
            ) {
                app.updateSimSoundSettings(it)
            }
            false
        }

        //
        //
        //

        app.simAccSettings.observe(viewLifecycleOwner) {
            binding.accInterval.text = Html.fromHtml(
                getString(R.string.home_acc_interval, it.formatInterval()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.txtPathAcc.text = Html.fromHtml(
                getString(R.string.sim_file_path, it.text()),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            binding.enableAcc.isChecked = it.enabled
        }
        binding.enableAcc.setOnCheckedChangeListener { _, isChecked ->
            app.updateSimAccSettings(app.simAccSettings.value!!.setEnabled(isChecked))
        }
        binding.cardAcc.setOnLongClickListener {
            AccSettingsFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.simAccSettings.value!!
            ) {
                app.updateSimAccSettings(it)
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
        _app = null
    }
}