package com.hsfl.leo_nelly.capturethecampus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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

        mainViewModel.elapsedTime.observe(viewLifecycleOwner) {}

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    private fun setupButtons() {
        binding.doneButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.resetMapPointsStatus()
        mainViewModel.isGameWon.value = false
        _binding = null
    }
}
