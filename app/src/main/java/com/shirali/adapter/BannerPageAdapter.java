package com.shirali.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shirali.R;
import com.shirali.model.browse.Banner;
import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;

import java.util.ArrayList;

/**
 * Created by Sagar on 9/5/18.
 */

public class BannerPageAdapter extends android.support.v4.view.PagerAdapter {

    private ArrayList<Banner> listOfBanner;
    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener mItemClickListener;

    public BannerPageAdapter(Context context, ArrayList<Banner> listOfBanner) {
        this.context = context;
        this.listOfBanner = listOfBanner;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return listOfBanner.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View imageLayout = inflater.inflate(R.layout.banner_layout, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout
                .findViewById(R.id.image);

        /* --- KIPL -> AKM: Update Glide version lib---*/
        Glide.with(context)
                .load(listOfBanner.get(position).imageUrl)
                .placeholder(R.drawable.imglogo)
                .diskCacheStrategy(DiskCacheStrategy.ALL).crossFade()
                .error(R.drawable.imglogo)
                .into(imageView);
        view.addView(imageLayout, 0);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listOfBanner.get(position).type.equalsIgnoreCase("song")) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        mItemClickListener.onItemClick(v, "song", position);
                    } else {
                        if (listOfBanner.get(position).isPremium) {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_songs_as_you_want));
                        } else {
                            mItemClickListener.onItemClick(v, "song", position);
                        }
                    }
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("album")) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        mItemClickListener.onItemClick(v, "album", position);
                    } else {
                        if (listOfBanner.get(position).isPremium) {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_album_songs_as_you_want));
                        } else {
                            mItemClickListener.onItemClick(v, "album", position);
                        }
                    }
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("artist")) {
                    if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                        mItemClickListener.onItemClick(v, "artist", position);
                    } else {
                        if (listOfBanner.get(position).isPremium) {
                            Utility.showSubscriptionAlert(context, context.getResources().getString(R.string.with_shiraLi_premium_you_can_play_as_many_premium_artist_songs_as_you_want));
                        } else {
                            mItemClickListener.onItemClick(v, "artist", position);
                        }
                    }
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("playlist")) {
                    mItemClickListener.onItemClick(v, "playlist", position);
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("subscription")) {
                    mItemClickListener.onItemClick(v, "subscription", position);
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("radiostation")) {
                    mItemClickListener.onItemClick(v, "radiostation", position);
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("newreleases")) {
                    mItemClickListener.onItemClick(v, "newreleases", position);
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("artistsearch")) {
                    mItemClickListener.onItemClick(v, "artistsearch", position);
                } else if (listOfBanner.get(position).type.equalsIgnoreCase("playlistdisplay")) {
                    mItemClickListener.onItemClick(v, "playlistdisplay", position);
                }else if (listOfBanner.get(position).type.equalsIgnoreCase("weburl")) {
                    mItemClickListener.onItemClick(v, "weburl", position);
                }else if (listOfBanner.get(position).type.equalsIgnoreCase("settings")) {
                    mItemClickListener.onItemClick(v, "settings", position);
                }
            }
        });
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String type, int position);
    }

}
