package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccessCode
import com.example.data.model.Recipe
import com.example.data.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY createdAt DESC")
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    fun getRecipeById(id: String): Flow<Recipe?>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR steps LIKE '%' || :query || '%'")
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe): Int

    @Delete
    suspend fun deleteRecipe(recipe: Recipe): Int

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String): Int

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int
}

@Dao
interface AccessCodeDao {
    @Query("SELECT * FROM access_codes ORDER BY createdAt DESC")
    fun getAllCodes(): Flow<List<AccessCode>>

    @Query("SELECT COUNT(*) FROM access_codes WHERE code = :code")
    suspend fun countCode(code: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCode(accessCode: AccessCode)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCodes(codes: List<AccessCode>)

    @Query("DELETE FROM access_codes WHERE code = :code")
    suspend fun deleteCode(code: String)

    @Query("SELECT COUNT(*) FROM access_codes")
    suspend fun getCodeCount(): Int
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscription(id: Long)

    @Query("DELETE FROM subscriptions")
    suspend fun clearAll()
}
