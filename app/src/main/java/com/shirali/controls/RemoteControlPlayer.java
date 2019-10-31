package com.shirali.controls;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.shirali.interfaces.LikeAndUnlikeCallBack;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.util.Constants;
import com.shirali.util.Utility;

public class RemoteControlPlayer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            /* --- KIPL -> AKM: while playing ads disable control ---*/
            if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing"))
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                        return;
                    } else {
                        if (Constants.isSongPlaying) {
                            return;
                        } else {
                            if (Constants.isPlay) {
                                Controls.pauseControl(context);
                            } else {
                                if (Constants.isSongPlay) {
                                    Controls.playControl(context);
                                } else {
                                    Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                                }
                            }
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (Constants.isSongPlay)
                        if (!Constants.isPlay)
                            Controls.playControl(context);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (Constants.isSongPlay)
                        if (Constants.isPlay)
                            Controls.pauseControl(context);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                        return;
                    } else {
                        if (Constants.isSongPlay) {
                            if (Constants.isSongPlaying) {
                                return;
                            } else {
                                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    Controls.nextControl(context);
                                } else {
                                    UserModel.getInstance().nextPlayMethod(context);
                                }
                            }
                        } else {
                            //It's for change station from notification and lock screen
                            /*if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                Controls.nextStation(context);
                            } else {
                                UserModel.getInstance().stationPlayForFreeUser(context,true);
                            }*/
                            return;
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                        return;
                    } else {
                        if (Constants.isSongPlay) {
                            if (Constants.isSongPlaying) {
                                return;
                            } else {
                                if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                    Controls.previousControl(context);
                                } else {
                                    UserModel.getInstance().previousPlayMethod(context);
                                }
                            }
                        } else {
                            //It's for change station from notification and lock screen
                            /*if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                Controls.previousStation(context);
                            } else {
                                UserModel.getInstance().stationPlayForFreeUser(context,false);
                            }*/
                            return;
                        }
                    }
                    break;
            }
        } else {
            if (intent.getAction().equals(SongPlayService.NOTIFY_PLAY)) {
                if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                    return;
                } else {
                    if (Constants.isSongPlaying) {
                        return;
                    } else {
                        if (Constants.isSongPlay) {
                            Controls.playControl(context);
                        } else {
                            Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_PAUSE)) {
                if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                    return;
                } else {
                    if (Constants.isSongPlaying) {
                        return;
                    } else {
                        Controls.pauseControl(context);
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_NEXT)) {
                if (Constants.isSongComplete) {
                    return;
                } else {
                    if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                        return;
                    } else {
                        if (Constants.isSongPlaying) {
                            return;
                        } else {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                if (Constants.isSongPlay) {
                                    Controls.nextControl(context);
                                } else {
                                    //It's for change station from notification and lock screen
//                                    Controls.nextStation(context);
                                    return;
                                }
                            } else {
                                if (Constants.isSongPlay) {
                                    UserModel.getInstance().nextPlayMethod(context);
                                } else {
                                    //It's for change station from notification and lock screen
                                    /*Controls.nextStation(context);*/
//                                    UserModel.getInstance().stationPlayForFreeUser(context,true);
                                    return;
                                }
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_DELETE)) {
                /* --- KIPL -> AKM: handling ads from closing through notification bar ---*/
                //Intent i = new Intent(context, SongPlayService.class);
                //context.stopService(i);
                if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                    return;
                } else {
                    if (Constants.isSongPlaying) {
                        return;
                    } else {
                        Controls.dismissControl(context);
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_PREVIOUS)) {
                if (Constants.isSongComplete) {
                    return;
                } else {
                    if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                        return;
                    } else {
                        if (Constants.isSongPlaying) {
                            return;
                        } else {
                            if (Utility.getUserInfo(context).subscribePlan.plantype.equalsIgnoreCase("Paid") || UserModel.getInstance().isForRenew || UserModel.getInstance().isForTrial) {
                                if (Constants.isSongPlay) {
                                    Controls.previousControl(context);
                                } else {
                                    //It's for change station from notification and lock screen
//                                    Controls.previousStation(context);
                                    return;
                                }
                            } else {
                                if (Constants.isSongPlay) {
                                    UserModel.getInstance().previousPlayMethod(context);
                                } else {
                                    //It's for change station from notification and lock screen
//                                    Controls.previousStation(context);
//                                    UserModel.getInstance().stationPlayForFreeUser(context,false);
                                    return;
                                }
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_LiKE)) {
                if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                    return;
                } else {
                    if (Constants.isSongPlaying) {
                        return;
                    } else {
                        if (Constants.isSongPlay) {
                            if (Constants.SONGS_LIST.size() > 0) {
                                UserModel.getInstance().likeapi(context, new LikeAndUnlikeCallBack() {
                                    @Override
                                    public void statusLikeUnlike(int i) {
                                        UserModel.getInstance().songStatus = i;
                                    }
                                });
                            }
                        } else {
                            if (Constants.StationList.size() > 0) {
                                UserModel.getInstance().likeStationApi(context, new LikeAndUnlikeCallBack() {
                                    @Override
                                    public void statusLikeUnlike(int i) {
                                        UserModel.getInstance().songStatus = i;
                                    }
                                });
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(SongPlayService.NOTIFY_DISLIKE)) {
                if (Constants.adShow || Utility.getBooleaPreferences(context,"ad_playing")) {
                    return;
                } else {
                    if (Constants.isSongPlaying) {
                        return;
                    } else {
                        if (Constants.isSongPlay) {
                            if (Constants.SONGS_LIST.size() > 0) {
                                UserModel.getInstance().unlikeApi(context, new LikeAndUnlikeCallBack() {
                                    @Override
                                    public void statusLikeUnlike(int i) {
                                        UserModel.getInstance().songStatus = i;
                                    }
                                });
                            }
                        } else {
                            if (Constants.StationList.size() > 0) {
                                UserModel.getInstance().unlikeStationApi(context, new LikeAndUnlikeCallBack() {
                                    @Override
                                    public void statusLikeUnlike(int i) {
                                        UserModel.getInstance().songStatus = i;
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }
}
