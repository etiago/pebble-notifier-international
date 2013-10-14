/* 
Copyright (c) 2013 Tiago Espinha

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.espinhasoftware.wechatpebble.service;

import java.util.ArrayDeque;
import java.util.Deque;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dattasmoon.pebble.plugin.Constants;
import com.espinhasoftware.wechatpebble.db.DatabaseHandler;
import com.espinhasoftware.wechatpebble.model.CharacterMatrix;
import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;

public class MessageProcessingService extends Service {
    public static final String     KEY_ORIGINAL_MSG        = "KEY_ORIGINAL_MSG";
    public static final String     KEY_ORIGINAL_TITLE      = "KEY_ORIGINAL_TITLE";

    public static final String     KEY_RPL_PBL_MSG         = "KEY_RPL_PBL_MSG";
    public static final String     KEY_RPL_STR             = "KEY_RPL_STR";
    public static final String     KEY_RPL_TITLE           = "KEY_RPL_TITLE";

    public static final int        MSG_SEND_ORIGINAL_MSG   = 1;
    public static final int        MSG_REPLY_PROCESSED_MSG = 2;

    public static final int        PROCESS_UNIFONT         = 1;
    // I'm disabling this until I can find a better pinyin library
    // public static final int PROCESS_PINYIN = 2;
    public static final int        PROCESS_NO_PINYIN       = 3;

    private static DatabaseHandler db;

    private static Context         _context;

    /**
     * Handler of incoming messages from PebbleCommService.
     */
    static class HandleWeChatIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MessageProcessingService.MSG_SEND_ORIGINAL_MSG:
                Log.d("MessageProcessing", "Got message");

                String originalMessage = msg.getData().getString(MessageProcessingService.KEY_ORIGINAL_MSG);
                String title = msg.getData().getString(MessageProcessingService.KEY_ORIGINAL_TITLE);

                Messenger replyChannel = msg.replyTo;
                Message reply = new Message();

                reply.what = MessageProcessingService.MSG_REPLY_PROCESSED_MSG;

                if (msg.arg1 == MessageProcessingService.PROCESS_UNIFONT) {
                    PebbleMessage pb = processMessage(title + " # " + originalMessage);

                    Bundle b = new Bundle();
                    b.putSerializable(MessageProcessingService.KEY_RPL_PBL_MSG, pb);

                    reply.setData(b);

                    try {
                        Log.d("MessageProcessing", "Replied to HandleWeChat with Unifont");
                        replyChannel.send(reply);
                    } catch (RemoteException e) {
                        Log.d("MessageProcessing", "Exception replying to HandleWeChat with Unifont");
                    }
                    // } else if (msg.arg1 ==
                    // MessageProcessingService.PROCESS_PINYIN) {
                    // // pass through
                    // Bundle b = new Bundle();
                    // b.putSerializable(MessageProcessingService.KEY_RPL_STR,
                    // processMessageString(originalMessage));
                    //
                    // reply.setData(b);
                    //
                    // try {
                    // Log.d("MessageProcessing",
                    // "Replied to HandleWeChat with PinYin");
                    // replyChannel.send(reply);
                    // } catch (RemoteException e) {
                    // Log.d("MessageProcessing",
                    // "Exception replying to HandleWeChat with PinYin");
                    // }
                } else if (msg.arg1 == MessageProcessingService.PROCESS_NO_PINYIN) {
                    // pass through
                    Bundle b = new Bundle();
                    b.putSerializable(MessageProcessingService.KEY_RPL_TITLE, title);
                    b.putSerializable(MessageProcessingService.KEY_RPL_STR, originalMessage);

                    reply.setData(b);

                    try {
                        Log.d("MessageProcessing", "Replied to HandleWeChat without PinYin");
                        replyChannel.send(reply);
                    } catch (RemoteException e) {
                        Log.d("MessageProcessing", "Exception replying to HandleWeChat without PinYin");
                    }
                }
                break;
            default:
                super.handleMessage(msg);
            }
        }

        private boolean isDatabaseReady() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);

            if (sharedPref.getBoolean(Constants.DATABASE_READY, false)) {
                return true;
            }

            int maxWaitMilis = 15000;
            int waited = 0;

            while (waited < maxWaitMilis) {
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                }

                if (sharedPref.getBoolean(Constants.DATABASE_READY, false)) {
                    return true;
                }
            }

            return false;
        }

        // /**
        // * Sends alerts to the Pebble watch, as per the Pebble app's intents
        // * @param alert Alert which to send to the watch.
        // */
        // private static String processMessageString(String originalMessage) {
        // // This is the traditional Pebble alert which does not show Unicode
        // characters
        // HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        // format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
        // format.setVCharType(HanyuPinyinVCharType.WITH_V);
        //
        // try {
        // // I know this is deprecated but there's no viable alternative...
        // return PinyinHelper.toHanyuPinyinString(originalMessage, format ,
        // "");
        // } catch (BadHanyuPinyinOutputFormatCombination e) {
        // Log.e("Pinyin", "Failed to convert pinyin");
        // }
        //
        // return "";
        // }

        private PebbleMessage processMessage(String originalMessage) {
            // This method not only polls the database but waits for it to be
            // ready
            // in the event it is loading.
            if (!isDatabaseReady()) {
                Log.e("MessagProcessing", "Database not ready after waiting!");
            }

            PebbleMessage message = new PebbleMessage();

            // Clear the characterQueue, just in case
            Deque<CharacterMatrix> characterQueue = new ArrayDeque<CharacterMatrix>();

            while (originalMessage.length() > 0) {
                int codepoint = originalMessage.codePointAt(0);

                String codepointStr = Integer.toHexString(codepoint).toUpperCase();
                codepointStr = ("0000" + codepointStr).substring(codepointStr.length());

                String originalHex = db.getFont(codepointStr).getHex();

                CharacterMatrix c = new CharacterMatrix(originalHex);

                characterQueue.add(c);

                originalMessage = originalMessage.substring(1);
            }

            message.setCharacterQueue(characterQueue);

            return message;
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessengerHandleWeChat = new Messenger(new HandleWeChatIncomingHandler());

    @Override
    public void onCreate() {
        MessageProcessingService._context = getApplicationContext();

        db = new DatabaseHandler(getApplicationContext());

        db.open();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessengerHandleWeChat.getBinder();
    }

}
