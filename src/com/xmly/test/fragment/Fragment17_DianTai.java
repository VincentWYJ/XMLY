package com.xmly.test.fragment;

import java.util.HashMap;
import java.util.Map;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackHotList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.xmly.test.BaseFragment;

import com.xmly.test.R;
import com.xmly.test.ViewHolder;

public class Fragment17_DianTai extends BaseFragment
{
	private static final String TAG = "MusicFragment";
	private Context mContext;
	private ListView mListView;
	private TrackAdapter mTrackAdapter;

	private int mPageId = 1;
	private TrackHotList mTrackHotList = null;
	private boolean mLoading = false;

	private CommonRequest mXimalaya;
	private XmPlayerManager mPlayerManager;
	

	private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener()
	{

		@Override
		public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel)
		{
			if (mTrackAdapter != null)
			{
				mTrackAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onSoundPrepared()
		{
		}

		@Override
		public void onSoundPlayComplete()
		{
		}

		@Override
		public void onPlayStop()
		{
		}

		@Override
		public void onPlayStart()
		{
		}

		@Override
		public void onPlayProgress(int currPos, int duration)
		{
		}

		@Override
		public void onPlayPause()
		{
		}

		@Override
		public boolean onError(XmPlayerException exception)
		{
			return false;

		}

		@Override
		public void onBufferingStop()
		{
		}

		@Override
		public void onBufferingStart()
		{
		}

		@Override
		public void onBufferProgress(int percent)
		{
		}

	};

	public void refresh()
	{
		Log.e(TAG, "---refresh");
		if (hasMore())
		{
			loadData();
		}
	}

	private boolean hasMore()
	{
		if (mTrackHotList != null && mTrackHotList.getTotalPage() <= mPageId)
		{
			return false;
		}
		return true;
	}

	private void loadData()
	{
		if (mLoading)
		{
			return;
		}
		mLoading = true;
		Map<String, String> param = new HashMap<String, String>();
		param.put(DTransferConstants.CATEGORY_ID, "" + 17);
		//param.put(DTransferConstants.TAG_NAME, null);
		param.put(DTransferConstants.PAGE, "" + mPageId);
		param.put(DTransferConstants.PAGE_SIZE, "" + mXimalaya.getDefaultPagesize());
		CommonRequest.getHotTracks(param, new IDataCallBack<TrackHotList>()
		{

			@Override
			public void onSuccess(TrackHotList object)
			{
				Log.e(TAG, "onSuccess " + (object != null));
				if (object != null && object.getTracks() != null
						&& object.getTracks().size() != 0)
				{
					mPageId++;
					if (mTrackHotList == null)
					{
						mTrackHotList = object;
					}
					else
					{
						mTrackHotList.getTracks().addAll(object.getTracks());
					}
					mTrackAdapter.notifyDataSetChanged();
				}
				mLoading = false;
			}

			@Override
			public void onError(int code, String message)
			{
				Log.e(TAG, "onError " + code + ", " + message);
				mLoading = false;
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.music_list_layout, container, false);
		mListView = (ListView) view.findViewById(R.id.listview);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		Log.i(TAG, "Fragment17_DianTai onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity();

		mXimalaya = CommonRequest.getInstanse();
		mPlayerManager = XmPlayerManager.getInstance(mContext);

		mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);

		
		
		

		mTrackAdapter = new TrackAdapter();
		mListView.setAdapter(mTrackAdapter);

		mListView.setOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				if (scrollState == SCROLL_STATE_IDLE)
				{
					int count = view.getCount();
					count = count - 5 > 0 ? count - 5 : count - 1;
					if (view.getLastVisiblePosition() > count
							&& (mTrackHotList == null || mPageId < mTrackHotList
									.getTotalPage()))
					{
						loadData();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				mPlayerManager.playList(mTrackHotList, position);
			}
		});

		loadData();
	}

	@Override
	public void onDestroyView()
	{
		Log.i(TAG, "Fragment17_DianTai onDestroyView");
		
		//mTrackHotList.getTracks().clear();
		
		//mFinalBitmap.clearMemoryCache();
		
		if (mPlayerManager != null)
		{
			mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
		}
		super.onDestroyView();
	}

	public class TrackAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			if (mTrackHotList == null || mTrackHotList.getTracks() == null)
			{
				return 0;
			}
			return mTrackHotList.getTracks().size();
		}

		@Override
		public Object getItem(int position)
		{
			return mTrackHotList.getTracks().get(position);

		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			if (convertView == null)
			{
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.music_item_layout, parent, false);
				holder = new ViewHolder();
				holder.content = (ViewGroup) convertView;
				holder.image = (ImageView) convertView
						.findViewById(R.id.imageview);
				holder.title = (TextView) convertView
						.findViewById(R.id.trackname);
				holder.intro = (TextView) convertView.findViewById(R.id.intro);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			Track sound = mTrackHotList.getTracks().get(position);
			holder.title.setText(sound.getTrackTitle());
			holder.intro.setText(sound.getAnnouncer() == null ? sound
					.getTrackTags() : sound.getAnnouncer().getNickname());
			Glide.with(mContext).load(sound.getCoverUrlLarge()).into(holder.image);
			PlayableModel curr = mPlayerManager.getCurrSound();
			if (sound.equals(curr))
			{
				holder.content.setBackgroundResource(R.color.list_item_selected_color);
			}
			else
			{
				holder.content.setBackgroundColor(Color.WHITE);
			}
			return convertView;
		}

	}
}
