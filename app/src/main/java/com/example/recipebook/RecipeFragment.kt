package com.example.recipebook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.recipebook.databinding.FragmentListBinding
import com.example.recipebook.databinding.FragmentRecipeBinding

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{selectImage(it)}
        binding.saveButton.setOnClickListener{save(it)}
        binding.deleteButton.setOnClickListener{delete(it)}

        arguments?.let {
            val information = RecipeFragmentArgs.fromBundle(it).information
            if(information == "new"){
                binding.deleteButton.isEnabled = false
                binding.saveButton.isEnabled = true
                binding.nameText.setText("")
                binding.ingredientText.setText("")
            }
            else{
                binding.deleteButton.isEnabled = true
                binding.saveButton.isEnabled = false
            }
        }

    }

    fun save(view: View){

    }
    fun delete(view: View){

    }
    fun selectImage(view: View){

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}