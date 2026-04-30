package com.eric.wandroid.data.remote.api

import com.eric.wandroid.data.remote.dto.ApiResponse
import com.eric.wandroid.data.remote.dto.ArticleDto
import com.eric.wandroid.data.remote.dto.BannerDto
import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.data.remote.dto.CoinRecordDto
import com.eric.wandroid.data.remote.dto.CoinUserInfoDto
import com.eric.wandroid.data.remote.dto.HotKeyDto
import com.eric.wandroid.data.remote.dto.NavigationDto
import com.eric.wandroid.data.remote.dto.PageDto
import com.eric.wandroid.data.remote.dto.PopularColumnDto
import com.eric.wandroid.data.remote.dto.ShareUserArticlesDto
import com.eric.wandroid.data.remote.dto.TodoDto
import com.eric.wandroid.data.remote.dto.UserMessageDto
import com.eric.wandroid.data.remote.dto.UserInfoResponseDto
import com.eric.wandroid.data.remote.dto.UserDto
import com.eric.wandroid.data.remote.dto.WebsiteDto
import com.eric.wandroid.data.remote.dto.WendaCommentDto
import retrofit2.http.GET
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WanAndroidApiService {
    @GET("article/list/{page}/json")
    suspend fun getHomeArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @GET("article/top/json")
    suspend fun getTopArticles(): ApiResponse<List<ArticleDto>>

    @GET("banner/json")
    suspend fun getBanners(): ApiResponse<List<BannerDto>>

    @GET("hotkey/json")
    suspend fun getHotKeys(): ApiResponse<List<HotKeyDto>>

    @GET("friend/json")
    suspend fun getCommonWebsites(): ApiResponse<List<WebsiteDto>>

    @GET("popular/wenda/json")
    suspend fun getPopularWenda(): ApiResponse<List<ArticleDto>>

    @GET("popular/column/json")
    suspend fun getPopularColumns(): ApiResponse<List<PopularColumnDto>>

    @GET("popular/route/json")
    suspend fun getPopularRoutes(): ApiResponse<List<ChapterDto>>

    @GET("tree/json")
    suspend fun getSystemTree(): ApiResponse<List<ChapterDto>>

    @GET("navi/json")
    suspend fun getNavigationItems(): ApiResponse<List<NavigationDto>>

    @GET("article/list/{page}/json")
    suspend fun getSystemArticles(
        @Path("page") page: Int,
        @Query("cid") categoryId: Int
    ): ApiResponse<PageDto<ArticleDto>>

    @GET("project/tree/json")
    suspend fun getProjectCategories(): ApiResponse<List<ChapterDto>>

    @GET("project/list/{page}/json")
    suspend fun getProjectArticles(
        @Path("page") page: Int,
        @Query("cid") categoryId: Int
    ): ApiResponse<PageDto<ArticleDto>>

    @FormUrlEncoded
    @POST("article/query/{page}/json")
    suspend fun searchArticles(
        @Path("page") page: Int,
        @Field("k") keyword: String
    ): ApiResponse<PageDto<ArticleDto>>

    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): ApiResponse<UserDto>

    @FormUrlEncoded
    @POST("user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("repassword") repeatPassword: String
    ): ApiResponse<UserDto>

    @GET("user/logout/json")
    suspend fun logout(): ApiResponse<Any>

    @GET("user/lg/userinfo/json")
    suspend fun getUserInfo(): ApiResponse<UserInfoResponseDto>

    @POST("lg/collect/{id}/json")
    suspend fun collectArticle(@Path("id") id: Int): ApiResponse<Any>

    @POST("lg/uncollect_originId/{id}/json")
    suspend fun uncollectArticle(@Path("id") id: Int): ApiResponse<Any>

    @GET("lg/collect/list/{page}/json")
    suspend fun getCollectedArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @GET("wenda/list/{page}/json")
    suspend fun getWendaArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @GET("wenda/comments/{id}/json")
    suspend fun getWendaComments(@Path("id") articleId: Int): ApiResponse<PageDto<WendaCommentDto>>

    @GET("user_article/list/{page}/json")
    suspend fun getSquareArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @GET("user/{id}/share_articles/{page}/json")
    suspend fun getShareUserArticles(
        @Path("id") userId: Int,
        @Path("page") page: Int
    ): ApiResponse<ShareUserArticlesDto>

    @GET("user/lg/private_articles/{page}/json")
    suspend fun getPrivateSharedArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @POST("lg/user_article/delete/{id}/json")
    suspend fun deletePrivateSharedArticle(@Path("id") id: Int): ApiResponse<Any>

    @FormUrlEncoded
    @POST("lg/user_article/add/json")
    suspend fun addSharedArticle(
        @Field("title") title: String,
        @Field("link") link: String
    ): ApiResponse<Any>

    @GET("wxarticle/chapters/json")
    suspend fun getWechatCategories(): ApiResponse<List<ChapterDto>>

    @GET("wxarticle/list/{id}/{page}/json")
    suspend fun getWechatArticles(
        @Path("id") categoryId: Int,
        @Path("page") page: Int
    ): ApiResponse<PageDto<ArticleDto>>

    @GET("article/listproject/{page}/json")
    suspend fun getLatestProjectArticles(@Path("page") page: Int): ApiResponse<PageDto<ArticleDto>>

    @GET("lg/coin/userinfo/json")
    suspend fun getCoinUserInfo(): ApiResponse<CoinUserInfoDto>

    @GET("lg/coin/list/{page}/json")
    suspend fun getCoinRecords(@Path("page") page: Int): ApiResponse<PageDto<CoinRecordDto>>

    @GET("message/lg/count_unread/json")
    suspend fun getUnreadMessageCount(): ApiResponse<Int>

    @GET("message/lg/unread_list/{page}/json")
    suspend fun getUnreadMessages(@Path("page") page: Int): ApiResponse<PageDto<UserMessageDto>>

    @GET("message/lg/readed_list/{page}/json")
    suspend fun getReadMessages(@Path("page") page: Int): ApiResponse<PageDto<UserMessageDto>>

    @GET("lg/todo/v2/list/{page}/json")
    suspend fun getTodos(
        @Path("page") page: Int,
        @Query("status") status: Int? = null
    ): ApiResponse<PageDto<TodoDto>>

    @FormUrlEncoded
    @POST("lg/todo/add/json")
    suspend fun addTodo(
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("date") date: String,
        @Field("type") type: Int,
        @Field("priority") priority: Int
    ): ApiResponse<TodoDto>

    @FormUrlEncoded
    @POST("lg/todo/update/{id}/json")
    suspend fun updateTodo(
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("date") date: String,
        @Field("type") type: Int,
        @Field("priority") priority: Int
    ): ApiResponse<TodoDto>

    @POST("lg/todo/delete/{id}/json")
    suspend fun deleteTodo(@Path("id") id: Int): ApiResponse<Any>

    @FormUrlEncoded
    @POST("lg/todo/done/{id}/json")
    suspend fun updateTodoStatus(
        @Path("id") id: Int,
        @Field("status") status: Int
    ): ApiResponse<TodoDto>
}
