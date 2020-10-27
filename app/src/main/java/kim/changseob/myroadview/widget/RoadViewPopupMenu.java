package kim.changseob.myroadview.widget;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import kim.changseob.myroadview.MainActivity;

public class RoadViewPopupMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener {

    Context mContext;
    String[] mMenus;

    public RoadViewPopupMenu(Context context, View anchor) {
        super(context, anchor);
        mContext = context;

    }

    public void SetMenu(String[] menus) {
        mMenus = menus;

    }

    @Override
    public void show() {
        this.getMenu().clear();
        for(int i = 0; i < mMenus.length; i++)
            this.getMenu().add(mMenus[i]);
        super.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ((MainActivity)mContext).mHandler.sendEmptyMessage(item.getItemId());
        return false;
    }
}
