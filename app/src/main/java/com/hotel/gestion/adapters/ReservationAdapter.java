package com.hotel.gestion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotel.gestion.R;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Reservation;
import com.hotel.gestion.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    private final List<DatabaseHelper.ReservationSummary> reservations = new ArrayList<>();
    private final OnReservationActionListener listener;

    public ReservationAdapter(OnReservationActionListener listener) {
        this.listener = listener;
    }

    public void setReservations(List<DatabaseHelper.ReservationSummary> newReservations) {
        reservations.clear();
        reservations.addAll(newReservations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        DatabaseHelper.ReservationSummary summary = reservations.get(position);
        Reservation reservation = summary.reservation;

        holder.client.setText(summary.clientName.trim());
        holder.room.setText(summary.roomLabel);
        holder.status.setText(reservation.getStatut());
        holder.dates.setText(reservation.getDateDebut() + " -> " + reservation.getDateFin()
                + " | " + reservation.getNombrePersonnes() + " pers.");
        holder.total.setText(MoneyUtils.formatMoney(reservation.getMontantTotal()));

        int background;
        int textColor;
        switch (reservation.getStatut()) {
            case "annulee":
                background = R.drawable.bg_badge_red;
                textColor = R.color.badge_red_text;
                break;
            case "terminee":
                background = R.drawable.bg_badge_gray;
                textColor = R.color.badge_gray_text;
                break;
            default:
                background = R.drawable.bg_badge_green;
                textColor = R.color.badge_green_text;
                break;
        }
        holder.status.setBackgroundResource(background);
        holder.status.setTextColor(holder.itemView.getContext().getColor(textColor));

        boolean canChange = "confirmee".equals(reservation.getStatut());
        holder.services.setEnabled(canChange);
        holder.cancel.setEnabled(canChange);
        holder.services.setAlpha(canChange ? 1f : 0.45f);
        holder.cancel.setAlpha(canChange ? 1f : 0.45f);

        holder.services.setOnClickListener(v -> {
            if (canChange) {
                listener.onServices(reservation);
            }
        });
        holder.invoice.setOnClickListener(v -> listener.onInvoice(reservation));
        holder.cancel.setOnClickListener(v -> {
            if (canChange) {
                listener.onCancel(reservation);
            }
        });
        holder.delete.setOnClickListener(v -> listener.onDelete(reservation));
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        final TextView client;
        final TextView room;
        final TextView status;
        final TextView dates;
        final TextView total;
        final TextView services;
        final TextView invoice;
        final TextView cancel;
        final TextView delete;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            client = itemView.findViewById(R.id.textReservationClient);
            room = itemView.findViewById(R.id.textReservationRoom);
            status = itemView.findViewById(R.id.textReservationStatus);
            dates = itemView.findViewById(R.id.textReservationDates);
            total = itemView.findViewById(R.id.textReservationTotal);
            services = itemView.findViewById(R.id.buttonReservationServices);
            invoice = itemView.findViewById(R.id.buttonReservationInvoice);
            cancel = itemView.findViewById(R.id.buttonCancelReservation);
            delete = itemView.findViewById(R.id.buttonDeleteReservation);
        }
    }

    public interface OnReservationActionListener {
        void onServices(Reservation reservation);

        void onInvoice(Reservation reservation);

        void onCancel(Reservation reservation);

        void onDelete(Reservation reservation);
    }
}
