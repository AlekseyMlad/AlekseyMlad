package ru.netology.nework.ui.fragment

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapPickerBinding
import ru.netology.nework.dto.FabAction
import ru.netology.nework.viewmodel.SharedViewModel


class MapPickerFragment : Fragment(), UserLocationObjectListener, InputListener {

    private val sharedViewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var userLocationLayer: UserLocationLayer
    private var selectedPoint: Point? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.menu_map_picker, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        selectedPoint?.let {
                            setFragmentResult(
                                "map_picker_result",
                                bundleOf("lat" to it.latitude, "long" to it.longitude)
                            )
                        }
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)

        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.map.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.setHeadingModeActive(false)
        userLocationLayer.setObjectListener(this)

        binding.map.mapWindow.map.addInputListener(this)

        return binding.root
    }





    override fun onStart() {
        super.onStart()
        binding.map.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.map.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setFabAction(FabAction.NONE)
    }

    override fun onPause() {
        super.onPause()
        sharedViewModel.setFabAction(FabAction.NONE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF(0.5f, 0.5f),
            PointF(0.5f, 0.5f)
        )

        if (selectedPoint == null) {
            val userLocation = userLocationLayer.cameraPosition()?.target
            if (userLocation != null) {
                binding.map.mapWindow.map.move(
                    CameraPosition(userLocation, 15.0f, 0.0f, 0.0f)
                )
            }
        }
    }

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}


    override fun onMapTap(map: Map, point: Point) {
        selectedPoint = point
        map.mapObjects.clear()
                map.mapObjects.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on))
        }
    }

    override fun onMapLongTap(map: Map, point: Point) {}
}
