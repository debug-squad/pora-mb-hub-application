package si.feri.timpra.mbhubapp.ui.event

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import si.feri.timpra.mbhubapp.MainActivity
import si.feri.timpra.mbhubapp.MyApplication
import si.feri.timpra.mbhubapp.R
import si.feri.timpra.mbhubapp.databinding.FragmentEventBinding

class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private var _app: MyApplication? = null
    private val app get() = _app!!

    private var _map: MapView? = null
    private val map get() = _map!!

    private var _mapController: IMapController? = null
    private val mapController get() = _mapController!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val simulateViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        _app = requireActivity().application as MyApplication
        val root: View = binding.root

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

    var selectedMarker: Marker? = null

    @SuppressLint("MissingPermission")
    private fun initMap() {
        app.selectedEvent.observe(viewLifecycleOwner) {
            binding.eventInfo.text = Html.fromHtml(
                getString(R.string.event_event_info, it?.name ?: "null"),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }


        for (event in (requireActivity() as MainActivity).listOfEvents) {
            val markerPrevious = Marker(map)
            markerPrevious.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            markerPrevious.title =  getString(R.string.event_title, event.name)
            if (app.selectedEvent.value?.id == event.id) {
                markerPrevious.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_marker_blue
                )
                selectedMarker = markerPrevious
            } else {
                markerPrevious.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_marker
                )
            }

            markerPrevious.position.longitude = event.longitude.toDouble()
            markerPrevious.position.latitude = event.latitude.toDouble()
            map.overlays.add(markerPrevious)
            markerPrevious.setOnMarkerClickListener { marker, mapView ->
                selectedMarker?.let {
                    it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)
                }
                markerPrevious.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_blue)
                selectedMarker = markerPrevious
                app.updateEvent(event)
                map.invalidate()
                markerPrevious.showInfoWindow()
                true
            }
        }

        selectedMarker?.let {
            mapController.setZoom(15.0)
            mapController.setCenter(it.position)
        }

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
            if (selectedMarker == null) {
                mapController.setZoom(15.0)
                mapController.setCenter(getPositionMarker().position)
            }
        }

        //
        //
        //

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }
}
