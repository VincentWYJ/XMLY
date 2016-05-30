package com.xmly.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.xmly.test.constants.Constants;
import com.xmly.test.fragment.Fragment_BangDan;
import com.xmly.test.util.ToolUtil;
import com.bumptech.glide.Glide;
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
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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

@SuppressLint("ViewHolder")
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

	private XmPlayerManager mPlayerManager;
	private CommonRequest mXimalaya;

	private boolean mUpdateProgress = true;
	
	private List<BaseFragment> fragmentList;
	
	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main_layout);
		
		ToolUtil.initStatusBarColor( MainActivity.this);

		mContext = MainActivity.this;
		
		mXimalaya = CommonRequest.getInstanse();
		mXimalaya.init(mContext, mAppSecret);
		mXimalaya.setDefaultPagesize(50);
		
		mPlayerManager = XmPlayerManager.getInstance(mContext);
		mPlayerManager.init(mNotificationId, null);
		mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
		mPlayerManager.addAdsStatusListener(mAdsListener);
		mPlayerManager.getPlayerStatus();				
		
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = createNotification();
		mNotificationId = (int)System.currentTimeMillis();
		
		fragmentList = new ArrayList<BaseFragment>();
		for(int i=0; i<24; ++i){
			int position = 0;
			if(i<11){
				position = i;
			}else if(i<18){
				position = i+1;
			}else if(i<23){
				position = i+2;
			}else if(i<24){
				position = i+8;
			}
			fragmentList.add(new Fragment_BangDan(position));
		}

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
		
		mViewPager.setOffscreenPageLimit(24); //参数默认为1,即当不指定或者指定值小于1时系统会当做1处理,一般同时缓存的有三页; 如置为2则一般同时缓存的页面数为5
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
				dialogPopup = new Dialog(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);  //R.style.mydialogstyle
    			dialogPopup.setContentView(R.layout.class_dialog_layout);
                mGridView = (GridView) dialogPopup.findViewById(R.id.class_gridview);
                mBtnCancel  =(Button) dialogPopup.findViewById(R.id.cancel_button);
                mSimpleAdapter = new SimpleAdapter(MainActivity.this, getData(), R.layout.class_item_layout, 
	        		new String[]{"title"}, new int[]{R.id.class_item_textview}){
                	@Override
                	public View getView(int position, View convertView, ViewGroup parent) {
                		View view;  
                		if (convertView == null) {  
                			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                			view = mInflater.inflate(R.layout.class_item_layout, parent, false);
	        			} else {  
	        				view = convertView;  
	        			}
						TextView mTextView = (TextView) view.findViewById(R.id.class_item_textview);
						mTextView.setText(Titles[position]);
						if(position == mViewPager.getCurrentItem()){
							mTextView.setBackgroundResource(R.color.class_item_pressed_bg_color);
						}else{
							mTextView.setBackgroundResource(R.color.class_item_normal_bg_color);
						}
						return view;
                	}
                };
                mGridView.setAdapter(mSimpleAdapter);
                dialogPopup.setCanceledOnTouchOutside(true);
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
							mSimpleAdapter.notifyDataSetChanged();  //更新操作作用于方法getView(position, view, parent)
							new Thread(new Runnable(){

							     @Override
							     public void run() {
							          // TODO Auto-generated method stub
							          try {
							               Thread.sleep(10);
							               dialogPopup.cancel();
							          } catch (InterruptedException e) {
							               // TODO Auto-generated catch block
							               e.printStackTrace();
							          }
							     }				
							}).start();
						}else{
							dialogPopup.cancel();
						}
					}
				});
			}
		});
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "MainActivity onDestroy");
		if (mPlayerManager != null)
		{
			mPlayerManager.stop();
			mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
			mPlayerManager.removeAdsStatusListener(mAdsListener);
			mPlayerManager.release();
		}
		if(mNotificationManager != null){
			mNotificationManager.cancelAll();
		}
	}

	public void RefreshData(View view){
		Log.i(TAG, "refresh data");
		for(int i=0;i<24;++i){
			fragmentList.get(i).refresh();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		Log.i(TAG, "onBackPressed");
		moveTaskToBack(true);
		//super.onBackPressed();
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
				Glide.with(mContext).load(ad.getImageUrl()).into(mSoundCover);
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
				Glide.with(mContext).load(((Track) model).getCoverUrlLarge()).into(mSoundCover);
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
				if (model instanceof Track)
				{
					Track info = (Track) model;
					title = info.getTrackTitle();
					msg = info.getAnnouncer() == null ? "" : info.getAnnouncer().getNickname();
					coverUrl = info.getCoverUrlLarge();
				}
				mTextView.setText(title);  //设置播放栏位信息
				Glide.with(mContext).load(coverUrl).into(mSoundCover);
				if (!TextUtils.isEmpty(coverUrl))
				{
					updateRemoteViewIcon(coverUrl);
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
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 try {
						Bitmap bt = Glide.with(mContext)  
						    .load(coverUrl)  
						    .asBitmap()
						    .centerCrop()  
						    .into(500, 500)  
						    .get();
						Log.i(TAG, "**********************");
						mRemoteView.setImageViewBitmap(R.id.img_notifyIcon, bt);
						mNotificationManager.notify(mNotificationId, mNotification);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
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
