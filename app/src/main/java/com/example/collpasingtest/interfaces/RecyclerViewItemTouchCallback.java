package com.example.collpasingtest.interfaces;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.collpasingtest.adapters.AddedRouteAdapter;

// for drag and swipe
public class RecyclerViewItemTouchCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;

    public RecyclerViewItemTouchCallback(ItemTouchHelperContract mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN ;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof AddedRouteAdapter.AddedRouteVH) {
                AddedRouteAdapter.AddedRouteVH holder = (AddedRouteAdapter.AddedRouteVH) viewHolder;
                mAdapter.onRowSelected(holder);
            }
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof AddedRouteAdapter.AddedRouteVH) {
            AddedRouteAdapter.AddedRouteVH holder = (AddedRouteAdapter.AddedRouteVH) viewHolder;
            mAdapter.onRowClear(holder);
        }
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPos, int toPos);

        void onRowSelected(AddedRouteAdapter.AddedRouteVH holder);

        void onRowClear(AddedRouteAdapter.AddedRouteVH holder);
    }
}
