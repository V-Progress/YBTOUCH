package com.ideafactory.client.business.localnetcall;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ideafactory.client.R;

import java.util.List;


public class CallNumQueueAdapter extends RecyclerView.Adapter<CallNumQueueAdapter.CallNumberQueueList> {

    private List<String> callList;

    public void setCallList(List<String> callList) {
        this.callList = callList;
    }

    @Override
    public CallNumberQueueList onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  View.inflate(parent.getContext(), R.layout.call_queue_layout_item,null);
        return new CallNumberQueueList(view) ;
    }

    @Override
    public void onBindViewHolder(CallNumberQueueList holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }
    class CallNumberQueueList extends RecyclerView.ViewHolder {
        private TextView textView;
        public CallNumberQueueList(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv_call_queue);
        }
        public void setData(final int position) {
            textView.setText(callList.get(position));
        }
    }

    public void receivedAdd(String callNum){
        callList.add(0,callNum);
        notifyItemInserted(0);
    }


}
