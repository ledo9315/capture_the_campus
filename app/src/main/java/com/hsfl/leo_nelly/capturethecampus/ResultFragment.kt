package com.hsfl.leo_nelly.capturethecampus

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hsfl.leo_nelly.capturethecampus.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mainViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {

        binding.saveChallengeButton.setOnClickListener {
            val success = ChallengeManager.saveChallenge(
                requireContext(),
                mainViewModel.challengeName.value ?: "",
                mainViewModel.challengeDescription.value ?: "",
                mainViewModel.mapPoints.value!!
            )

            if (success) {
                Log.d("ResultFragment", "Challenge saved successfully")

                Snackbar.make(
                    binding.root,
                    "Challenge saved successfully",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("OK") {}
                    .setActionTextColor(Color.RED)
                    .show()

                findNavController().navigate(R.id.action_resultFragment_to_startFragment)
            }
        }

        binding.doneButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startFragment)
        }
    }

    private fun observeViewModel() {
        mainViewModel.elapsedTime.observe(viewLifecycleOwner) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.resetMapPointsStatus()
        mainViewModel.resetChallengeInput()
        mainViewModel.isGameWon.value = false
        _binding = null
    }
}
