package com.baidu.share.core;

import android.view.KeyEvent;
import android.view.View;

/**
 * MenuKeyListener
 *
 * @author linjunwu
 * @since 2016/2/2
 */
class MenuKeyListener implements View.OnKeyListener {
    private  ShareMenu mShareMenu;

    public MenuKeyListener(ShareMenu shareMenu) {
        this.mShareMenu = shareMenu;
    }

    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0 && keyCode == 4 && this.mShareMenu.isShowing()) {
            this.mShareMenu.dismiss();
            return true;
        } else {
            return false;
        }
    }
}