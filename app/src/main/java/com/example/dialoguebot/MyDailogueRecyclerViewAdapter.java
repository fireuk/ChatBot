package com.example.dialoguebot;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dialoguebot.DailogueFragment.OnListFragmentInteractionListener;

import com.example.dialoguebot.DialogueContent.DialogueItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DialogueItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyDailogueRecyclerViewAdapter extends RecyclerView.Adapter<MyDailogueRecyclerViewAdapter.ViewHolder> {

    private final List<DialogueItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final String TAG = "braind_MyDailogueRecyclerViewAdapter";

    public MyDailogueRecyclerViewAdapter(List<DialogueItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_dailogue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Log.d(TAG, "position = "+position);
        String st_TalkText = mValues.get(position).st_TalkContent;
        String st_ReceiveText = mValues.get(position).st_ReceiveContent;
        holder.mTv_Talk.setText(st_TalkText);
        holder.mTv_Receive.setText(st_ReceiveText);
        if (st_TalkText.isEmpty()){
            holder.mTv_Talk.setVisibility(View.GONE);
        }
        else {
            holder.mTv_Talk.setVisibility(View.VISIBLE);
        }
        if (st_ReceiveText.isEmpty()){
            holder.mTv_Receive.setVisibility(View.GONE);
        }
        else {
            holder.mTv_Receive.setVisibility(View.VISIBLE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private int mLastPosition = 0;
    public static final int TYPE_USER_TALK = 1;
    public static final int TYPE_DINGDANG_RECEIVE = 2;
    public int add(String st, int int_Type) {
        DialogueItem  di = null;
        if (int_Type == TYPE_USER_TALK){
            di = DialogueContent.createDialogueItem(st, "");
        }
        else if (int_Type == TYPE_DINGDANG_RECEIVE){
            di = DialogueContent.createDialogueItem("", st);
        }

        if (di != null){
            mValues.add(di);
            mLastPosition = getItemCount()-1;
            notifyItemInserted(mLastPosition);   // 滚动到最新的位置
        }

        return mLastPosition;
    }

    public void update(String st, int postion){
        mValues.get(postion).st_TalkContent = st;
        notifyItemChanged(postion);
    }

    public void remove(int position){
        mValues.remove(position);
        mLastPosition = position - 1;
        notifyItemRemoved(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTv_Talk;
        public final TextView mTv_Receive;
        public DialogueItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTv_Talk = (TextView) view.findViewById(R.id.tv_talk);
            mTv_Receive = (TextView) view.findViewById(R.id.tv_receive);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTv_Talk.getText() + "'";
        }
    }
}
