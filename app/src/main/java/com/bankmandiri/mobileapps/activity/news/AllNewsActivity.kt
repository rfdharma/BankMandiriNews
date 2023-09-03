package com.bankmandiri.mobileapps.activity.news

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bankmandiri.mobileapps.R
import com.bankmandiri.mobileapps.adapter.AllNewsAdapter
import com.bankmandiri.mobileapps.api.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllNewsActivity : AppCompatActivity() {
    private lateinit var allNewsAdapter: AllNewsAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentPageAllNews = 1
    private var isLoadingAllNews = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_news)

        recyclerView = findViewById(R.id.recyclerViewAllNews)
        allNewsAdapter = AllNewsAdapter(emptyList())

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllNewsActivity)
            adapter = allNewsAdapter
        }

        allNewsAdapter.onItemClick = { article ->
            val intent = Intent(this@AllNewsActivity, DetailAllNews::class.java)
            intent.putExtra("ArticlesItem", article)
            startActivity(intent)
        }

        val itemButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        itemButton.setOnClickListener {
            finish()
        }

        // Panggil fungsi untuk mengambil data berita
        fetchAllNews()
    }

    private fun fetchAllNews() {
        val apiKey = "63a860ab3e8548b9bdcf5769dfb50a9d"
        val q = "bank mandiri indonesia"
        val sort = "relevancy"

        val apiService = ApiClient.apiService
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getEverything(q, currentPageAllNews, apiKey,sort)
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()

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
                    withContext(Dispatchers.Main) {
                        // Append the new data to the adapter
                        allNewsAdapter.newsList += validArticles
                        allNewsAdapter.notifyDataSetChanged()
                        isLoadingAllNews = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
