package angelo.itl.arduinoairqualitymonitor.adapter.main;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import angelo.itl.arduinoairqualitymonitor.*;

import java.util.List;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {
        Context context;
        List<DrawerItem> drawerItemList;
        int layoutResID;

        public CustomDrawerAdapter(Context context, int layoutResourceID, List<DrawerItem> listItems) {
            super(context, layoutResourceID, listItems);
            this.context = context;
            this.drawerItemList = listItems;
            this.layoutResID = layoutResourceID;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DrawerItemHolder drawerHolder;
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                drawerHolder = new DrawerItemHolder();

                view = inflater.inflate(layoutResID, parent, false);
                drawerHolder.title = (TextView) view
                        .findViewById(R.id.title_nav_drawer);
                drawerHolder.ItemName = (TextView) view
                        .findViewById(R.id.option_nav_drawer);
                drawerHolder.itemLayout = (LinearLayout) view
                        .findViewById(R.id.item_layout);
                drawerHolder.titleLayout = (LinearLayout) view
                        .findViewById(R.id.header_layout);
                drawerHolder.icon = (ImageView) view
                        .findViewById(R.id.icon);

                view.setTag(drawerHolder);

            } else {
                drawerHolder = (DrawerItemHolder) view.getTag();

            }

            DrawerItem dItem = this.drawerItemList.get(position);


            if (dItem.getTitle() != null) {
                drawerHolder.titleLayout.setVisibility(LinearLayout.VISIBLE);
                drawerHolder.itemLayout.setVisibility(LinearLayout.INVISIBLE);
                drawerHolder.title.setText(dItem.getTitle());

            } else {
                drawerHolder.titleLayout.setVisibility(LinearLayout.INVISIBLE);
                drawerHolder.icon.setVisibility(LinearLayout.VISIBLE);
                drawerHolder.icon.setImageResource(dItem.getImgResID());
                drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);
                drawerHolder.ItemName.setText(dItem.getItemName());
            }
            return view;
        }

        class DrawerItemHolder {
            ImageView icon;
            TextView ItemName;
            TextView title;
            LinearLayout itemLayout;
            LinearLayout titleLayout;
        }
    }