package si.feri.timpra.mbhubapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.feri.timpra.mbhubapp.MainActivity
import si.feri.timpra.mbhubapp.MyApplication
import si.feri.timpra.mbhubapp.databinding.FragmentHomeBinding

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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}