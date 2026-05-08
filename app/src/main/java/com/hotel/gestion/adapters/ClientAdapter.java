package com.hotel.gestion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotel.gestion.R;
import com.hotel.gestion.models.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {
    private final List<Client> clients = new ArrayList<>();

    public void setClients(List<Client> newClients) {
        clients.clear();
        clients.addAll(newClients);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clients.get(position);
        holder.avatar.setText(client.getInitiales());
        holder.name.setText(client.getNomComplet().trim());
        holder.phone.setText(client.getTelephone());
        holder.passport.setText(client.getNumeroPasseport());
        holder.status.setText(client.getStatut());

        int background;
        int textColor;
        switch (client.getStatut()) {
            case "En séjour":
                background = R.drawable.bg_badge_orange;
                textColor = R.color.badge_orange_text;
                break;
            case "Archivé":
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
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        final TextView avatar;
        final TextView name;
        final TextView phone;
        final TextView passport;
        final TextView status;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.textAvatar);
            name = itemView.findViewById(R.id.textClientName);
            phone = itemView.findViewById(R.id.textClientPhone);
            passport = itemView.findViewById(R.id.textClientPassport);
            status = itemView.findViewById(R.id.textClientStatus);
        }
    }
}
