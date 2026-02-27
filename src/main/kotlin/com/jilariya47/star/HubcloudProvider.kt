package com.jilariya47.star

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class HubcloudProvider : MainAPI() {
    override var name = "4KHDHub"
    override var supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "hi-en"
    // નવી વેબસાઈટ અહીં સેટ કરી છે
    override var mainUrl = "https://4khdhub.dad"

    // Home page latest movies/series
    override suspend fun getMainPage(): HomePageResponse {
        // નવી સાઇટ પર સામાન્ય રીતે હોમ પેજ પર જ ડેટા હોય છે
        val doc = app.get(mainUrl).document
        // 'article' અથવા 'div.post-item' જેવા કોમન ક્લાસ હોઈ શકે છે
        val items = doc.select("div.post-item, article").mapNotNull { element ->
            val title = element.selectFirst("h2 a, h3 a")?.text() ?: return@mapNotNull null
            val link = element.selectFirst("h2 a, h3 a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("src")
            
            MovieSearchResponse(
                name = title,
                url = link,
                apiName = this.name,
                type = if (title.contains("Season", ignoreCase = true)) TvType.TvSeries else TvType.Movie,
                posterUrl = poster,
            )
        }
        return HomePageResponse(listOf(HomePageList("Latest Uploads", items)))
    }

    // Search movies
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.replace(" ", "+")}"
        val doc = app.get(url).document
        
        return doc.select("div.post-item, article").mapNotNull { element ->
            val title = element.selectFirst("h2 a, h3 a")?.text() ?: return@mapNotNull null
            val link = element.selectFirst("h2 a, h3 a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("src")
            
            MovieSearchResponse(
                name = title,
                url = link,
                apiName = this.name,
                type = if (title.contains("Season", ignoreCase = true)) TvType.TvSeries else TvType.Movie,
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
        // બટન ટેક્સ્ટ 'Download' અથવા 'G-Drive' હોઈ શકે છે
        val downloadLinks = doc.select("a[href*=/archives/], a:contains(Download), a:contains(Drive)")
        
        if (downloadLinks.isEmpty()) return false

        downloadLinks.forEach { link ->
            val href = link.attr("href")
            val name = link.text()
            
            callback.invoke(
                ExtractorLink(
                    source = this.name,
                    name = if (name.length > 20) "Mirror" else name,
                    url = if (href.startsWith("http")) href else mainUrl + href,
                    referer = mainUrl,
                    quality = Qualities.Unknown.value,
                    isM3u8 = href.contains(".m3u8")
                )
            )
        }
        return true
    }
}
