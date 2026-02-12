package com.jilariya47.star

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class HubcloudProvider : MainAPI() {
    override var name = "Hubcloud 4K"
    override var supportedTypes = setOf(TvType.Movie)
    override var lang = "hi-en"
    override var mainUrl = "https://hubcloud.foo"

    // Home page latest movies
    override suspend fun getMainPage(): HomePageResponse {
        val doc = app.get("$mainUrl/drive").document
        val items = doc.select("div.drive-item").mapNotNull { element ->
            val title = element.selectFirst("h3 a")?.text() ?: return@mapNotNull null
            val link = element.selectFirst("h3 a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("src")
            
            MovieSearchResponse(
                name = title,
                url = link,
                apiName = this.name,
                type = TvType.Movie,
                posterUrl = poster,
            )
        }
        return HomePageResponse(listOf(HomePageList("Latest Uploads", items)))
    }

    // Search movies
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search?q=${query.replace(" ", "+")}"
        val doc = app.get(url).document
        
        return doc.select("div.drive-item").mapNotNull { element ->
            val title = element.selectFirst("h3 a")?.text() ?: return@mapNotNull null
            val link = element.selectFirst("h3 a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("src")
            
            MovieSearchResponse(
                name = title,
                url = link,
                apiName = this.name,
                type = TvType.Movie,
                posterUrl = poster,
            )
        }
    }

    // Get streaming links
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val downloadBtn = doc.selectFirst("a:contains(Download)") ?: return false
        val directUrl = downloadBtn.attr("href")
        
        callback.invoke(
            ExtractorLink(
                source = this.name,
                name = "Hubcloud",
                url = if (directUrl.startsWith("http")) directUrl else mainUrl + directUrl,
                referer = mainUrl,
                quality = Qualities.Unknown.value,
                isM3u8 = false
            )
        )
        return true
    }
}
