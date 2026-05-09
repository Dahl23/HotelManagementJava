package com.hotel.gestion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotel.gestion.R;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Chambre;
import com.hotel.gestion.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChambreAdapter extends RecyclerView.Adapter<ChambreAdapter.ChambreViewHolder> {
    private final List<Chambre> chambres = new ArrayList<>();
    private final OnChambreActionListener listener;

    public ChambreAdapter(OnChambreActionListener listener) {
        this.listener = listener;
    }

    public void setChambres(List<Chambre> newChambres) {
        chambres.clear();
        chambres.addAll(newChambres);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChambreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chambre, parent, false);
        return new ChambreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChambreViewHolder holder, int position) {
        Chambre chambre = chambres.get(position);
        holder.number.setText("Chambre " + chambre.getNumero());
        holder.status.setText(chambre.getStatut());
        holder.type.setText(chambre.getType());
        holder.capacity.setText(String.format(Locale.getDefault(), "%d pers.", chambre.getCapacite()));
        holder.equipment.setText(chambre.getEquipements());
        holder.price.setText(MoneyUtils.formatMoneyPerNight(chambre.getPrixNuit()));

        int background;
        int textColor;
        switch (chambre.getStatut()) {
            case DatabaseHelper.FILTER_OCCUPIED:
                background = R.drawable.bg_badge_orange;
                textColor = R.color.badge_orange_text;
                break;
            case DatabaseHelper.FILTER_MAINTENANCE:
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
        holder.edit.setOnClickListener(v -> listener.onEditChambre(chambre));
        holder.delete.setOnClickListener(v -> listener.onDeleteChambre(chambre));
    }

    @Override
    public int getItemCount() {
        return chambres.size();
    }

    static class ChambreViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView status;
        final TextView type;
        final TextView capacity;
        final TextView equipment;
        final TextView price;
        final TextView edit;
        final TextView delete;

        ChambreViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.textRoomNumber);
            status = itemView.findViewById(R.id.textRoomStatus);
            type = itemView.findViewById(R.id.textRoomType);
            capacity = itemView.findViewById(R.id.textRoomCapacity);
            equipment = itemView.findViewById(R.id.textRoomEquipment);
            price = itemView.findViewById(R.id.textRoomPrice);
            edit = itemView.findViewById(R.id.buttonEditRoom);
            delete = itemView.findViewById(R.id.buttonDeleteRoom);
        }
    }

    public interface OnChambreActionListener {
        void onEditChambre(Chambre chambre);

        void onDeleteChambre(Chambre chambre);
    }
}
