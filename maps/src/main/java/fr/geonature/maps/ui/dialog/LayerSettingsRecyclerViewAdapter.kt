package fr.geonature.maps.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerType

/**
 * Default [RecyclerView.Adapter] for [LayerSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LayerSettingsRecyclerViewAdapter(private val listener: OnLayerRecyclerViewAdapterListener) :
    RecyclerView.Adapter<LayerSettingsRecyclerViewAdapter.AbstractViewHolder>() {

    private val items = mutableListOf<Pair<LayerSettings, ViewType>>()
    private val selectedItems = mutableListOf<LayerSettings>()
    private val onClickListener: View.OnClickListener

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeChanged(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeRemoved(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(itemCount == 0)
            }
        })

        onClickListener = View.OnClickListener { v ->
            val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)

            val layerSettings = v.tag as LayerSettings
            val isSelected = selectedItems.contains(layerSettings)


            if (isSelected) {
                selectedItems.remove(layerSettings)
                checkbox.isChecked = false
            } else {
                selectedItems.add(layerSettings)
                checkbox.isChecked = true
            }

            listener.onSelectedLayersSettings(selectedItems)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (ViewType.values()[viewType]) {
            ViewType.HEADER -> LayerHeaderViewHolder(parent)
            else -> LayerViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(items[position].first)
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].second.ordinal
    }

    /**
     * Sets layers.
     */
    fun setItems(newItems: List<LayerSettings>) {
        val newItemsWithViewType = mutableListOf<Pair<LayerSettings, ViewType>>()
        newItems.asSequence().sortedBy { it.getType() }.forEach {
            if (it.getType() != newItemsWithViewType.lastOrNull()?.first?.getType()) {
                newItemsWithViewType.add(Pair(it, ViewType.HEADER))
            }

            newItemsWithViewType.add(Pair(it, ViewType.LAYER))
        }

        if (this.items.isEmpty()) {
            this.items.addAll(newItemsWithViewType)

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

        if (newItemsWithViewType.isEmpty()) {
            this.items.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@LayerSettingsRecyclerViewAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@LayerSettingsRecyclerViewAdapter.items[oldItemPosition].first.source == newItemsWithViewType[newItemPosition].first.source
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@LayerSettingsRecyclerViewAdapter.items[oldItemPosition].first == newItemsWithViewType[newItemPosition].first
            }
        })

        this.items.clear()
        this.items.addAll(newItemsWithViewType)

        diffResult.dispatchUpdatesTo(this)
    }

    fun setSelectedLayers(selectedLayersSettings: List<LayerSettings>) {
        this.selectedItems.clear()
        this.selectedItems.addAll(selectedLayersSettings)
        notifyDataSetChanged()
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: LayerSettings)
    }

    inner class LayerHeaderViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_layer_header,
                parent,
                false
            )
    ) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val icon: ImageView = itemView.findViewById(android.R.id.icon)

        override fun bind(item: LayerSettings) {
            when (item.getType()) {
                LayerType.TILES -> {
                    title.setText(R.string.alert_dialog_layers_type_tiles)
                    icon.setImageResource(R.drawable.ic_layer_tiles)
                }
                LayerType.VECTOR -> {
                    title.setText(R.string.alert_dialog_layers_type_vector)
                    icon.setImageResource(R.drawable.ic_layer_vector)
                }
                else -> icon.visibility = View.GONE
            }
        }
    }

    inner class LayerViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_selectable_item_1,
                parent,
                false
            )
    ) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val checkBox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        override fun bind(item: LayerSettings) {
            title.text = item.label
            checkBox.isChecked = selectedItems.contains(item)

            with(itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }
    }

    enum class ViewType {
        HEADER,
        LAYER
    }

    /**
     * Callback used by [LayerSettingsRecyclerViewAdapter].
     */
    interface OnLayerRecyclerViewAdapterListener {

        /**
         * Called when a list of [LayerSettings] were been selected.
         *
         * @param layersSettings the selected list of [LayerSettings]
         */
        fun onSelectedLayersSettings(layersSettings: List<LayerSettings>)

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}