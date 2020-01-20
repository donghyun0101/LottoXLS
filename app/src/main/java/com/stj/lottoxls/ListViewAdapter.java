package com.stj.lottoxls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter
{
    private ArrayList<ListViewItem> mItems = new ArrayList<>();

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public ListViewItem getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        Context context = parent.getContext();

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listviewitem_lotto, parent, false);
        }

        /*
        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();

        layoutParams.height = 100;

        convertView.setLayoutParams(layoutParams);
*/
        TextView tvGetContents = convertView.findViewById(R.id.item_tv_contents);
        TextView tvGetNum1 = convertView.findViewById(R.id.item_tv_num1);
        TextView tvGetNum2 = convertView.findViewById(R.id.item_tv_num2);
        TextView tvGetNum3 = convertView.findViewById(R.id.item_tv_num3);
        TextView tvGetNum4 = convertView.findViewById(R.id.item_tv_num4);
        TextView tvGetNum5 = convertView.findViewById(R.id.item_tv_num5);
        TextView tvGetNum6 = convertView.findViewById(R.id.item_tv_num6);

        ListViewItem ListViewItem = getItem(position);

        tvGetContents.setText(ListViewItem.getContents());
        tvGetNum1.setText(ListViewItem.getNum1());
        tvGetNum2.setText(ListViewItem.getNum2());
        tvGetNum3.setText(ListViewItem.getNum3());
        tvGetNum4.setText(ListViewItem.getNum4());
        tvGetNum5.setText(ListViewItem.getNum5());
        tvGetNum6.setText(ListViewItem.getNum6());

        return convertView;
    }

    /* 아이템 데이터 추가를 위한 함수. 자신이 원하는대로 작성 */
    public void addItem(String contents, String num1, String num2, String num3, String num4, String num5, String num6)
    {

        ListViewItem mItem = new ListViewItem();

        mItem.setContents(contents);
        mItem.setNum1(num1);
        mItem.setNum2(num2);
        mItem.setNum3(num3);
        mItem.setNum4(num4);
        mItem.setNum5(num5);
        mItem.setNum6(num6);

        mItems.add(mItem);

    }

    public void clear()
    {
        mItems.clear();
    }
}