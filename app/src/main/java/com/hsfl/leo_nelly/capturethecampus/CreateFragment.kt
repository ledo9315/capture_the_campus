package com.hsfl.leo_nelly.capturethecampus

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hsfl.leo_nelly.capturethecampus.databinding.FragmentCreateBinding

class CreateFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private var mapPointsAdapter = MapPointAdapter(mutableListOf()) { position ->
        mainViewModel.removePoint(position)
        showUndoSnackbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mainViewModel
        mainViewModel.clearMapPoints()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        setupMapView()
        observeViewModel()

        binding.recyclerViewCreate.adapter = mapPointsAdapter
        binding.mapImageCreate.showTapHint = true
    }

    private fun setupButtons() {
        binding.setFlagButtonCreate.setOnClickListener {
            mainViewModel.addMapPoint()
        }
        binding.startGameButton.setOnClickListener {
            if (mainViewModel.isPlayerNearAnyFlag()) {
                Toast.makeText(context, "You are too close to a flag to start the game", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val playerName = mainViewModel.playerName.value
            val mapPoints = mainViewModel.mapPoints.value.orEmpty()

            if (!playerName.isNullOrEmpty() && mapPoints.isNotEmpty()) {
                findNavController().navigate(R.id.action_createFragment_to_gameFragment)
            } else {
                if (playerName.isNullOrEmpty()) {
                    Toast.makeText(context, "Please enter your name first", Toast.LENGTH_SHORT).show()
                } else if (mapPoints.isEmpty()) {
                    Toast.makeText(context, "Please set at least one flag", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.cancelGameButton.setOnClickListener {
            findNavController().navigate(R.id.action_createFragment_to_startFragment)
        }
    }

    private fun setupMapView() {
        val mapView = binding.mapImageCreate
        mapView.viewModel = mainViewModel

        mapView.onMapLongClickListener = { lat, lng ->
            mainViewModel.addMapPoint(lat, lng)
        }
    }

    private fun observeViewModel() {
        mainViewModel.mapX.observe(viewLifecycleOwner) { x ->
            binding.imageViewPosition.layoutParams = (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalBias = x
            }
        }

        mainViewModel.mapY.observe(viewLifecycleOwner) { y ->
            binding.imageViewPosition.layoutParams = (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams).apply {
                verticalBias = y
            }
        }

        mainViewModel.mapPoints.observe(viewLifecycleOwner) { updatedPoints ->
            binding.mapImageCreate.setMapPoints(updatedPoints)
            mapPointsAdapter.updatePoints(updatedPoints)
        }

        mainViewModel.flagSetEvent.observe(viewLifecycleOwner) { isSet ->
            if (isSet) {
                AudioHelper.playSound(requireContext(), R.raw.set_flag_sound)
                mainViewModel.handleEvent(EventType.FlagSet)
            }
        }

        mainViewModel.showToast.observe(viewLifecycleOwner) { show ->
            if (show) {
                Toast.makeText(context, "A flag is already placed at this location", Toast.LENGTH_SHORT).show()
                mainViewModel.showToast.postValue(false)
            }
        }

    }

    private fun showUndoSnackbar() {
        Snackbar.make(binding.root, "Flag deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") { mainViewModel.restorePoint() }
            .setActionTextColor(Color.RED)
            .show()
    }

    override fun onStart() {
        super.onStart()
        Log.d("CreateFragment", "starting Location")
        (activity as MainActivity).startLocation()
    }

    override fun onStop() {
        super.onStop()
        Log.d("CreateFragment", "stopping Location")
        (activity as MainActivity).stopLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
