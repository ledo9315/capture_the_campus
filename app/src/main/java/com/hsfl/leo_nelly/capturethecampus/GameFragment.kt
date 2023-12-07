package com.hsfl.leo_nelly.capturethecampus

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.hsfl.leo_nelly.capturethecampus.databinding.FragmentGameBinding
import androidx.fragment.app.activityViewModels

class GameFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.startGame()
        _binding = FragmentGameBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = mainViewModel
        }

        setupMapView()
        setupButtons()
        observeViewModel()
        mainViewModel.resetTotalDistance()
        mainViewModel.updateVisitedFlagsCount()
        binding.mapImageGame.showTapHint = false

        return binding.root
    }

    private fun setupMapView() {
        val mapView = binding.mapImageGame
        mapView.viewModel = mainViewModel

        mainViewModel.mapPoints.observe(viewLifecycleOwner) { updatedPoints ->
            mapView.setMapPoints(updatedPoints)
        }
    }

    private fun setupButtons() {
        binding.leaveButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameFragment_to_startFragment)
        }

        mainViewModel.isGameWon.observe(viewLifecycleOwner) { won ->
            if (won) {
                mainViewModel.stopGame()
                AudioHelper.playSound(requireContext() ,R.raw.victory_sound)

                mainViewModel.playerName.value?.let { playerName ->
                    if (playerName.isNotBlank()) {
                        mainViewModel.addPlayerName(playerName)
                    }
                }

                findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
            }
        }

    }

    private fun observeViewModel() {

        mainViewModel.mapX.observe(viewLifecycleOwner) { x ->
            binding.imageViewPosition.layoutParams = (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams)
                .apply {
                    horizontalBias = x
                }
        }

        mainViewModel.mapY.observe(viewLifecycleOwner) { y ->
            binding.imageViewPosition.layoutParams = (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams)
                .apply {
                    verticalBias = y
                }
        }

        mainViewModel.flagCapturedEvent.observe(viewLifecycleOwner) { isCaptured ->
            if (isCaptured) {
                AudioHelper.playSound(requireContext(), R.raw.flag_captured_sound)
                mainViewModel.handleEvent(EventType.FlagCaptured)
            }
        }

        mainViewModel.visitedFlagsCount.observe(viewLifecycleOwner) { visitedFlagsText ->
            binding.visitedFlagsTextView.text = visitedFlagsText
        }

        mainViewModel.showToast.observe(viewLifecycleOwner) { shouldShowToast ->
            if (shouldShowToast) {
                Toast.makeText(context, "You must first visit all the previous points!", Toast.LENGTH_SHORT).show()
                mainViewModel.showToast.value = false
            }
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("GameFragment", "starting Location")
        (activity as MainActivity).startLocation()
    }

    override fun onStop() {
        super.onStop()
        Log.d("GameFragment", "stopping Location")
        (activity as MainActivity).stopLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.stopGame()
        _binding = null
    }
}
