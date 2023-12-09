package com.hsfl.leo_nelly.capturethecampus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.hsfl.leo_nelly.capturethecampus.databinding.FragmentStartBinding


class StartFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var dialogManager: DialogManager

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mainViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogManager = DialogManager(requireContext())
        setupButtons()
    }

    private fun setupButtons() {
        val challenges = ChallengeManager.getPredefinedChallenges()

        binding.selectChallengeButton.setOnClickListener {
            dialogManager.showChallengeSelectionDialog(
                challenges,
                mainViewModel,
                findNavController()
            )
        }

        binding.createButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_createFragment)
        }

        binding.retryButton.setOnClickListener {
            if (mainViewModel.mapPoints.value?.isNotEmpty() == true) {
                (activity as MainActivity).startLocation()

                dialogManager.showRetryDialog(
                    mainViewModel,
                    onPositiveButtonClicked = { playerName ->

                        if (mainViewModel.isPlayerNearAnyPoint()) {
                            Toast.makeText(context, "You are too close to a flag to start the game", Toast.LENGTH_LONG).show()
                            (activity as MainActivity).stopLocation()
                            return@showRetryDialog
                        }

                        mainViewModel.addPlayerName(playerName)
                        mainViewModel.startGame(true)
                        (activity as MainActivity).stopLocation()
                        findNavController().navigate(R.id.action_startFragment_to_gameFragment)
                    },
                    onNegativeButtonClicked = {
                        (activity as MainActivity).stopLocation()
                    }
                )
            } else {
                Toast.makeText(context, "Set at least one flag in Create Game", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
