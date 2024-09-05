package com.example.recipebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.databinding.RecyclerRowBinding
import com.example.recipebook.model.Recipe
import com.example.recipebook.view.ListFragmentDirections

class RecipeAdapter (var recipeList: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeHolder(recyclerRowBinding)
    }
    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = recipeList[position].name
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToRecipeFragment(information = "old", id = recipeList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
    override fun getItemCount(): Int {
        return recipeList.size
    }
    class RecipeHolder(val recyclerRowBinding: RecyclerRowBinding): RecyclerView.ViewHolder(recyclerRowBinding.root){

    }
}