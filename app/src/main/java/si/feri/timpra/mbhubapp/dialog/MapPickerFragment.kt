package si.feri.timpra.mbhubapp.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.databinding.FragmentMapPickerBinding

private const val ARG_POSITION = "ARG_POSITION"
private const val REQUEST_KEY = "GET_POSITION"
private const val TAG = "PositionPickerFragment"


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class MapPickerFragment : DialogFragment() {
    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private var _map: MapView? = null
    private val map get() = _map!!

    private var _mapController: IMapController? = null
    private val mapController get() = _mapController!!

    private lateinit var position: GeoPoint
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getSerializable(ARG_POSITION) as GeoPoint
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        //
        //

        _map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        _mapController = map.controller


        //
        //
        //

        initMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //
    //
    //

    @SuppressLint("MissingPermission")
    private fun initMap() {
        val markerPrevious = Marker(map)
        markerPrevious.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        markerPrevious.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)
        markerPrevious.position.longitude = position.longitude
        markerPrevious.position.latitude = position.latitude
        map.overlays.add(markerPrevious)
        mapController.setZoom(15.0)
        mapController.setCenter(markerPrevious.position)

        //
        //
        //

        var markerMe: Marker? = null
        fun getPositionMarker(): Marker {
            if (markerMe == null) {
                markerMe = Marker(map)
                markerMe!!.title = getString(R.string.map_me)
                markerMe!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                markerMe!!.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_red)
                map.overlays.add(markerMe)
            }
            return markerMe!!
        }

        fun updateLocation(loc: Location) {
            getPositionMarker().position.latitude = loc.latitude
            getPositionMarker().position.longitude = loc.longitude
            mapController.setZoom(15.0)
            mapController.setCenter(getPositionMarker().position)
        }

        //
        //
        //
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                AlertDialog.Builder(context).setTitle(getString(R.string.map_picker_dialog_title))
                    .setMessage(
                        getString(R.string.map_picker_dialog_description)
                    ).setPositiveButton(getString(R.string.map_picker_dialog_confirm)) { _, _ ->
                        setFragmentResult(
                            REQUEST_KEY, resultBundle(p!!)
                        )
                        dismiss()
                    }.setNegativeButton(getString(R.string.map_picker_dialog_neutral)) { _, _ ->
                        // do nothing
                    }.show()
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }))
        // map.invalidate()

        //
        //
        //

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param position Position or null.
         * @return A new instance of fragment InputFragment.
         */
        @JvmStatic
        fun newInstance(position: GeoPoint) = MapPickerFragment().apply {
            arguments = newBundle(position)
        }

        private fun newBundle(position: GeoPoint) = Bundle().apply {
            putSerializable(ARG_POSITION, position)
        }

        fun resultBundle(position: GeoPoint) = Bundle().apply {
            putSerializable(ARG_POSITION, position)
        }

        fun startDialog(
            activity: FragmentActivity,
            viewLifecycleOwner: LifecycleOwner,
            position: GeoPoint,
            callback: (GeoPoint) -> Unit
        ) {
            val frag = newInstance(position)
            val supportFragmentManager = activity.supportFragmentManager
            // we have to implement setFragmentResultListener
            supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY, viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == REQUEST_KEY) {
                    val resPosition = bundle.getSerializable(ARG_POSITION) as GeoPoint
                    callback(resPosition)
                }
            }
            // show
            frag.show(supportFragmentManager, TAG)
        }
    }
}