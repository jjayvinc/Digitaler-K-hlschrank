package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class MealDbResponse(
    @Json(name = "meals")
    val meals: List<MealDbDto>?
)

@JsonClass(generateAdapter = true)
data class MealCategoryItem(
    @Json(name = "idCategory")
    val idCategory: String?,
    @Json(name = "strCategory")
    val strCategory: String?,
    @Json(name = "strCategoryThumb")
    val strCategoryThumb: String?,
    @Json(name = "strCategoryDescription")
    val strCategoryDescription: String?
)

@JsonClass(generateAdapter = true)
data class MealCategoriesResponse(
    @Json(name = "categories")
    val categories: List<MealCategoryItem>?
)

@JsonClass(generateAdapter = true)
data class MealDbDto(
    @Json(name = "idMeal") val idMeal: String?,
    @Json(name = "strMeal") val strMeal: String?,
    @Json(name = "strDrinkAlternate") val strDrinkAlternate: String? = null,
    @Json(name = "strCategory") val strCategory: String? = null,
    @Json(name = "strArea") val strArea: String? = null,
    @Json(name = "strInstructions") val strInstructions: String? = null,
    @Json(name = "strMealThumb") val strMealThumb: String? = null,
    @Json(name = "strTags") val strTags: String? = null,
    @Json(name = "strYoutube") val strYoutube: String? = null,

    // Ingredients 1..20
    @Json(name = "strIngredient1") val strIngredient1: String? = null,
    @Json(name = "strIngredient2") val strIngredient2: String? = null,
    @Json(name = "strIngredient3") val strIngredient3: String? = null,
    @Json(name = "strIngredient4") val strIngredient4: String? = null,
    @Json(name = "strIngredient5") val strIngredient5: String? = null,
    @Json(name = "strIngredient6") val strIngredient6: String? = null,
    @Json(name = "strIngredient7") val strIngredient7: String? = null,
    @Json(name = "strIngredient8") val strIngredient8: String? = null,
    @Json(name = "strIngredient9") val strIngredient9: String? = null,
    @Json(name = "strIngredient10") val strIngredient10: String? = null,
    @Json(name = "strIngredient11") val strIngredient11: String? = null,
    @Json(name = "strIngredient12") val strIngredient12: String? = null,
    @Json(name = "strIngredient13") val strIngredient13: String? = null,
    @Json(name = "strIngredient14") val strIngredient14: String? = null,
    @Json(name = "strIngredient15") val strIngredient15: String? = null,
    @Json(name = "strIngredient16") val strIngredient16: String? = null,
    @Json(name = "strIngredient17") val strIngredient17: String? = null,
    @Json(name = "strIngredient18") val strIngredient18: String? = null,
    @Json(name = "strIngredient19") val strIngredient19: String? = null,
    @Json(name = "strIngredient20") val strIngredient20: String? = null,

    // Measures 1..20
    @Json(name = "strMeasure1") val strMeasure1: String? = null,
    @Json(name = "strMeasure2") val strMeasure2: String? = null,
    @Json(name = "strMeasure3") val strMeasure3: String? = null,
    @Json(name = "strMeasure4") val strMeasure4: String? = null,
    @Json(name = "strMeasure5") val strMeasure5: String? = null,
    @Json(name = "strMeasure6") val strMeasure6: String? = null,
    @Json(name = "strMeasure7") val strMeasure7: String? = null,
    @Json(name = "strMeasure8") val strMeasure8: String? = null,
    @Json(name = "strMeasure9") val strMeasure9: String? = null,
    @Json(name = "strMeasure10") val strMeasure10: String? = null,
    @Json(name = "strMeasure11") val strMeasure11: String? = null,
    @Json(name = "strMeasure12") val strMeasure12: String? = null,
    @Json(name = "strMeasure13") val strMeasure13: String? = null,
    @Json(name = "strMeasure14") val strMeasure14: String? = null,
    @Json(name = "strMeasure15") val strMeasure15: String? = null,
    @Json(name = "strMeasure16") val strMeasure16: String? = null,
    @Json(name = "strMeasure17") val strMeasure17: String? = null,
    @Json(name = "strMeasure18") val strMeasure18: String? = null,
    @Json(name = "strMeasure19") val strMeasure19: String? = null,
    @Json(name = "strMeasure20") val strMeasure20: String? = null
)

interface TheMealDbApi {
    @GET("api/json/v1/1/search.php")
    suspend fun searchMeals(@Query("s") query: String): MealDbResponse

    @GET("api/json/v1/1/search.php")
    suspend fun listMealsByLetter(@Query("f") letter: Char): MealDbResponse

    @GET("api/json/v1/1/lookup.php")
    suspend fun lookupMealById(@Query("i") id: String): MealDbResponse

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): MealDbResponse

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealDbResponse

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByMainIngredient(@Query("i") ingredient: String): MealDbResponse

    @GET("api/json/v1/1/categories.php")
    suspend fun getCategories(): MealCategoriesResponse
}
