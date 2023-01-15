package si.feri.timpra.mbhubapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    lateinit var binding: FragmentAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?){
        binding= FragmentAboutBinding.bind(view)



    }


}