package com.example.recipebook.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.recipebook.adapter.RecipeAdapter
import com.example.recipebook.databinding.FragmentListBinding
import com.example.recipebook.model.Recipe
import com.example.recipebook.roomdb.RecipeDAO
import com.example.recipebook.roomdb.RecipeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val mDisposable = CompositeDisposable()
    private lateinit var db : RecipeDatabase
    private lateinit var recipeDAO : RecipeDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(requireContext(), RecipeDatabase::class.java, "Recipes").build()
        recipeDAO = db.recipeDAO()
    }
    private fun handleResponse(recipes: List<Recipe>){
        val adapter = RecipeAdapter(recipes)
        binding.recipeRecyclerView.adapter = adapter
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener {addNew(it)}
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        getData()
    }
    private fun getData(){
        mDisposable.add(
            recipeDAO.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    fun addNew(view: View){
        val action = ListFragmentDirections.actionListFragmentToRecipeFragment(information = "new", id = -1)
        Navigation.findNavController(view).navigate(action)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}