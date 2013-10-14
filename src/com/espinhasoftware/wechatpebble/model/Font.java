/* 
Copyright (c) 2013 Tiago Espinha

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.espinhasoftware.wechatpebble.model;

public class Font {

    private String _codepoint;
    private String _hex;

    public Font(String codepoint, String hex) {
        this._hex = hex;
        this._codepoint = codepoint;
    }

    public String getCodepoint() {
        return _codepoint;
    }

    public void setCodepoint(String unicode) {
        this._codepoint = unicode;
    }

    public String getHex() {
        return _hex;
    }

    public void setHex(String hex) {
        this._hex = hex;
    }

}
