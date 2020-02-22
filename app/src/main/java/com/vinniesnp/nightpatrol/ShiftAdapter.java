package com.vinniesnp.nightpatrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vinniesnp.nightpatrol.api.model.Shift;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ViewHolder> {

    private OnItemClickListener mListener;

    private List<Shift> shiftdata;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {mListener = listener;}

    // RecyclerView recyclerView;
    public ShiftAdapter(List<Shift> shiftdata) {
        this.shiftdata = shiftdata;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_shifts, parent, false);

        ViewHolder viewHolder = new ViewHolder(listItem,mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Shift shift = shiftdata.get(position);

        holder.bind(shift);

        Date date = new java.util.Date((long) shift.getStartTime());
        // the format of your date
        SimpleDateFormat day = new java.text.SimpleDateFormat("EEEE d MMM");
        SimpleDateFormat time = new java.text.SimpleDateFormat("HH:mm");
        // give a timezone reference for formatting (see comment at the bottom)
        time.setTimeZone(java.util.TimeZone.getTimeZone("GMT+11"));
        String formattedDate = day.format(date);

        holder.dateView.setText(formattedDate);
        holder.vanView.setText(shift.getVehicle());
        holder.timeView.setText(time.format(date));
       // holder.shiftIDView.setText(shift.getId());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = shift.isExpanded();
                shift.setExpanded(!expanded);
                notifyItemChanged(position);

            }
        });
    }

    @Override
    public int getItemCount() {
        return shiftdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateView;
        public TextView vanView;
        public TextView timeView;
       // public TextView shiftIDView;
        public LinearLayout linearLayout;
        private View subItem;
        public Button deleteButton;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.sub_item_genre);
            this.dateView = itemView.findViewById(R.id.dateText);
            this.vanView = itemView.findViewById(R.id.vanText);
            this.timeView = itemView.findViewById(R.id.timeText);
           // this.shiftIDView = itemView.findViewById(R.id.shiftIDText);
            subItem = itemView.findViewById(R.id.sub_item);

            linearLayout = itemView.findViewById(R.id.relativeLayout);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {


                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }

        private void bind(Shift shift) {
            boolean expanded = shift.isExpanded();

            subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

        }

    }
}
