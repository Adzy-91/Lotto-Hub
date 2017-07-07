package ie.adampurser.lottohub;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NavDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG_DEBUG = "NavDrawerAdapter";

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private NavDrawerItem[] mDrawerItems;
    private RecyclerViewOnItemClickListener mItemClickListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        RelativeLayout container;
        TextView title;
        ImageView icon;
        RelativeLayout divider;

        public ItemViewHolder(View v) {
            super(v);

            cardview = (CardView) v.findViewById(R.id.navDrawerItemCardView);
            container = (RelativeLayout)v.findViewById(R.id.navDrawerItemContainer);
            title = (TextView) v.findViewById(R.id.navDrawerItemTitle);
            icon = (ImageView) v.findViewById(R.id.navDrawerItemIcon);
            divider = (RelativeLayout) v.findViewById(R.id.navDrawerItemDivider);
        }

        public void bindItem(NavDrawerItem item) {
            title.setText(item.getTitle());
            container.setSelected(item.isSelected());

            if(container.isSelected()) {
                icon.setImageResource(item.getIconSelectedResId());
            }
            else {
                icon.setImageResource(item.getIconResId());
            }

            if(item.hasDivider()) {
                divider.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView appVersion;

        public HeaderViewHolder(View v) {
            super(v);

            appVersion = (TextView) v.findViewById(R.id.navHeaderText);

        }

        public void bindHeader() {
            appVersion.setText(appVersion.getContext().getResources()
                    .getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        }
    }

    public NavDrawerAdapter(NavDrawerItem[] drawerItems,
                            RecyclerViewOnItemClickListener itemListener) {

        mDrawerItems = drawerItems;
        mItemClickListener = itemListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.nav_drawer_header, parent, false));
            case VIEW_TYPE_ITEM:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.nav_drawer_item, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.bindHeader();
        }
        else {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.bindItem(mDrawerItems[position]);
            itemViewHolder.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDrawerItems.length;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_HEADER;
            default:
                return VIEW_TYPE_ITEM;
        }
    }
}
