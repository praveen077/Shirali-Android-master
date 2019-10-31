package com.shirali.network;

import com.shirali.model.GenreAlbum;
import com.shirali.model.NewRelease;
import com.shirali.model.StreamCount;
import com.shirali.model.browse.NewHomeList;
import com.shirali.model.campaign.Campaign;
import com.shirali.model.campaign.CampaignResponse;
import com.shirali.model.comman.AppCount;
import com.shirali.model.mymusic.MyMusic;
import com.shirali.model.playlist.PlayListRequest;
import com.shirali.model.playlist.Playlist;
import com.shirali.model.playlist.PlaylistDetail;
import com.shirali.model.playlist.PlaylistResponse;
import com.shirali.model.playlist.PlaylistUpdate;
import com.shirali.model.radio.Radio;
import com.shirali.model.recent.Recent;
import com.shirali.model.search.AdvanceSearch;
import com.shirali.model.search.GenreData;
import com.shirali.model.search.Search;
import com.shirali.model.search.SearchItem;
import com.shirali.model.setting.AppSetting;
import com.shirali.model.share.ShareAlbum;
import com.shirali.model.share.SharePlaylist;
import com.shirali.model.share.ShareSong;
import com.shirali.model.songs.AlbumSongList;
import com.shirali.model.songs.AllArtist;
import com.shirali.model.songs.ArtistInfo;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.Home;
import com.shirali.model.songs.SongDetail;
import com.shirali.model.songs.SongsList;
import com.shirali.model.songs.artist.ArtistNewAlbum;
import com.shirali.model.songs.artist.ArtistPlaylist;
import com.shirali.model.songs.artist.ArtistPopularSong;
import com.shirali.model.stations.StationList;
import com.shirali.model.user.ForgotPassword;
import com.shirali.model.user.PaymentInfo;
import com.shirali.model.user.UpdatePayment;
import com.shirali.model.user.UserModel;
import com.shirali.model.user.UserPlaylist;
import com.shirali.model.user.UserSongStatus;
import com.shirali.model.user.UserSubscription;
import com.shirali.model.v2model.AllArtistByChar;
import com.shirali.model.v2model.AllArtists;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Sagar on 3/7/17.
 */

public interface Api {

    @POST("auth/signup")
    Call<UserModel> signUp(@Body HashMap<String, Object> hashMap);

    @POST("auth/login")
    Call<UserModel> login(@Body HashMap<String, Object> hashMap);

    @GET("song/active")
    Call<SongsList> songsList();

    @GET("song/newReleases")
    Call<NewRelease> newReleaseAlbum(@Query("genre") String genre);

    @GET("song/popular")
    Call<NewRelease> popular(@Query("genre") String genre);

    @GET("song/recommendations")
    Call<NewRelease> recommandation();

    @GET("song/recentlyPlayedSongs/all")
    Call<SongsList> recentPlayAllSong(@Query("page") String number);

    @GET("song/preferredSongs/all")
    Call<SongsList> preferredSong(@Query("page") String number);

    @GET("user/home/{user_id}")
    Call<Home> getUserMusicData(@Path("user_id") String id);

    @GET("genre/active")
    Call<GenresList> getGenres(@Query("setting") String isCheck);

