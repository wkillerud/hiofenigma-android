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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimerView
{
	private final LinearLayout contentLayout;

	private int textSize;

	private TextView hours;
	private TextView minutes;
	private TextView seconds;
	private TextView hourMinuteSeparator;
	private TextView minuteSecondSeparator;

	private static Context context;
	private int timerViewId;

	boolean isCounting;
	boolean isSounding;
	private CountdownService countdownService;

	public TimerView(Context context, int timerViewId,
			CountdownService serviceBinder)
	{
		textSize = 50;
		TimerView.context = context;
		countdownService = serviceBinder;
		this.timerViewId = timerViewId;

		contentLayout = new LinearLayout(TimerView.context);
		contentLayout.setClickable(true);
		contentLayout.setGravity(android.view.Gravity.CENTER);

		setupLayouts();

		/*
		 * The shortClickListener. Here we start the timer and set the alarm, as
		 * well as stop a sounding alarm.
		 */
		contentLayout.setOnClickListener(new OnClickListener()
        {

            public synchronized void onClick(View v)
            {
                /*
                     * We shut down the service to conserve resources when all
                     * countdowns are finished (even if there was just one). In
                     * order to be able to use the app right away, we need to start
                     * the service on click.
                     */
                Intent startService = new Intent(TimerView.context,
                        CountdownService.class);
                TimerView.context.startService(startService);

                if (isCounting)
                {
                    Toast.makeText(TimerView.context, R.string.how_to_stop,
                            Toast.LENGTH_SHORT).show();
                } else if (isSounding)
                {
                    /*
                          * Here we release the wake lock we acquired further down in
                          * the code, in KitchenCountDownTimer.onFinish(). We also
                          * stop the annoying alarm. Then we reset the UI.
                          */

                    countdownService.stopAlarm(TimerView.this.timerViewId);

                    hours.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    minutes.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    seconds.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    hourMinuteSeparator.setTextColor(TimerView.context.getResources()
                            .getColor(R.color.white));
                    minuteSecondSeparator.setTextColor(TimerView.context
                            .getResources().getColor(R.color.white));
                    isSounding = false;
                } else if (!isCounting && !isSounding)
                {

                    /* Set a new alarm and start counting down! */
                    Integer hours = OpenTimerActivity.getHours();
                    Integer minutes = OpenTimerActivity.getMinutes();
                    Integer seconds = OpenTimerActivity.getSeconds();

                    Long millisInFuture = (long) ((seconds * 1000)
                            + (minutes * 60 * 1000) + (hours * 60 * 60 * 1000));

                    if (millisInFuture < 1000)
                    {
                        Toast.makeText(TimerView.context, R.string.enter_higher,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TimerView.this.hours.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    TimerView.this.minutes.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    TimerView.this.seconds.setTextColor(TimerView.context.getResources().getColor(
                            R.color.white));
                    hourMinuteSeparator.setTextColor(TimerView.context.getResources()
                            .getColor(R.color.white));
                    minuteSecondSeparator.setTextColor(TimerView.context
                            .getResources().getColor(R.color.white));

                    countdownService.startTimer(TimerView.this.timerViewId, millisInFuture);
                    isCounting = true;
                }

            }

        });

		/*
		 * The longClickListener. Here we stop a countdown and alarm while it is
		 * in progress (hasn't sounded yet).
		 */
		contentLayout
				.setOnLongClickListener(new android.view.View.OnLongClickListener()
                {
                    public synchronized boolean onLongClick(View v)
                    {
                        if (isCounting)
                        {
                            /* Reset the UI */
                            resetUI();
                            countdownService.stopTimer(TimerView.this.timerViewId);
                            isCounting = false;
                        }
                        return true;
                    }
                });
	}

	/* Sets up the Timer UI */
	protected void setupLayouts()
	{

		hours = new TextView(contentLayout.getContext());
		minutes = new TextView(contentLayout.getContext());
		seconds = new TextView(contentLayout.getContext());
		hourMinuteSeparator = new TextView(contentLayout.getContext());
		minuteSecondSeparator = new TextView(contentLayout.getContext());

		hours.setTextSize(textSize);
		minutes.setTextSize(textSize);
		seconds.setTextSize(textSize);
		hourMinuteSeparator.setTextSize(textSize);
		minuteSecondSeparator.setTextSize(textSize);

		resetUI();

		contentLayout.addView(hours);
		contentLayout.addView(hourMinuteSeparator);
		contentLayout.addView(minutes);
		contentLayout.addView(minuteSecondSeparator);
		contentLayout.addView(seconds);

		contentLayout
				.setBackgroundResource(R.drawable.status_border_grey_slim);
	}

	protected void resetUI()
	{
		hours.setTextColor(context.getResources().getColor(R.color.white));
		minutes
				.setTextColor(context.getResources().getColor(R.color.white));
		seconds
				.setTextColor(context.getResources().getColor(R.color.white));
		hourMinuteSeparator.setTextColor(context.getResources().getColor(
                R.color.white));
		minuteSecondSeparator.setTextColor(context.getResources().getColor(
                R.color.white));

		hourMinuteSeparator.setText(":");
		minuteSecondSeparator.setText(":");
		hours.setText("00");
		minutes.setText("00");
		seconds.setText("00");
	}

	public void updateTick(long millisUntillFinished)
	{
		int hours = (int) (millisUntillFinished / 3600000);
		int minutes = (int) (millisUntillFinished / 60000) - (hours * 60);
		int seconds = (int) (millisUntillFinished / 1000) - (hours * 60 * 60)
				- (minutes * 60);
		this.hours.setText((hours < 10) ? "0" + hours : "" + hours);
		this.minutes.setText((minutes < 10) ? "0" + minutes : "" + minutes);
		this.seconds.setText((seconds < 10) ? "0" + seconds : "" + seconds);
	}

	/* Used by the UI for object reference */
	public LinearLayout getTimerLayout()
	{
		return contentLayout;
	}

	/*
	 * Used by the UI to remove the timer. Stops the countdown and releases
	 * resources.
	 */
	public void remove()
	{
		if (contentLayout != null)
		{
			contentLayout.removeAllViews();
		}
	}

	@Override
	public String toString()
	{
		return "TimerView";
	}

	public void setSounding()
	{
		isSounding = true;
		isCounting = false;
		hours.setTextColor(context.getResources().getColor(R.color.red));
		minutes.setTextColor(context.getResources().getColor(R.color.red));
		seconds.setTextColor(context.getResources().getColor(R.color.red));
		hourMinuteSeparator.setTextColor(context.getResources().getColor(
                R.color.red));
		minuteSecondSeparator.setTextColor(context.getResources().getColor(
                R.color.red));

		hourMinuteSeparator.setText(":");
		minuteSecondSeparator.setText(":");
		hours.setText("00");
		minutes.setText("00");
		seconds.setText("00");

	}

}
