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
import si.feri.timpra.mbhubapp.dialog.MapPickerFragment

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

        val textLat = binding.textLatitude
        val textLng = binding.textLongitude
        app.simulationPosition.observe(viewLifecycleOwner) {
            textLat.text = Html.fromHtml(
                getString(R.string.sim_latitude, it.latitude),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            textLng.text = Html.fromHtml(
                getString(R.string.sim_longitude, it.longitude),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }

        //
        //
        //

        binding.button.setOnClickListener {
            MapPickerFragment.startDialog(
                requireActivity(),
                viewLifecycleOwner,
                app.simulationPosition.value!!
            ) {
                app.updateSimulationPosition(it)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _app = null
    }
}