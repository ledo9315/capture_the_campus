package com.hsfl.leo_nelly.capturethecampus

import DialogManager
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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

        binding.createButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_createFragment)
        }

        binding.retryButton.setOnClickListener {
            if (mainViewModel.mapPoints.value?.isNotEmpty() == true) {
                (activity as MainActivity).startLocation()

                dialogManager.showRetryDialog(
                    mainViewModel,
                    onPositiveButtonClicked = { playerName ->

                        if (mainViewModel.isPlayerNearAnyFlag()) {
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
