/* 
Copyright (c) 2013 Tiago Espinha

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.espinhasoftware.wechatpebble.model;

import java.util.ArrayList;
import java.util.List;

import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;

public class CharacterMatrix {
    private final List<Byte> _byteList;
    private final int        _widthBytes;

    public CharacterMatrix(String hex) {
        // 0.03125 sets the ration between the length of the hex representation
        // (64 or 32 char)
        // versus character width (2 or 1 byte respectively)
        this._widthBytes = (int) (hex.length() * 0.03125);

        // Array is hex, two hex chars => byte, so the size is /2
        this._byteList = new ArrayList<Byte>(hex.length() / 2);

        if (this._widthBytes == 2) {
            while (hex.length() > 0) {
                String temp = hex.substring(0, 4);

                hex = hex.substring(4);

                int value = Integer.parseInt(temp, 16);

                byte[] end = new byte[2];

                // Convert value to two bytes (always fits 0x0 to 0xFFFF)
                end[1] = (byte) (value & 0xFF);
                end[0] = (byte) ((value >>> 8) & 0xFF);

                // Reverse the 1s and 0s to write black on white bg
                end[1] = (byte) (~end[1] & 0xff);
                end[0] = (byte) (~end[0] & 0xff);

                end[1] = (byte) (Integer.reverse(end[1]) >>> 24);
                end[0] = (byte) (Integer.reverse(end[0]) >>> 24);

                this._byteList.add(end[0]);
                this._byteList.add(end[1]);

            }
        } else if (this._widthBytes == 1) {
            while (hex.length() > 0) {
                String temp = hex.substring(0, 2);

                hex = hex.substring(2);

                int value = Integer.parseInt(temp, 16);

                byte end = (byte) (value & 0xFF);

                end = (byte) (~end & 0xff);

                end = (byte) (Integer.reverse(end) >>> 24);

                this._byteList.add(end);
            }
        }
    }

    public List<Byte> getByteList() {
        return _byteList;
    }

    public int getWidthBytes() {
        return _widthBytes;
    }

    public String x_getBinaryRepresentation() {
        Byte[] bytes = new Byte[_byteList.size()];
        getByteList().toArray(bytes);

        return PebbleMessage.toBinary(bytes);
    }
}
