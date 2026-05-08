package com.hotel.gestion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotel.gestion.R;
import com.hotel.gestion.models.Service;
import com.hotel.gestion.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    public interface OnAddServiceListener {
        void onAddService(Service service);
    }

    private final List<Service> services = new ArrayList<>();
    private final Map<Long, Integer> quantities = new HashMap<>();
    private final OnAddServiceListener listener;

    public ServiceAdapter(OnAddServiceListener listener) {
        this.listener = listener;
    }

    public void setServices(List<Service> newServices, Map<Long, Integer> newQuantities) {
        services.clear();
        services.addAll(newServices);
        quantities.clear();
        quantities.putAll(newQuantities);
        notifyDataSetChanged();
    }

    public void updateQuantity(long serviceId, int quantity) {
        quantities.put(serviceId, quantity);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        int quantity = quantities.containsKey(service.getId()) ? quantities.get(service.getId()) : 0;

        holder.name.setText(service.getNom());
        holder.description.setText(service.getDescription());
        holder.price.setText(MoneyUtils.formatMoney(service.getPrix()));
        holder.addButton.setOnClickListener(v -> listener.onAddService(service));

        if (quantity > 0) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(holder.itemView.getContext().getString(R.string.service_added_badge, quantity));
        } else {
            holder.badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final TextView price;
        final TextView badge;
        final Button addButton;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textServiceName);
            description = itemView.findViewById(R.id.textServiceDescription);
            price = itemView.findViewById(R.id.textServicePrice);
            badge = itemView.findViewById(R.id.textServiceBadge);
            addButton = itemView.findViewById(R.id.buttonAddService);
        }
    }
}
