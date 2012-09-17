/**
 *   Copyright William Killerud 2012
 *   
 *   This file is part of OpenTimer.
 *
 *   OpenTimer is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   OpenTimer is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenTimer.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   For questions contact William Killerud at william@killerud.com
 * 
 */

package edu.killerud.kitchentimer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.quietlycoding.android.picker.NumberPicker;

/**
 * My take on the February App Challenge at Enigma, HIOF. The app has the option
 * of creating several separate timers, and a timer that reaches zero wakes up
 * the phone (if necessary), shows the user a notification, and sounds the
 * alarm.
 * 
 * Uses touch for starting and stopping the timers, rather than a separate
 * button.
 * 
 * @author William Killerud
 * 
 */
public class OpenTimerActivity extends Activity
{
	private LinearLayout mLlContentLayout;
	private LinearLayout mLlTimePicker;
	private static NumberPicker npHours;
	private static NumberPicker npMinutes;
	private static NumberPicker npSeconds;
	private CountdownService mCDService;
	protected ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mCDService = ((CountdownService.ServiceBinder) service)
					.getService();
			mCDService.announceServiceState();

			if (mCDService.announceServiceState() != mTimerViews.size())
			{
				for (int i = 0; i < mTimerViews.size(); i++)
				{
					mTimerViews.get(i).remove();
				}
				for (int i = 0; i < mCDService.announceServiceState(); i++)
				{
					addTimerView();
				}
			}

			/* What we need has now been loaded, and we can start using the app */
			mLlContentLayout.removeView(findViewById(R.id.tvLoading));
		}

		public void onServiceDisconnected(ComponentName className)
		{
			// Log.i("OpenTimerActivity",
			// "Service disconnected. 99.9% likely that all timers were done!");
			mCDService = null;
		}
	};

	private ArrayList<TimerView> mTimerViews;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		/*
		 * Binds the activity to our service in order to make method calls
		 * directly on the service, as well as properties.
		 */
		Intent bindIntent = new Intent(this, CountdownService.class);
		bindService(bindIntent, mConnection, Context.BIND_IMPORTANT);

		/* Sets up the layout */
		setContentView(R.layout.main);
		mLlContentLayout = (LinearLayout) findViewById(R.id.llContentLayout);
		mLlTimePicker = (LinearLayout) findViewById(R.id.llTimePicker);
		setupTimePickers();
		mTimerViews = new ArrayList<TimerView>();

		/* Sets up the add- and remove timer buttons */
		Button bAddTimer = (Button) findViewById(R.id.bAddTimer);
		Button bRemoveTimer = (Button) findViewById(R.id.bRemoveTimer);
		bAddTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				addTimer();
			}

		});
		bRemoveTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				removeTimer();
			}

		});
	}

	@Override
	public void onResume()
	{
		super.onResume();
		registerBroadcastReceiver();

		Intent bindIntent = new Intent(this, CountdownService.class);
		bindService(bindIntent, mConnection, Context.BIND_IMPORTANT);

		Intent startService = new Intent(this, CountdownService.class);
		startService(startService);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		unbindService(mConnection);
		/*
		 * I don't know about you, but when I navigate out of an app with
		 * nothing happening in it, I'm not comming back!
		 * 
		 * Run the following method to do some magic! <|:D~
		 */
		appShutdownMagic();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.stopAll:
			for (int i = 0; i < mCDService.announceServiceState(); i++)
			{
				mCDService.stopTimer(i);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void registerBroadcastReceiver()
	{
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("TIMER_ADDED");
		ifilter.addAction("TIMER_REMOVED");
		ifilter.addAction("TIMER_STOPPED");
		ifilter.addAction("TIMER_ALARM_STOPPED");
		ifilter.addAction("TIMER_STARTED");
		// ifilter.addAction("NUMBER_OF_TIMERS");
		ifilter.addAction("TIMER_TICK");
		ifilter.addAction("ALARM_SOUNDING");
		registerReceiver(broadcastReceiver, new IntentFilter(ifilter));
	}

	protected void addTimer()
	{
		mCDService.addTimer();
	}

	protected void addTimerView()
	{
		mTimerViews.add(new TimerView(getApplicationContext(), mTimerViews
				.size(), mCDService));
		mLlContentLayout.addView(mTimerViews.get(mTimerViews.size() - 1)
				.getTimerLayout());
	}

	protected void removeTimer()
	{
		if (mCDService.announceServiceState() > 0)
		{
			mCDService.removeTimer();
		}
	}

	protected void removeTimerView()
	{
		if (mTimerViews.size() > 0)
		{
			mLlContentLayout.removeView(mTimerViews.get(mTimerViews.size() - 1)
					.getTimerLayout());
			mTimerViews.remove(mTimerViews.size() - 1);
		}
	}

	/* Sets up the TimePicker widgets */
	protected void setupTimePickers()
	{
		npHours = new NumberPicker(getApplicationContext());
		npMinutes = new NumberPicker(getApplicationContext());
		npSeconds = new NumberPicker(getApplicationContext());

		npHours.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npMinutes.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npSeconds.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		npHours.setCurrent(0);
		npMinutes.setCurrent(1);
		npSeconds.setCurrent(0);

		npHours.setRange(0, 24);
		npMinutes.setRange(0, 59);
		npSeconds.setRange(0, 59);

		mLlTimePicker.addView(npHours);
		mLlTimePicker.addView(npMinutes);
		mLlTimePicker.addView(npSeconds);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent == null)
			{
				return;
			}
			if (intent.getAction().equals("TIMER_TICK"))
			{
				mTimerViews.get(intent.getIntExtra("TIMER_ID", -1)).updateTick(
						intent.getLongExtra("TIME_LEFT", 0l));
			} else if (intent.getAction().equals("TIMER_REMOVED"))
			{
				removeTimerView();
			} else if (intent.getAction().equals("TIMER_STOPPED"))
			{
				mTimerViews.get(intent.getIntExtra("TIMER_ID", -1)).resetUI();

			} else if (intent.getAction().equals("ALARM_SOUNDING"))
			{
				mTimerViews.get(intent.getIntExtra("TIMER_ID", -1))
						.setSounding();
			} else if (intent.getAction().equals("TIMER_ALARM_STOPPED"))

			{
				mTimerViews.get(intent.getIntExtra("TIMER_ID", -1)).resetUI();
			} else if (intent.getAction().equals("TIMER_ADDED"))
			{
				addTimerView();
			}

		}

	};

	public static int getHours()
	{
		return npHours.getCurrent();
	}

	public static int getMinutes()
	{
		return npMinutes.getCurrent();
	}

	public static int getSeconds()
	{
		return npSeconds.getCurrent();
	}

	protected void appShutdownMagic()
	{
		if (mCDService != null && mConnection != null)
		{
			if (mCDService.allAreFinished())
			{
				mCDService.stopSelf();
			}
		}
	}

}