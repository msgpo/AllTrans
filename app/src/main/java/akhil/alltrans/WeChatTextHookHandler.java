package akhil.alltrans;

import android.text.TextUtils;

/**
 * @author weishu
 * @date 2018/11/13.
 */
public class WeChatTextHookHandler extends SetTextHookHandler {

    @Override
    public void callOriginalMethod(CharSequence translatedString, Object userData) {
        MethodHookParam param = (MethodHookParam) userData;
        CharSequence origString = (CharSequence) param.args[0];
        if (TextUtils.equals(translatedString, origString)) {
            super.callOriginalMethod(translatedString, userData);
            return;
        }
        StringBuilder result = new StringBuilder(origString + "\n");
        result.append("---\n");
//        int length = result.length();
//        for (int i = 0; i < length; i++) {
//            result.append("-");
//        }
        result.append(translatedString);
        super.callOriginalMethod(result, userData);
    }
}
