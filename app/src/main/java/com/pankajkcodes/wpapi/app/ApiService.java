package com.pankajkcodes.wpapi.app;

import com.pankajkcodes.wpapi.model.Media;
import com.pankajkcodes.wpapi.model.Post;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("posts")
    Call<List<Post>> getPosts();

//    @GET("categories")
//    Call<List<dynami>> getCategories();

    @GET("posts/{id}")
    Call<Post> getPostById(@Path("id") int postId);

    @GET("media/{featured_media}")
    Call<Media> getPostThumbnail(@Path("featured_media") int media);


}
