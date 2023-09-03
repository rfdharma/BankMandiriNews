package com.bankmandiri.mobileapps.activity.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bankmandiri.mobileapps.R
import com.bankmandiri.mobileapps.activity.news.DetailAllNews
import com.bankmandiri.mobileapps.adapter.AllNewsAdapter
import com.bankmandiri.mobileapps.api.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var allNewsAdapter: AllNewsAdapter
    private lateinit var resultTextView: TextView
    private lateinit var sortBySpinner: Spinner

    private var currentPage = 1
    private var isLoading = false
    private var currentQuery: String? = null
    private var currentSort = "relevancy" // Default sort option

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        resultTextView = findViewById(R.id.result)
        sortBySpinner = findViewById(R.id.sortby_spinner)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAllNews)
        allNewsAdapter = AllNewsAdapter(emptyList())
        recyclerView.adapter = allNewsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        allNewsAdapter.onItemClick = { article ->
            val intent = Intent(this@SearchActivity, DetailAllNews::class.java)
            intent.putExtra("ArticlesItem", article)
            startActivity(intent)
        }

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    currentQuery = query
                    currentPage = 1
                    fetchAllNews(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Anda dapat mengimplementasikan filter real-time jika diperlukan
                return true
            }
        })

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_search
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_search -> true

                R.id.bottom_user -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Mendapatkan daftar pilihan pengurutan dari sumber daya string
        val sortOptions = resources.getStringArray(R.array.sortBy)

        // Membuat adapter untuk Spinner
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)

        // Menentukan tampilan dropdown untuk adapter Spinner
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Menetapkan adapter ke Spinner
        sortBySpinner.adapter = spinnerAdapter

        // Menambahkan listener untuk menangani perubahan pengurutan yang dipilih
        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Mendapatkan nilai pengurutan yang dipilih
                val selectedSort = sortOptions[position]

                // Memperbarui nilai currentSort berdasarkan pilihan pengurutan yang dipilih
                when (selectedSort) {
                    "Relevancy" -> currentSort = "relevancy"
                    "Popularity" -> currentSort = "popularity"
                    "Date" -> currentSort = "publishedAt"
                }

                // Di sini Anda dapat memanggil fetchAllNews() lagi dengan parameter pengurutan yang sesuai.
                fetchAllNews(currentQuery ?: "")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak perlu melakukan apa-apa jika tidak ada yang dipilih.
            }
        }

        // Implementasi infinite scrolling di sini
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                    isLoading = true
                    currentPage++
                    currentQuery?.let { fetchAllNews(it) }
                }
            }
        })
    }

    private fun fetchAllNews(query: String) {
        val apiKey = "f624b53a7000484ab2aeab1e206d1371"
        val apiService = ApiClient.apiService
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getEverything(query, currentPage, apiKey, currentSort)
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    resultTextView.text = "${response.body()?.totalResults ?: 0} Result found :"
                    val validArticles = articles.filter { article ->
                        article.publishedAt != null &&
                                article.author != null &&
                                article.urlToImage != null &&
                                article.description != null &&
                                article.source != null &&
                                article.title != null &&
                                article.url != null &&
                                article.content != null
                    }
//                    val totalResults = validArticles.size
//                    resultTextView.text = "$totalResults Result found :"
                    withContext(Dispatchers.Main) {
                        if (currentPage == 1) {
                            allNewsAdapter.newsList = validArticles
                        } else {
                            allNewsAdapter.newsList += validArticles
                        }
                        allNewsAdapter.notifyDataSetChanged()
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
