/* 
Copyright (c) 2013 Tiago Espinha

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.espinhasoftware.wechatpebble.db;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.espinhasoftware.pebblenotifier.R;

public class DatabaseLoadNotifier {

    private final NotificationCompat.Builder mBuilder;

    private final NotificationManager        mNotificationManager;

    private final Context                    _context;

    public DatabaseLoadNotifier(Context context) {
        mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("WeChatPebble")
                .setContentText(context.getResources().getText(R.string.notif_started_loading)).setAutoCancel(true)
                .setOngoing(true);

        this._context = context;

        mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void changeProgress(int progress, int max) {
        // mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new
        // Intent(), 0));
        mBuilder.setProgress(max, progress, false);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    public void finish(int progress, int max, int timeout) {
        mBuilder.setContentText(_context.getResources().getText(R.string.notif_finished_loading));
        mBuilder.setContentIntent(PendingIntent.getActivity(_context, 0, new Intent(), 0));
        mBuilder.setOngoing(false);

        changeProgress(progress, max);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(5000);
                } catch (InterruptedException e) {
                }
                mNotificationManager.cancel(1);
            }
        }).run();
    }
}
