package com.pankajkcodes.wpapi.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pankajkcodes.wpapi.R;
import com.pankajkcodes.wpapi.model.Media;
import com.pankajkcodes.wpapi.sqlite.PostDB;
import com.pankajkcodes.wpapi.util.InternetConnection;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    private Toolbar postToolbar;
    private TextView postTitle;
    private WebView postContent;
    private ImageView postBackdrop;
    View parentView;
    boolean isItemSelected;


    public static Intent createIntent(Context context, int id, int featuredMedia, String title,
                                      String excerpt, String content){
        Intent intent = new Intent(context, PostActivity.class);
        //Setzen des wertes aus dem Intent
        intent.putExtra("postId", id);
        intent.putExtra("featuredMedia",featuredMedia);
        intent.putExtra("postExcerpt", excerpt);
        intent.putExtra("postTitle", title);
        intent.putExtra("postContent",content);
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Get Intent
        int id = (int) getIntent().getSerializableExtra("postId");
        int featuredMedia = (int) getIntent().getSerializableExtra("featuredMedia");
        String title =  getIntent().getSerializableExtra("postTitle").toString();
        String content = getIntent().getSerializableExtra("postContent").toString()
                .replaceAll("\\\\n", "<br>").replaceAll("\\\\r", "").replaceAll("\\\\", "");;


        initToolbar(title, id);
        initPost(title);
        initWebView(content);



        //Call Media
        if(InternetConnection.checkInternetConnection(getApplicationContext())) {
            ApiService api = WordPressClient.getApiService();

            Call<Media> call = api.getPostThumbnail(featuredMedia);

            call.enqueue(new Callback<Media>() {
                @Override
                public void onResponse(Call<Media> call, Response<Media> response) {

                    Log.d("ResponseMediaCode", "Status = " + response.code());

                    if (response.code() != 404) {
                        Media media = response.body();
                        String mediaUrl = media.getGuid().get("rendered").toString().replaceAll("\"", "");

                        Glide.with(getApplicationContext()).load(mediaUrl)
                                .thumbnail(0.5f)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(postBackdrop);
                    }else{

                    }
                }

                @Override
                public void onFailure(Call<Media> call, Throwable t) {
                    Log.d("RetrofitResponse", "Error");
                }
            });

        }else{
            Snackbar.make(parentView, "Can't connect to the Internet", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    //Init Toolbar but do not set media
    private void initPost(String title){

        postBackdrop = findViewById(R.id.post_backdrop);
        postTitle = findViewById(R.id.post_title);
        postTitle.setText(title);
        postContent = findViewById(R.id.webview);

    }



    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Get Intent
        int id = (int) getIntent().getSerializableExtra("postId");
        String title =  getIntent().getSerializableExtra("postTitle").toString();
        String excerpt = getIntent().getSerializableExtra("postExcerpt").toString();
        String content = getIntent().getSerializableExtra("postContent").toString().replaceAll("\\\\n", "").replaceAll("\\\\r", "").replaceAll("\\\\", "");

        //Toggle Navigation icon


        if(!isItemSelected){
            item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp,getTheme()));

            isItemSelected = true;
            PostDB.getInstance(getApplicationContext()).insert(id, title, excerpt, isItemSelected);
        }else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp,getTheme()));
            isItemSelected = false;

            PostDB.getInstance(getApplicationContext()).delete(id);
        }


        return super.onOptionsItemSelected(item);
    }


    private static class MyWebView extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initToolbar(String title, int id){

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        initCollapsingToolbar(title);


        isItemSelected = PostDB.getInstance(getApplicationContext()).getDbPostIsFav(id);
        if(isItemSelected){
            Log.d("SelectedItem", " "+ isItemSelected);


        }else {
            Log.d("SelectedItem", " " + isItemSelected);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Init CollapsingToolbarLayout
    private void initCollapsingToolbar(final String title){
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.post_collapsing_toolbarLayout);
        collapsingToolbar.setTitle(" ");

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String content){
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(PostActivity.this);
        progressDialog.setTitle(getString(R.string.progressdialog_title));
        progressDialog.setMessage(getString(R.string.progressdialog_message));

        //Set Html content
        content = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" +
                "<script src=\"prism.js\"></script>" +
                "<div class=\"content\">" + content+ "</div>";


        Log.d("WebViewContent", content);

        postContent.getSettings().setLoadsImagesAutomatically(true);
        postContent.getSettings().setJavaScriptEnabled(true);
        postContent.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressDialog.dismiss();

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {


                return super.shouldOverrideUrlLoading(view, request);


            }
        });



        postContent.loadDataWithBaseURL("file:///android_asset/*",content,
                "text/html; charset=utf-8", "UTF-8", null);



    }




    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_to_favorite_menu, menu);

        if(isItemSelected) {
            menu.findItem(R.id.add_as_favorite).setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp, getTheme()));
        }else {
            menu.findItem(R.id.add_as_favorite).setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp, getTheme()));
        }
        return true;
    }

}