    @PUT("user/{id}")
    Call<UserModel> updateGenres(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("playlist/shirali")
    Call<Playlist> getShiraliPlaylist(@Query("genre") String genre);

    @PUT("user/{userId}/mymusic/{songId}")
    Call<UserModel> addToPlaylist(@Path("userId") String userId, @Path("songId") String songId);

    @DELETE("user/{userId}/mymusic/{songId}")
    Call<UserModel> removeFromPlaylist(@Path("userId") String userId, @Path("songId") String songId);

    @PUT("user/{userId}/recentlyPlayed/{songId}")
    Call<UserModel> addInRecentPlayed(@Path("userId") String userId, @Path("songId") String songId);

    @GET("search/{tag}")
    Call<Search> getSearchItem(@Path("tag") String tag);

    @POST("playlist")
    Call<PlayListRequest> CreatePlaylist(@Body HashMap<String, Object> hashMap);

    @GET("user/mymusic/{userId}")
    Call<MyMusic> getMusic(@Path("userId") String userId);

    @GET("user/mymusic/{userId}")
    Call<MyMusic> getMyMusicWithPage(@Path("userId") String id, @Query("page") String page_no);

    @GET("user/mymusic/{userId}")
    Call<MyMusic> getMyMusic(@Path("userId") String id);

    @GET("user/{userId}/playlists")
    Call<PlaylistResponse> getUserPlaylist(@Path("userId") String userId);

    @GET("setting/getSetting")
    Call<AppSetting> getAppSetting();

    @POST("user/{id}/subscribe")
    Call<UserSubscription> subscribePlan(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("album/{id}/songs")
    Call<SongsList> getAlbumSong(@Path("id") String id);

    @GET("user/{id}")
    Call<UserModel> getUserInfo(@Path("id") String id);

    @POST("user/{id}/subscribe")
    Call<UserSubscription> subscribeUser(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("artist/{id}")
    Call<ArtistInfo> getArtistDetail(@Path("id") String id);

    @GET("artist")
    Call<AllArtist> getAllArtistDetail();

    @PUT("playlist/{id}")
    Call<PlayListRequest> editPlaylist(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("album/{album_id}/songs")
    Call<AlbumSongList> getPlaylist(@Path("album_id") String id);

    @PATCH("playlist/{song_id}")
    Call<PlaylistUpdate> addToPlaylist(@Path("song_id") String id, @Body HashMap<String, Object> hashMap);

    @GET("user/{userid}/playlists")
    Call<UserPlaylist> getPlaylistSong(@Path("userid") String id);

    @PUT("user/{user_id}/playlist/follow/{playlist_id}")
    Call<UserModel> followShiraliPlaylist(@Path("user_id") String id, @Path("playlist_id") String play_id);

    @PUT("user/{user_id}/playlist/unfollow/{playlist_id}")
    Call<UserModel> unfollowShiraliPlaylist(@Path("user_id") String id, @Path("playlist_id") String play_id);

    @DELETE("playlist/{playlist_id}")
    Call<Playlist> deletePlaylist(@Path("playlist_id") String id);

    @GET("user/{user_id}/advertisement")
    Call<Campaign> getAdsAccordingUser(@Path("user_id") String id);

    @PUT("campaign/{id}/click")
    Call<CampaignResponse> clickCountOnAd(@Path("id") String id);

    @POST("song/{song_id}/stream")
    Call<StreamCount> songStreamCount(@Path("song_id") String song_id, @Body HashMap<String, Object> hashMap);

    @POST("user/{id}/userStats")
    Call<StreamCount> userStats(@Path("id") String user_id);

    @POST("song/{userid}/unlike/{songId}")
    Call<UserSongStatus> setunlike(@Path("userid") String userid, @Path("songId") String songsid);

    @POST("song/{userid}/like/{songId}")
    Call<UserSongStatus> setlike(@Path("userid") String userid, @Path("songId") String songsid);

    @GET("song/{userid}/status/{songId}")
    Call<UserSongStatus> getLikes(@Path("userid") String userid, @Path("songId") String songsid);

    @GET("playlist/{playlist_id}/songs")
    Call<PlaylistDetail> getSelectedPlaylist(@Path("playlist_id") String id);

    @GET("album/newReleaseAlbums/all")
    Call<NewRelease> getAllNewReleaseAlbum(@Query("page") String page_no);

    @GET("song/newReleaseSongs/all")
    Call<NewRelease> getAllNewReleaseSong(@Query("page") String page_no);

    @GET("songs/all")
    Call<NewRelease> getSongByGenre(@Query("genre") String genres, @Query("page") String page_no);

    @PUT("user/{id}")
    Call<UserModel> updateProfile(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("user/{id}/payoutMethod")
    Call<UpdatePayment> getPayment(@Path("id") String id);

    @PUT("user/{id}/updatePaymentMethod")
    Call<UserSubscription> updatePayment(@Path("id") String id, @Body HashMap<String, Object> hashMap);

    @GET("artist/{id}/newReleaseAlbums")
    Call<ArtistNewAlbum> getArtistNewAlbum(@Path("id") String id, @Query("page") String page_no);

    @GET("artist/{id}/popularPlaylist")
    Call<ArtistPlaylist> getArtistPlaylist(@Path("id") String id, @Query("page") String page_no);

    @GET("artist/{id}/popularSongs")
    Call<ArtistPopularSong> getArtistPopularSong(@Path("id") String id, @Query("page") String page_no);

    @GET("artist/{id}/albums")
    Call<ArtistNewAlbum> getArtistAlbums(@Path("id") String id, @Query("page") String page_no);

    @POST("auth/resetPasswordLink")
    Call<ForgotPassword> forgotPassword(@Body HashMap<String, Object> hashMap);

    @POST("auth/resetPassword")
    Call<ForgotPassword> updatePassword(@Body HashMap<String, Object> hashMap);

    // for deep link
    @GET("song/{id}/detail")
    Call<SongDetail> getSongDetail(@Path("id") String id);

    @GET("album/{id}/detail")
    Call<ShareAlbum> getAlbumById(@Path("id") String id);

    @GET("playlist/{id}/songs")
    Call<SharePlaylist> getSongByPlaylistId(@Path("id") String id);

    @POST("share")
    Call<ShareSong> share(@Body HashMap<String, Object> hashMap);

    @POST("updateAppStats")
    Call<AppCount> appInstall(@Body HashMap<String, Object> hashMap);

    @GET("album/recommended/all")
    Call<NewRelease> getAllRecommandedAlbum(@Query("page") String page_no);

    @GET("song/recommended/all")
    Call<SongsList> getAllRecommandedSong(@Query("page") String page_no);

    @POST("user/{id}/cancelSubscription")
    Call<AppCount> cancelSubscription(@Path("id") String user_id);

    @PUT("user/{user_id}/recentlyPlayed/{song_id}")
    Call<UserModel> updateRecent(@Path("user_id") String user, @Path("song_id") String song);

    @GET("user/{user_id}/recentlyPlayed")
    Call<Recent> getRecent(@Path("user_id") String user);

    @GET("user/{id}/payoutMethod")
    Call<PaymentInfo> getPayoutMethod(@Path("id") String id);

    @GET("album/recentlyPlayedAlbums/all")
    Call<NewRelease> getRecentlyPlayedAlbum(@Query("page") String page_no);

    @GET("song/popular")
    Call<NewRelease> getPopularSong(@Query("page") String genre);

    @GET("artist/{id}/albums")
    Call<ArtistNewAlbum> getArtistAlbumsDetail(@Path("id") String id, @Query("page") String page_no);

    @GET("search/suggesstions/{term}")
    Call<SearchItem> searchResult(@Path("term") String tag);

    @GET("search/advance/{term}")
    Call<AdvanceSearch> searchAdavnceResult(@Path("term") String tag);

    @GET("search/albums/all/{term}")
    Call<AdvanceSearch> searchAllAlbum(@Path("term") String tag);

    @GET("search/songs/all/{term}")
    Call<AdvanceSearch> searchAllSong(@Path("term") String tag);

    @GET("search/artists/all/{term}")
    Call<AdvanceSearch> searchAllArtists(@Path("term") String tag);

    @POST("song/skipsong")
    Call<ResponseBody> skipSong(@Body HashMap<String, Object> hashMap);

    @GET("genres/{genreid}/fetchDataByGenre")
    Call<GenreData> getGenreDetail(@Path("genreid") String genreid);

    @GET("genres/{genreid}/fetchSongsByGenre")
    Call<SongsList> getGenreSong(@Path("genreid") String genreid, @Query("page") String page_no);

    @GET("genres/{genreid}/fetchAlbumsByGenre")
    Call<GenreAlbum> getGenreAlbum(@Path("genreid") String genreid, @Query("page") String page_no);

    @GET("genres/{genreid}/fetchArtistsByGenre")
    Call<AdvanceSearch> getGenreArtist(@Path("genreid") String genreid, @Query("page") String page_no);

    @GET("radiostation/all")
    Call<StationList> getAllRadioStation();

    @GET("user/homenew/{user_id}")
    Call<NewHomeList> getNewHomeData(@Path("user_id") String user_id);

    @PUT("user/{user_id}/recentlyPlayed/{type}/{id}")
    Call<UserModel> updateRecentWithType(@Path("user_id") String user, @Path("id") String id, @Path("type") String type);

    @GET("artist/getartistslisting/all")
    Call<AllArtists> getAllArtist(@Query("term") String page);

    @GET("artist/getartistslistingbychar/all")
    Call<AllArtistByChar> getAllArtistByChar(@Query("char") String chara, @Query("term") String term, @Query("page") String page);

    @GET("user/{user_id}/recentlyPlayedNew")
    Call<Recent> getRecentNew(@Path("user_id") String user);

    @GET("artist/{id}")
    Call<ArtistInfo> getArtistNew(@Path("id") String artist);

    @POST("radiostation/{stattionId}/unlike/{userid}")
    Call<UserSongStatus> setStationUnlike(@Path("userid") String userid, @Path("stattionId") String songsid);

    @POST("radiostation/{stattionId}/like/{userid}")
    Call<UserSongStatus> setStationLike(@Path("userid") String userid, @Path("stattionId") String songsid);

    @GET("radiostation/{stattionId}/status/{userid}")
    Call<UserSongStatus> getStationLikes(@Path("userid") String userid, @Path("stattionId") String songsid);

    @GET("search")
    Call<Radio> getRadioArtwork(@Query("term") String songsid, @Query("entity") String page);
}
