package com.hsfl.leo_nelly.capturethecampus

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.hsfl.leo_nelly.capturethecampus.databinding.FragmentGameBinding
import androidx.fragment.app.activityViewModels
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar

class GameFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = mainViewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.startGame()
        Log.d("GameFragment", "start Game")

        setupMapView()
        setupButtons()
        observeViewModel()
        mainViewModel.resetTotalDistance()
        mainViewModel.updateVisitedFlagsCount()

        //Animation für den Pointer der Uhr
        val pointerAnimation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
        binding.ivpointer.startAnimation(pointerAnimation)
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

    }

    private fun observeViewModel() {
        mainViewModel.isGameWon.observe(viewLifecycleOwner) { won ->
            if (won) {
                mainViewModel.stopGame()
                AudioManager.playSound(requireContext(), R.raw.victory_sound)

                mainViewModel.playerName.value?.let { playerName ->
                    if (playerName.isNotBlank()) {
                        mainViewModel.addPlayerName(playerName)
                    }
                }
                findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
            }
        }

        mainViewModel.mapX.observe(viewLifecycleOwner) { x ->
            binding.imageViewPosition.layoutParams =
                (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams)
                    .apply {
                        horizontalBias = x
                    }
        }

        mainViewModel.mapY.observe(viewLifecycleOwner) { y ->
            binding.imageViewPosition.layoutParams =
                (binding.imageViewPosition.layoutParams as ConstraintLayout.LayoutParams)
                    .apply {
                        verticalBias = y
                    }
        }

        mainViewModel.flagCapturedEvent.observe(viewLifecycleOwner) { isCaptured ->
            if (isCaptured) {
                AudioManager.playSound(requireContext(), R.raw.flag_captured_sound)
                mainViewModel.handleEvent(EventType.FlagCaptured)
            }
        }

        mainViewModel.visitedFlagsCount.observe(viewLifecycleOwner) { visitedFlagsText ->
            binding.visitedFlagsTextView.text = visitedFlagsText
        }

        mainViewModel.showMessage.observe(viewLifecycleOwner) { shouldShowMessage ->
            if (shouldShowMessage) {
                Snackbar.make(
                    requireView(),
                    "You must first visit all the previous points!",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("OK") {}
                    .setActionTextColor(Color.RED)
                    .show()
                mainViewModel.showMessage.value = false
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
        mainViewModel.stopGame()
        Log.d("GameFragment", "stop Game")
        (activity as MainActivity).stopLocation()
        Log.d("GameFragment", "stopping Location")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
