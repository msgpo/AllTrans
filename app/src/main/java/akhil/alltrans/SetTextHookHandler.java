/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;


public class SetTextHookHandler extends XC_MethodReplacement implements OriginalCallable {


    public static boolean isNotWhiteSpace(String abc) {
        return !(abc == null || "".equals(abc)) && !abc.matches("^\\s*$");
    }

    public void callOriginalMethod(CharSequence translatedString, Object userData) {

        MethodHookParam methodHookParam = (MethodHookParam) userData;
        Method myMethod = (Method) methodHookParam.method;
        myMethod.setAccessible(true);
        Object[] myArgs = methodHookParam.args;

        if (myMethod.getName().equals("setText")) {
            //if((myMethod.getName()=="setText")) {
            if (myArgs[0].getClass().equals(AlteredCharSequence.class)) {
                myArgs[0] = AlteredCharSequence.make(translatedString, null, 0, 0);
            } else if (myArgs[0].getClass().equals(CharBuffer.class)) {
                CharBuffer charBuffer = CharBuffer.allocate(translatedString.length() + 1);
                charBuffer.append(translatedString);
                myArgs[0] = charBuffer;
            } else if (myArgs[0].getClass().equals(SpannableString.class)) {
                myArgs[0] = new SpannableString(translatedString);
            } else if (myArgs[0].getClass().equals(SpannedString.class)) {
                myArgs[0] = new SpannedString(translatedString);
            } else if (myArgs[0].getClass().equals(String.class)) {
                myArgs[0] = translatedString.toString();
            } else if (myArgs[0].getClass().equals(StringBuffer.class)) {
                myArgs[0] = new StringBuffer(translatedString);
            } else if (myArgs[0].getClass().equals(StringBuilder.class)) {
                myArgs[0] = new StringBuilder(translatedString);
            } else {
                myArgs[0] = new SpannableStringBuilder(translatedString);
            }
        } else {
            myArgs[0] = TextUtils.stringOrSpannedString(translatedString);
        }

        try {
            XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, myArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        if (methodHookParam.args[0] != null) {
            CharSequence arg1 = (CharSequence) methodHookParam.args[0];
            if (TextUtils.isEmpty(arg1)) {// TODO 空白字符判断
                // 如果是null，不用说，直接调原来的
                callOriginalMethod(arg1, methodHookParam);
                return null;
            }

            String stringArgs = methodHookParam.args[0].toString();

            // utils.debugLog("In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);

            if (PreferenceList.Caching) {
                String translatedString = alltrans.cache.get(stringArgs);
                if (translatedString != null) {
                    // 缓存命中！
                    // XposedBridge.log("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    callOriginalMethod(translatedString, methodHookParam);
                    return null;
                }
                // utils.debugLog("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
            }

            // 没得缓存，只有去翻译了。

            GetTranslate getTranslate = new GetTranslate();
            getTranslate.stringToBeTrans = stringArgs;
            getTranslate.originalCallable = this;
            getTranslate.userData = methodHookParam;
            getTranslate.canCallOriginal = true;
            GetTranslateToken getTranslateToken = new GetTranslateToken();
            getTranslateToken.getTranslate = getTranslate;

            callOriginalMethod(stringArgs, methodHookParam);

            getTranslateToken.doAll();

        }
        return null;
    }

}
