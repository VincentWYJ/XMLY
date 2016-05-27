package com.xmly.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xmly.test.constants.Constants;
import com.xmly.test.fragment.Fragment0_ZongBang;
import com.xmly.test.fragment.Fragment10_QingGanShengHuo;
import com.xmly.test.fragment.Fragment12_XiangShengPingShu;
import com.xmly.test.fragment.Fragment13_JiaoYuPeiXun;
import com.xmly.test.fragment.Fragment14_BaiJiaJiangTan;
import com.xmly.test.fragment.Fragment15_GuangBoJu;
import com.xmly.test.fragment.Fragment16_XiQu;
import com.xmly.test.fragment.Fragment17_DianTai;
import com.xmly.test.fragment.Fragment18_ITKeJi;
import com.xmly.test.fragment.Fragment4_YuLe;
import com.xmly.test.fragment.Fragment3_YouShengShu;
import com.xmly.test.fragment.Fragment1_ZiXun;
import com.xmly.test.fragment.Fragment20_XiaoYuan;
import com.xmly.test.fragment.Fragment21_QiChe;
import com.xmly.test.fragment.Fragment22_LvYou;
import com.xmly.test.fragment.Fragment23_DianYing;
import com.xmly.test.fragment.Fragment24_DongManYouXi;
import com.xmly.test.fragment.Fragment2_YinYue;
import com.xmly.test.fragment.Fragment31_JiaoYuPeiXun;
import com.xmly.test.fragment.Fragment5_WaiYu;
import com.xmly.test.fragment.Fragment6_ErTong;
import com.xmly.test.fragment.Fragment7_JianKangYangSheng;
import com.xmly.test.fragment.Fragment8_ShangYeCaiJing;
import com.xmly.test.fragment.Fragment9_LiShiRenWen;
import com.xmly.test.util.ToolUtil;
import com.xmly.test1.R;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ButtonBarLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

public class MainActivity extends FragmentActivity
{
	private static final String TAG = "MusicFragment";
	
	private String mAppSecret = "4d8e605fa7ed546c4bcb33dee1381179";
	
	private static final String[] Titles = new String[] {
			"总榜", "资讯", "音乐", "有声书", "娱乐", "外语"
			, "儿童","健康养生", "商业财经", "历史人文", "情感生活", "相声评书"
			, "教育培训","百家讲坛", "广播剧", "戏曲", "电台", "IT科技"
			, "校园","汽车", "旅游", "电影", "动漫游戏", "时尚生活"};

	private TextView mTextView;
	private ImageView mSoundCover;
	private SeekBar mSeekBar;
	private ProgressBar mProgress;
	private ImageButton mBtnPreSound;
	private ImageButton mBtnPlay;
	private ImageButton mBtnNextSound;
	private TabLayout mTabLayout;
	private Button mBtnMoreClass;
	private ViewPager mViewPager;
	
	private PagerAdapter mViewPagerAdapter;
	
	private Dialog dialogPopup;
	private GridView mGridView;
	private Button mBtnCancel;
	private SimpleAdapter mSimpleAdapter;
	
	private NotificationManager mNotificationManager;
	private int mNotificationId;
	private RemoteViews mRemoteView;
	private Notification mNotification;
	
	private Context mContext;

	private FinalBitmap mFinalBitmap;
	private FinalHttp mFinalHttp;
	private XmPlayerManager mPlayerManager;
	private CommonRequest mXimalaya;

	private boolean mUpdateProgress = true;
	
	private List<BaseFragment> fragmentList;

