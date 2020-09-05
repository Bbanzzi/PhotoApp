package com.example.photoapp.Data;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.photoapp.LoginInfoProvider;
import com.example.photoapp.PlanList.CreatePlanActivity;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.DateFilter;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.library.v1.proto.JoinSharedAlbumResponse;
import com.google.photos.library.v1.proto.ShareAlbumResponse;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import com.google.photos.types.proto.ShareInfo;
import com.google.photos.types.proto.SharedAlbumOptions;
import com.google.type.Date;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GooglePhotoReference  extends DatabaseReferenceData {

    private static GoogleCredentials googleCredentials;
    private static PhotosLibraryClient photosLibraryClient;

    public GooglePhotoReference(){}
    public GooglePhotoReference(String token) {
        super();
        Log.i( "Googlephoto Connecting", token);
        AccessToken accessToken = new AccessToken(token, null);
        googleCredentials = new GoogleCredentials(accessToken);
        PhotosLibrarySettings settings = null;
        try {
            settings = PhotosLibrarySettings.newBuilder()
                    .setCredentialsProvider(
                            FixedCredentialsProvider.create(googleCredentials))
                    .build();

            photosLibraryClient =
                    PhotosLibraryClient.initialize(settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Googlephoto에 앨범만드는 작업업
    public Map<String,String> setAlbumTitle(String albumTitle) {

        Map<String,String> albumInfo=new HashMap<>();
        Album createdAlbum = photosLibraryClient.createAlbum(albumTitle);

        String albumId = createdAlbum.getId();
        albumInfo.put("AlbumId",albumId);
        albumInfo.put("AlbumTitle",albumTitle);
        SharedAlbumOptions options =
                // Set the options for the album you want to share
                SharedAlbumOptions.newBuilder()
                        .setIsCollaborative(true)
                        .setIsCommentable(true)
                        .build();
        ShareAlbumResponse response = photosLibraryClient.shareAlbum(albumId, options);

        // The response contains the shareInfo object, a url, and a token for sharing
        ShareInfo info = response.getShareInfo();
        // Link to the shared album
        String url = info.getShareableUrl();
        String shareToken = info
                // The share token which other users of your app can use to join the album you shared
                .getShareToken();
        albumInfo.put("AlbumSharedToken",shareToken);
        SharedAlbumOptions sharedOptions = info
                // The options set when sharing this album
                .getSharedAlbumOptions();

        return albumInfo;
    }

    public ArrayList<String> getSharedAlbumInfo() {
        ArrayList<String> albumInfo = new ArrayList<>();
        InternalPhotosLibraryClient.ListSharedAlbumsPagedResponse response = photosLibraryClient.listSharedAlbums();
        for (Album album : response.iterateAll()) {
            albumInfo.add(album.getTitle());
            albumInfo.add(album.getId());
        }
        return albumInfo;
    }


    public static PhotosLibraryClient getPhotosLibraryClient() {
        return photosLibraryClient;
    }

    public boolean getAlbumIsWriteable(String albumId){
        InternalPhotosLibraryClient.ListSharedAlbumsPagedResponse response = photosLibraryClient.listSharedAlbums();
        for (Album album : response.iterateAll()) {
            if(album.getId().equals(albumId)){
                return album.getIsWriteable();
            }
        }
        return false;
    }

    public static String joinAlbum( String shareToken){

        Album sharedAlbum = photosLibraryClient.getSharedAlbum(shareToken);
        if(!sharedAlbum.getShareInfo().getIsJoined()) { // if( sharedAlbum.getIsWriteable())
            JoinSharedAlbumResponse response = photosLibraryClient.joinSharedAlbum(shareToken);
            Album joinedAlbum = response.getAlbum();
            Log.i("joinAlbum,", joinedAlbum.getId());
            return joinedAlbum.getId();
        }else{
            Log.i("joinAlbum,", sharedAlbum.getId());
            return sharedAlbum.getId();
        }
    }

    public void deleteAlbum(String sharedToken){
        photosLibraryClient.leaveSharedAlbum(sharedToken);
    }

}

