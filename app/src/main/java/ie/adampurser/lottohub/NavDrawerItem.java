package ie.adampurser.lottohub;

public class NavDrawerItem {
    private String mTitle;
    private int mIconResId;
    private int mIconSelectedResId;
    private boolean mIsSelected = false;
    private boolean mHasDivider;

    public NavDrawerItem(String title, int iconResId, int iconSelectedResId) {
        mTitle = title;
        mIconResId = iconResId;
        mIconSelectedResId = iconSelectedResId;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public int getIconSelectedResId() {
        return mIconSelectedResId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean hasDivider() {
        return mHasDivider;
    }

    public void setHasDivider(boolean hasDivider) {
        mHasDivider = hasDivider;
    }
}
