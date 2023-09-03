package com.bankmandiri.mobileapps.activity.news

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bankmandiri.mobileapps.R
import com.bankmandiri.mobileapps.adapter.TopNewsAdapter
import com.bankmandiri.mobileapps.api.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopNewsActivity : AppCompatActivity() {

    private lateinit var topNewsAdapter: TopNewsAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentPage = 1
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_news)

        recyclerView = findViewById(R.id.recyclerView)
        topNewsAdapter = TopNewsAdapter(emptyList())

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TopNewsActivity)
            adapter = topNewsAdapter
        }

        topNewsAdapter.onItemClick = { article ->
            val intent = Intent(this@TopNewsActivity, DetailTopNews::class.java)
            intent.putExtra("ArticlesItem", article)
            startActivity(intent)
        }

        val itemButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        itemButton.setOnClickListener {
            finish()
        }

        // Implementasi infinite scrolling
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
                    fetchNews()
                }
            }
        })

        // Panggil fungsi untuk mengambil data berita
        fetchNews()
    }

    private fun fetchNews() {
        val apiKey = "f624b53a7000484ab2aeab1e206d1371"
        val country = "us"

        val apiService = ApiClient.apiService
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getTopHeadlines(country, currentPage, apiKey)
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()

                    // Filter out articles with null values
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
                        topNewsAdapter.articles += validArticles
                        topNewsAdapter.notifyDataSetChanged()
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
