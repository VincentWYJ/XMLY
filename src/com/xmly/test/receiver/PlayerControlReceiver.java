package com.xmly.test.receiver;

import com.xmly.test.constants.Constants;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlayerControlReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		XmPlayerManager manager = XmPlayerManager.getInstance(context);
		String action = intent.getAction();
		if (Constants.ACTION_CONTROL_PLAY_PAUSE.equals(action))
		{
			if (manager.isPlaying())
			{
				manager.pause();
			}
			else
			{
				manager.play();
			}
		}
		else if (Constants.ACTION_CONTROL_PLAY_NEXT.equals(action))
		{
			manager.playNext();
		}
		else if (Constants.ACTION_CONTROL_PLAY_PRE.equals(action))
		{
			manager.playPre();
		}
	}

}

