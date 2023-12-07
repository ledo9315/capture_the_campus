package com.hsfl.leo_nelly.capturethecampus

import DialogManager
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
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

    private val mapPointsChallenge1 = listOf(
        MapPoint(54.7748, 9.4478, PointState.NOT_VISITED),
        MapPoint(54.777500, 9.444000, PointState.NOT_VISITED)
    )

    private val mapPointsChallenge2 = listOf(
        MapPoint(54.770000, 9.460000, PointState.NOT_VISITED),
        MapPoint(54.769500, 9.461000, PointState.NOT_VISITED)
    )

    private val predefinedChallenges = listOf(
        Challenge("Challenge 1", "Challenge 1 Description", mapPointsChallenge1),
        Challenge("Challenge 2", "Challenge 2 Description", mapPointsChallenge2)
    )



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

        binding.selectChallengeButton?.setOnClickListener {
            showChallengeSelectionDialog()
        }

    }

    private fun setupButtons() {

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


    private fun showChallengeSelectionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_challenge, null)
        val listView = dialogView.findViewById<ListView>(R.id.challengeListView)

        val challengeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, predefinedChallenges.map { it.name })
        listView.adapter = challengeAdapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Challenge auswählen")
            .setView(dialogView)
            .setNegativeButton("Abbrechen", null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedChallenge = predefinedChallenges[position]
            mainViewModel.selectedChallenge.value = selectedChallenge
            dialog.dismiss()
            findNavController().navigate(R.id.action_startFragment_to_createFragment)
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
