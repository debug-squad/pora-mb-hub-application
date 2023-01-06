package si.feri.timpra.mbhubapp.ui.simulate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.feri.timpra.mbhubapp.databinding.FragmentSimulateBinding

class SimulateFragment : Fragment() {

    private var _binding: FragmentSimulateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val simulateViewModel = ViewModelProvider(this)[SimulateViewModel::class.java]

        _binding = FragmentSimulateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSimulate
        simulateViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}