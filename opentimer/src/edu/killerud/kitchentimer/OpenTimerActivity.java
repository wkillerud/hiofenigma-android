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
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
	private LinearLayout contentLayout;
	private LinearLayout timePicker;
	private static NumberPicker hoursPicker;
	private static NumberPicker minutesPicker;
	private static NumberPicker secondsPicker;
	private CountdownService countdownService;
    private ArrayList<TimerView> timerViews;
    protected ServiceConnection countdownServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			countdownService = ((CountdownService.ServiceBinder) service)
					.getService();
			countdownService.announceServiceState();

			if (countdownService.announceServiceState() != timerViews.size())
			{
				for (int i = 0; i < timerViews.size(); i++)
				{
					timerViews.get(i).remove();
				}
				for (int i = 0; i < countdownService.announceServiceState(); i++)
				{
					addTimerView();
				}
			}

			contentLayout.removeView(findViewById(R.id.tvLoading));
		}

		public void onServiceDisconnected(ComponentName className)
		{
			countdownService = null;
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent bindIntent = new Intent(this, CountdownService.class);
		bindService(bindIntent, countdownServiceConnection, Context.BIND_IMPORTANT);

		setContentView(R.layout.main);
		contentLayout = (LinearLayout) findViewById(R.id.llContentLayout);
		timePicker = (LinearLayout) findViewById(R.id.llTimePicker);

        setupTimePickers();
        restoreSavedTimeIfAny();

        timerViews = new ArrayList<TimerView>();
        setupAddRemoveButtons();
	}

    private void setupAddRemoveButtons()
    {
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

    private void restoreSavedTimeIfAny()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        hoursPicker.setCurrent(preferences.getInt("HOURS", 0));
        minutesPicker.setCurrent(preferences.getInt("MINUTES", 0));
        secondsPicker.setCurrent(preferences.getInt("SECONDS", 0));
    }

    @Override
	public void onResume()
	{
		super.onResume();
		registerBroadcastReceiver();

		Intent bindIntent = new Intent(this, CountdownService.class);
		bindService(bindIntent, countdownServiceConnection, Context.BIND_IMPORTANT);

		Intent startService = new Intent(this, CountdownService.class);
		startService(startService);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		unbindService(countdownServiceConnection);
		shutDownServiceIfNotUsed();
	}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("HOURS", hoursPicker.getCurrent());
        outState.putInt("MINUTES", minutesPicker.getCurrent());
        outState.putInt("SECONDS", secondsPicker.getCurrent());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("HOURS", hoursPicker.getCurrent());
        editor.putInt("MINUTES", minutesPicker.getCurrent());
        editor.putInt("SECONDS", secondsPicker.getCurrent());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.containsKey("HOURS"))
        {
            hoursPicker.setCurrent(savedInstanceState.getInt("HOURS"));
        }
        if(savedInstanceState.containsKey("MINUTES"))
        {
            minutesPicker.setCurrent(savedInstanceState.getInt("MINUTES"));
        }
        if(savedInstanceState.containsKey("SECONDS"))
        {
            secondsPicker.setCurrent(savedInstanceState.getInt("SECONDS"));
        }
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
			for (int i = 0; i < countdownService.announceServiceState(); i++)
			{
				countdownService.stopTimer(i);
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
		countdownService.addTimer();
	}

	protected void addTimerView()
	{
		timerViews.add(new TimerView(getApplicationContext(), timerViews
                .size(), countdownService));
		contentLayout.addView(timerViews.get(timerViews.size() - 1)
                .getTimerLayout());
	}

	protected void removeTimer()
	{
		if (countdownService.announceServiceState() > 0)
		{
			countdownService.removeTimer();
		}
	}

	protected void removeTimerView()
	{
		if (timerViews.size() > 0)
		{
			contentLayout.removeView(timerViews.get(timerViews.size() - 1)
                    .getTimerLayout());
			timerViews.remove(timerViews.size() - 1);
		}
	}

	/* Sets up the TimePicker widgets */
	protected void setupTimePickers()
	{
		hoursPicker = new NumberPicker(getApplicationContext());
		minutesPicker = new NumberPicker(getApplicationContext());
		secondsPicker = new NumberPicker(getApplicationContext());

		hoursPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		minutesPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		secondsPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		hoursPicker.setCurrent(0);
		minutesPicker.setCurrent(1);
		secondsPicker.setCurrent(0);

		hoursPicker.setRange(0, 24);
		minutesPicker.setRange(0, 59);
		secondsPicker.setRange(0, 59);

		timePicker.addView(hoursPicker);
		timePicker.addView(minutesPicker);
		timePicker.addView(secondsPicker);
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
				timerViews.get(intent.getIntExtra("TIMER_ID", -1)).updateTick(
						intent.getLongExtra("TIME_LEFT", 0l));
			} else if (intent.getAction().equals("TIMER_REMOVED"))
			{
				removeTimerView();
			} else if (intent.getAction().equals("TIMER_STOPPED"))
			{
				timerViews.get(intent.getIntExtra("TIMER_ID", -1)).resetUI();

			} else if (intent.getAction().equals("ALARM_SOUNDING"))
			{
				timerViews.get(intent.getIntExtra("TIMER_ID", -1))
						.setSounding();
			} else if (intent.getAction().equals("TIMER_ALARM_STOPPED"))

			{
				timerViews.get(intent.getIntExtra("TIMER_ID", -1)).resetUI();
			} else if (intent.getAction().equals("TIMER_ADDED"))
			{
				addTimerView();
			}

		}

	};

	public static int getHours()
	{
		return hoursPicker.getCurrent();
	}

	public static int getMinutes()
	{
		return minutesPicker.getCurrent();
	}

	public static int getSeconds()
	{
		return secondsPicker.getCurrent();
	}

	protected void shutDownServiceIfNotUsed()
	{
		if (countdownService != null && countdownServiceConnection != null)
		{
			if (countdownService.allAreFinished())
			{
				countdownService.stopSelf();
			}
		}
	}

}