package fr.geonature.maps.sample.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.maps.sample.R

/**
 * [RecyclerView.Adapter] that can display a menu entry.
 */
class MenuItemRecyclerViewAdapter(private val listener: OnMenuItemRecyclerViewAdapterListener) :
    RecyclerView.Adapter<MenuItemRecyclerViewAdapter.MenuItemViewHolder>() {

    private val items = mutableListOf<MenuItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * Sets new menu items.
     */
    fun setItems(newItems: List<MenuItem>) {
        if (this.items.isEmpty()) {
            this.items.addAll(newItems)

            if (this.items.isNotEmpty()) {
                notifyItemRangeInserted(
                    0,
                    this.items.size
                )
            } else {
                notifyDataSetChanged()
            }

            return
        }

        if (newItems.isEmpty()) {
            this.items.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return items.size
                }

                override fun getNewListSize(): Int {
                    return newItems.size
                }

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return items[oldItemPosition].label == newItems[newItemPosition].label
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return items[oldItemPosition] == newItems[newItemPosition]
                }
            }
        )

        this.items.clear()
        this.items.addAll(newItems)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class MenuItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_item_1,
                parent,
                false
            )
    ) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(menuItem: MenuItem) {
            text1.text = menuItem.label
            itemView.setOnClickListener { listener.onClick(menuItem) }
        }
    }

    /**
     * Callback used by [MenuItemRecyclerViewAdapter].
     */
    interface OnMenuItemRecyclerViewAdapterListener {

        /**
         * Called when an item has been clicked.
         *
         * @param menuItem the selected item
         */
        fun onClick(menuItem: MenuItem)
    }
}