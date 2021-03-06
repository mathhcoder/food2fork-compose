package com.codingwithmitch.food2forkcompose.presentation.ui.recipe

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingwithmitch.food2forkcompose.domain.model.Recipe
import com.codingwithmitch.food2forkcompose.interactors.recipe.GetRecipe
import com.codingwithmitch.food2forkcompose.util.TAG
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Named

const val STATE_KEY_RECIPE = "recipe.state.recipe.key"

@ExperimentalCoroutinesApi
class RecipeViewModel
@ViewModelInject
constructor(
    private val getRecipe: GetRecipe,
    private @Named("auth_token") val token: String,
    @Assisted private val state: SavedStateHandle,
): ViewModel(){

    val recipe: MutableState<Recipe?> = mutableStateOf(null)

    val loading = mutableStateOf(false)

    init {

        // restore if process dies
        state.get<Int>(STATE_KEY_RECIPE)?.let{ recipeId ->
            onTriggerEvent(RecipeEvent.GetRecipeEvent(recipeId))
        }
    }

    fun onTriggerEvent(event: RecipeEvent){
        try {
            when(event){
                is RecipeEvent.GetRecipeEvent -> {
                    if(recipe.value == null){
                        getRecipe(event.id)
                    }
                }
            }
        }catch (e: Exception){
            Log.e(TAG, "launchJob: Exception: ${e}, ${e.cause}")
            e.printStackTrace()
        }
        finally {
            Log.d(TAG, "launchJob: finally called.")
        }
    }

    private fun getRecipe(id: Int){
        getRecipe.execute(id, token).onEach { dataState ->
            withContext(Main){

                loading.value = dataState.loading

                dataState.data?.let { data ->
                    recipe.value = data
                    state.set(STATE_KEY_RECIPE, data.id)
                }

                dataState.error?.let { error ->
                    Log.e(TAG, "getRecipe: ${error}")
                    // TODO("handle errors")
                }
            }
        }.launchIn(viewModelScope)
    }
}




















