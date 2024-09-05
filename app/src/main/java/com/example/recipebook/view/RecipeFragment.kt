package com.example.recipebook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.recipebook.databinding.FragmentRecipeBinding
import com.example.recipebook.model.Recipe
import com.example.recipebook.roomdb.RecipeDAO
import com.example.recipebook.roomdb.RecipeDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException

private lateinit var permissionLauncher: ActivityResultLauncher<String>
private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

private var selectedImage: Uri? = null
private var selectedBitmap: Bitmap? = null
private val mDisposable = CompositeDisposable()
private var selectedRecipe: Recipe? = null

private lateinit var db : RecipeDatabase
private lateinit var recipeDAO : RecipeDAO

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(), RecipeDatabase::class.java, "Recipes").build()
        recipeDAO = db.recipeDAO()
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
                selectedRecipe = null
                binding.deleteButton.isEnabled = false
                binding.saveButton.isEnabled = true
                binding.nameText.setText("")
                binding.ingredientText.setText("")
            }
            else{
                binding.deleteButton.isEnabled = true
                binding.saveButton.isEnabled = false
                val id = RecipeFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    recipeDAO.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }
    }

    private fun handleResponse(recipe: Recipe){
        selectedRecipe = recipe
        binding.nameText.setText(recipe.name)
        binding.ingredientText.setText(recipe.ingredient)
        val byteArray = recipe.image
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        binding.imageView.setImageBitmap(bitmap)

    }

    fun save(view: View){
        val name = binding.nameText.text.toString()
        val ingredient = binding.ingredientText.text.toString()
        if(selectedBitmap != null){
            val smallBitmap = smallBitmapCreator(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50, outputStream)
            val byteArray = outputStream.toByteArray()

            val recipe = Recipe(name,ingredient,byteArray)
            //RxJava
            mDisposable.add(
                recipeDAO.insert(recipe)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }
    private fun handleResponseForInsert(){
        val action = RecipeFragmentDirections.actionRecipeFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun delete(view: View){
        if(selectedRecipe != null){
            mDisposable.add(
                recipeDAO.delete(recipe = selectedRecipe!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }

    }
    fun selectImage(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "We need to access the gallery and select an image.", Snackbar.LENGTH_INDEFINITE).setAction(
                        "Allow",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else{
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "We need to access the gallery and select an image.", Snackbar.LENGTH_INDEFINITE).setAction(
                        "Allow",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else{
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode== AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    selectedImage = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        } else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: IOException) {
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        {result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(), "No permission given.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun smallBitmapCreator(selectedBitmap: Bitmap, maxSize: Int):Bitmap{
        var width = selectedBitmap.width
        var height = selectedBitmap.height
        var bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
            //horizontal
            width = maxSize
            val smallHeight = width / bitmapRatio
            height = smallHeight.toInt()
        }else{
            //vertical
            height = maxSize
            val smallWidth = height * bitmapRatio
            width = smallWidth.toInt()

        }
        return Bitmap.createScaledBitmap(selectedBitmap, width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}