	public static int indexFragment = 0; 
	
	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main_layout);
		
		ToolUtil.initStatusBarColor( MainActivity.this);

		mContext = MainActivity.this;
		
		mFinalHttp = new FinalHttp();
		
		mXimalaya = CommonRequest.getInstanse();
		mXimalaya.init(mContext, mAppSecret);
		mXimalaya.setDefaultPagesize(50);
		
		mPlayerManager = XmPlayerManager.getInstance(mContext);
		mPlayerManager.init(mNotificationId, null);
		mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
		mPlayerManager.addAdsStatusListener(mAdsListener);
		mPlayerManager.getPlayerStatus();		

		mFinalBitmap = FinalBitmap.create(mContext.getApplicationContext());
		mFinalBitmap.configLoadingImage(R.drawable.ic_launcher);
		mFinalBitmap.configLoadfailImage(R.drawable.ic_launcher);		
		
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = createNotification();
		mNotificationId = (int)System.currentTimeMillis();
		
		fragmentList = new ArrayList<BaseFragment>();
		fragmentList.add(new Fragment0_ZongBang());
		fragmentList.add(new Fragment1_ZiXun());
		fragmentList.add(new Fragment2_YinYue());
		fragmentList.add(new Fragment3_YouShengShu());
		fragmentList.add(new Fragment4_YuLe());
		fragmentList.add(new Fragment5_WaiYu());
		
		fragmentList.add(new Fragment6_ErTong());
		fragmentList.add(new Fragment7_JianKangYangSheng());
		fragmentList.add(new Fragment8_ShangYeCaiJing());
		fragmentList.add(new Fragment9_LiShiRenWen());
		fragmentList.add(new Fragment10_QingGanShengHuo());
		fragmentList.add(new Fragment12_XiangShengPingShu());
		
		fragmentList.add(new Fragment13_JiaoYuPeiXun());
		fragmentList.add(new Fragment14_BaiJiaJiangTan());
		fragmentList.add(new Fragment15_GuangBoJu());
		fragmentList.add(new Fragment16_XiQu());
		fragmentList.add(new Fragment17_DianTai());
		fragmentList.add(new Fragment18_ITKeJi());
		
		fragmentList.add(new Fragment20_XiaoYuan());
		fragmentList.add(new Fragment21_QiChe());
		fragmentList.add(new Fragment22_LvYou());
		fragmentList.add(new Fragment23_DianYing());
		fragmentList.add(new Fragment24_DongManYouXi());
		fragmentList.add(new Fragment31_JiaoYuPeiXun());

		mTextView = (TextView) findViewById(R.id.message);
		mSoundCover = (ImageView) findViewById(R.id.sound_cover);
		mSeekBar = (SeekBar) findViewById(R.id.seek_bar);  //继承自ProgressBar, 默认max value为100
		mProgress = (ProgressBar) findViewById(R.id.buffering_progress);
		mBtnPreSound = (ImageButton) findViewById(R.id.pre_sound);
		mBtnPlay = (ImageButton) findViewById(R.id.play_or_pause);
		mBtnNextSound = (ImageButton) findViewById(R.id.next_sound);
		mTabLayout = (TabLayout) findViewById(R.id.table_layout);  //和其他app相比, 默认高度对于小字体来说有点偏高, 如目前调整为40dp效果较好
		mBtnMoreClass = (Button) findViewById(R.id.more_class);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		
		mViewPagerAdapter = new SlidingPagerAdapter(getSupportFragmentManager());
		
		mViewPager.setOffscreenPageLimit(2); //参数默认为1,即当不指定或者指定值小于1时系统会当做1处理,一般同时缓存的有三页; 如置为2则一般同时缓存的页面数为5
		mViewPager.setAdapter(mViewPagerAdapter);

		mTabLayout.setupWithViewPager(mViewPager);
		
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				mPlayerManager.seekToByPercent(seekBar.getProgress() / (float) seekBar.getMax());
				mUpdateProgress = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				mUpdateProgress = false;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
			}
		});

		mBtnPreSound.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mPlayerManager.playPre();
				mXimalaya.setDefaultPagesize(100);
			}
		});

		mBtnPlay.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mPlayerManager.isPlaying())
				{
					mPlayerManager.pause();
				}
				else
				{
					mPlayerManager.play();
				}
			}
		});

		mBtnNextSound.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mPlayerManager.playNext();
			}
		});
		
		mBtnMoreClass.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogPopup = new Dialog(mContext);
    			dialogPopup.setContentView(R.layout.class_dialog_layout);
    			dialogPopup.setCanceledOnTouchOutside(true);
                mGridView = (GridView) dialogPopup.findViewById(R.id.class_gridview);
                mBtnCancel  =(Button) dialogPopup.findViewById(R.id.cancel_button);
                mSimpleAdapter = new SimpleAdapter(MainActivity.this, getData(), R.layout.class_item_layout, 
	        		new String[]{"title"}, new int[]{R.id.class_item_textview});
                mGridView.setAdapter(mSimpleAdapter);
                //mGridView.setSelection(mViewPager.getCurrentItem());
                dialogPopup.show();
                
                mBtnCancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialogPopup.cancel();
					}
				});
                
                mGridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// TODO Auto-generated method stub
						if(mViewPager.getCurrentItem() != position){
							mViewPager.setCurrentItem(position);
						}
						dialogPopup.cancel();
					}
				});
			}
		});
	}

	@Override
	public void onBackPressed()
	{
		Log.i(TAG, "onBackPressed");
		if (mPlayerManager != null)
		{
			mPlayerManager.stop();
			mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
			mPlayerManager.release();
		}
		super.onBackPressed();
	}
	
	private String generateFilePath(String baseDir, String url)
	{
		if (TextUtils.isEmpty(baseDir))
		{
			baseDir = Environment.getExternalStorageDirectory() + "/img_chache/";
		}
		File dir = new File(baseDir);
		if (dir.isFile())
		{
			dir.delete();
		}
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return baseDir + System.currentTimeMillis() + getSubfixByUrl(url);
	}
	
	private String getSubfixByUrl(String url)
	{
		if (TextUtils.isEmpty(url))
		{
			return "";
		}
		if (url.contains("."))
		{
			return url.substring(url.lastIndexOf("."));
		}
		return ".jpg";
	}
	
	class SlidingPagerAdapter extends FragmentPagerAdapter
	{
		public SlidingPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragmentList.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return Titles[position % Titles.length];
		}

		@Override
		public int getCount()
		{
			return Titles.length;
		}
	}
	
	private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener()
	{
		
		@Override
		public void onStartPlayAds(Advertis ad, int position)
		{
			Log.i(TAG, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
			if (ad != null)
			{
				mFinalBitmap.display(mSoundCover, ad.getImageUrl());
			}
		}
		
		@Override
		public void onStartGetAdsInfo()
		{
			Log.i(TAG, "onStartGetAdsInfo");
			mBtnPlay.setEnabled(false);
			mSeekBar.setEnabled(false);
		}
		
		@Override
		public void onGetAdsInfo(AdvertisList ads)
		{
			Log.i(TAG, "onGetAdsInfo " + (ads != null));
		}
		
		@Override
		public void onError(int what, int extra)
		{
			Log.i(TAG, "onError what:" + what + ", extra:" + extra);
		}
		
		@Override
		public void onCompletePlayAds()
		{
			Log.i(TAG, "onCompletePlayAds");
			mBtnPlay.setEnabled(true);
			mSeekBar.setEnabled(true);
			PlayableModel model = mPlayerManager.getCurrSound();
			if (model != null && model instanceof Track)
			{
				mFinalBitmap.display(mSoundCover, ((Track) model).getCoverUrlLarge());
			}
		}
		
		@Override
		public void onAdsStopBuffering()
		{
			Log.i(TAG, "onAdsStopBuffering");
		}
		
		@Override
		public void onAdsStartBuffering()
		{
			Log.i(TAG, "onAdsStartBuffering");
		}
	};

	private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener()
	{

		@Override
		public void onSoundPrepared()
		{
			Log.i(TAG, "onSoundPrepared");
			mSeekBar.setEnabled(true);
			mProgress.setVisibility(View.GONE);
		}

		@Override
		public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel)
		{
			Log.i(TAG, "onSoundSwitch index:");
			PlayableModel model = mPlayerManager.getCurrSound();
			if (model != null)
			{
				String title = null;
				String msg = null;
				String coverUrl = null;
				String coverSmall = null;
				if (model instanceof Track)
				{
					Track info = (Track) model;
					title = info.getTrackTitle();
					msg = info.getAnnouncer() == null ? "" : info.getAnnouncer().getNickname();
					coverUrl = info.getCoverUrlLarge();
					coverSmall = info.getCoverUrlMiddle();
				}
				mTextView.setText(title);  //设置播放栏位信息
				mFinalBitmap.display(mSoundCover, coverUrl);
				if (!TextUtils.isEmpty(coverSmall))
				{
					updateRemoteViewIcon(coverSmall);
				}
				else
				{
					Log.i(TAG, "download img null");
				}
				updateNotification(title, msg, true, true);  //设置notify栏位信息
			}
			updateButtonStatus();
		}

		private void updateNotification(String title, String msg, boolean isPlaying,
				boolean hasNext)
		{
			if (!TextUtils.isEmpty(title))
			{
				mRemoteView.setTextViewText(R.id.txt_notifyMusicName, title);
			}
			if (!TextUtils.isEmpty(msg))
			{
				mRemoteView.setTextViewText(R.id.txt_notifyNickName, msg);
			}
			if (isPlaying)
			{
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_pause);
			}
			else
			{
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_play);
			}
			mNotificationManager.notify(mNotificationId, mNotification);
		}

		private void updateRemoteViewIcon(final String coverUrl)
		{
			mFinalHttp.download(coverUrl, generateFilePath(null, coverUrl), 
				new AjaxCallBack<File>()
				{
	
					@Override
					public void onSuccess(File t)
					{
						Log.i(TAG, "download bitmap success : " + t.getAbsolutePath());
						if (t == null || !t.exists())
						{
							return;
						}
						Bitmap bt = BitmapFactory.decodeFile(t.getAbsolutePath());
						if (bt == null)
						{
							return;
						}
						mRemoteView.setImageViewBitmap(R.id.img_notifyIcon, bt);
						mNotificationManager.notify(mNotificationId, mNotification);
					}
	
					public void onFailure(Throwable t, int errorNo, String strMsg)
					{
						Log.i(TAG, "download bitmap error : " + errorNo + ", " + strMsg + ", " + t.getMessage());
						Log.i(TAG, coverUrl);  //打印出图片链接, 目前notify栏的图片更新失败
						Log.i(TAG, generateFilePath(null, coverUrl));
						mRemoteView.setImageViewResource(R.id.img_notifyIcon, R.drawable.ic_launcher);
						mNotificationManager.notify(mNotificationId, mNotification);
					}
				});
		}
		
		private void updateButtonStatus()
		{
			if (mPlayerManager.hasPreSound())
			{
				mBtnPreSound.setEnabled(true);
			}
			else
			{
				mBtnPreSound.setEnabled(false);
			}
			if (mPlayerManager.hasNextSound())
			{
				mBtnNextSound.setEnabled(true);
			}
			else
			{
				mBtnNextSound.setEnabled(false);
			}
		}

		@Override
		public void onPlayStop()
		{
			Log.i(TAG, "onPlayStop");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			updateNotification(null, null, false, true);
		}

		@Override
		public void onPlayStart()
		{
			Log.i(TAG, "onPlayStart");
			mBtnPlay.setBackgroundResource(R.drawable.music_start_drawable);
			updateNotification(null, null, false, true);
		}

		@Override
		public void onPlayProgress(int currPos, int duration)
		{
			String title = "";
			PlayableModel info = mPlayerManager.getCurrSound();
			if (info != null)
			{
				if (info instanceof Track)
				{
					title = ((Track) info).getTrackTitle();
				}
				else if (info instanceof Schedule)
				{
					title = ((Schedule) info).getRelatedProgram().getProgramName();
				}
				else if (info instanceof Radio)
				{
					title = ((Radio) info).getRadioName();
				}
			}
			mTextView.setText(title + "[" + ToolUtil.formatTime(currPos) + "/" + ToolUtil.formatTime(duration) + "]");
			if (mUpdateProgress && duration != 0)
			{
				mSeekBar.setProgress((int) (100 * currPos / (float) duration));
			}
		}

		@Override
		public void onPlayPause()
		{
			Log.i(TAG, "onPlayPause");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			updateNotification(null, null, true, true);
		}

		@Override
		public void onSoundPlayComplete()
		{
			Log.i(TAG, "onSoundPlayComplete");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
		}

		@Override
		public boolean onError(XmPlayerException exception)
		{
			Log.i(TAG, "onError " + exception.getMessage());
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			return false;
		}

		@Override
		public void onBufferProgress(int position)
		{
			mSeekBar.setSecondaryProgress(position);
		}

		public void onBufferingStart()
		{
			mSeekBar.setEnabled(false);
			mProgress.setVisibility(View.GONE);//mProgress.setVisibility(View.VISIBLE);
		}

		public void onBufferingStop()
		{
			mSeekBar.setEnabled(true);
			mProgress.setVisibility(View.GONE);
		}

	};
	
	private Notification createNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

		Intent main = new Intent(mContext, MainActivity.class);
		PendingIntent mainPi = PendingIntent.getActivity(mContext, 0, main, 0);
		
		Intent play = new Intent(Constants.ACTION_CONTROL_PLAY_PAUSE);
		PendingIntent playPi = PendingIntent.getBroadcast(mContext, 0, play, 0);
		
		Intent pre = new Intent(Constants.ACTION_CONTROL_PLAY_PRE);
		PendingIntent prePi = PendingIntent.getBroadcast(mContext, 0, pre, 0);
		
		Intent next = new Intent(Constants.ACTION_CONTROL_PLAY_NEXT);
		PendingIntent nextPi = PendingIntent.getBroadcast(mContext, 0, next, 0);
		
		mRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.music_notify_layout);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyIcon, mainPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPlayOrPause, playPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyNext, nextPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPre, prePi);
		
		builder.setContent(mRemoteView)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("名称")
				.setContentText("信息")
				.setContentIntent(mainPi);
		return builder.build();
	}
	
	 private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for(int i=0; i<Titles.length; ++i){
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("title", Titles[i]);
	        list.add(map);
        }
        return list;
	 }
}
