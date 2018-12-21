package com.arasoftware.call_recorder_demo.listeners;

public interface ListViewClickListener<T> {
    void onItemClick(T selectedObject, int position);
}
