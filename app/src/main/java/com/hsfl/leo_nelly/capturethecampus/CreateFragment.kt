package com.hsfl.leo_nelly.capturethecampus

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
        showSnackbar("Flag deleted", "Undo") {
            mainViewModel.restorePoint()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mainViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // adapter für die recyclerview
        binding.recyclerViewCreate.adapter = mapPointsAdapter

        setupButtons()
        observeViewModel()
        setupMapView()
    }

    private fun setupButtons() {
        binding.setFlagButtonCreate.setOnClickListener {
            mainViewModel.addMapPoint()
        }

        binding.startGameButton.setOnClickListener {
            //wenn der spieler zu nah an einem flag ist, kann er das spiel nicht starten
            if (mainViewModel.isPlayerNearAnyPoint()) {
                showSnackbar("You are too close to a flag to start the game", "OK") {}
                return@setOnClickListener
            }

            val playerName = mainViewModel.playerName.value
            val mapPoints = mainViewModel.mapPoints.value.orEmpty()

            if (!playerName.isNullOrEmpty() && mapPoints.isNotEmpty()) {
                mainViewModel.resetHighScoreIfNeeded()
                findNavController().navigate(R.id.action_createFragment_to_gameFragment)
            } else {
                if (playerName.isNullOrEmpty()) {
                    showSnackbar("Please enter a player name", "Enter Name") {
                        binding.inputNameField.requestFocus()
                        showKeyboard(binding.inputNameField)
                    }

                } else if (mapPoints.isEmpty()) {
                    showSnackbar("Please place at least one flag", "OK") {}
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
            binding.imageViewPosition.layoutParams =
                (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams).apply {
                    horizontalBias = x
                }
        }

        mainViewModel.mapY.observe(viewLifecycleOwner) { y ->
            binding.imageViewPosition.layoutParams =
                (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams).apply {
                    verticalBias = y
                }
        }

        mainViewModel.mapPoints.observe(viewLifecycleOwner) { updatedPoints ->
            Log.d("CreateFragment", "mapPoints updated")
            binding.mapImageCreate.setMapPoints(updatedPoints)
            mapPointsAdapter.updatePoints(updatedPoints)
        }

        mainViewModel.flagSetEvent.observe(viewLifecycleOwner) { isSet ->
            if (isSet) {
                AudioManager.playSound(requireContext(), R.raw.set_flag_sound)
                mainViewModel.handleEvent(EventType.FlagSet)
            }
        }

        mainViewModel.selectedChallenge.observe(viewLifecycleOwner) { challenge ->
            challenge?.let {
                mainViewModel.setMapPoints(it.mapPoints)
            }
        }

        mainViewModel.showMessage.observe(viewLifecycleOwner) { show ->
            if (show) {
                showSnackbar("A flag is already placed at this location", "OK") {}
                mainViewModel.showMessage.postValue(false)
            }
        }

    }

    private fun showSnackbar(text: String, actionText: String, action: () -> Unit) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            .setAction(actionText) { action() }
            .setActionTextColor(Color.RED)
            .show()
    }

    private fun showKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
