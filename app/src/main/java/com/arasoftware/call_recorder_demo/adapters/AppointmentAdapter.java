package com.arasoftware.call_recorder_demo.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arasoftware.call_recorder_demo.R;
import com.arasoftware.call_recorder_demo.listeners.ListViewClickListener;
import com.arasoftware.call_recorder_demo.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder> {

    private List<Appointment> appointmentList;
    private ListViewClickListener<Appointment> itemClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView place, time;
        public View mView;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            place = (TextView) view.findViewById(R.id.item_appointment_place_tv);
            time = (TextView) view.findViewById(R.id.item_appointment_time_tv);
        }
    }


    public AppointmentAdapter(List<Appointment> appointmentList,
                              ListViewClickListener<Appointment> clickListener) {
        this.appointmentList = appointmentList;
        this.itemClickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appoitment, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.place.setText(appointment.getPlace());
        holder.time.setText(appointment.getTime());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.onItemClick(appointment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }
}