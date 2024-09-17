package fr.geonature.maps.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerType

/**
 * Default [RecyclerView.Adapter] for [LayerSettings].
 *
 * @author S. Grimault
 */
class LayerSettingsRecyclerViewAdapter(private val listener: OnLayerRecyclerViewAdapterListener) :
    RecyclerView.Adapter<LayerSettingsRecyclerViewAdapter.AbstractViewHolder>() {

    private val items = mutableListOf<Pair<LayerSettings, ViewType>>()
    private val selectedItems = mutableMapOf<String, LayerSettings>()

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (ViewType.values()[viewType]) {
            ViewType.HEADER_LAYER_ONLINE -> LayerOnlineHeaderViewHolder(parent)
            ViewType.HEADER_LAYER -> LayerHeaderViewHolder(parent)
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
        val sortedItems = newItems.sorted()
        val newItemsWithViewType =
            sortedItems.asSequence()
                .mapIndexed { index, layerSettings ->
                    when {
                        // first item
                        index == 0 -> mutableListOf(
                            Pair(
                                layerSettings,
                                if (layerSettings.isOnline()) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                            ),
                            Pair(
                                layerSettings,
                                ViewType.LAYER
                            )
                        )
                        // same type but one of them refer to an online source
                        sortedItems[index - 1].getType() == layerSettings.getType() && sortedItems[index - 1].isOnline() != layerSettings.isOnline() -> mutableListOf(
                            Pair(
                                layerSettings,
                                if (layerSettings.isOnline()) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                            ),
                            Pair(
                                layerSettings,
                                ViewType.LAYER
                            )
                        )
                        // different type
                        sortedItems[index - 1].getType() != layerSettings.getType() -> mutableListOf(
                            Pair(
                                layerSettings,
                                if (layerSettings.isOnline()) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                            ),
                            Pair(
                                layerSettings,
                                ViewType.LAYER
                            )
                        )
                        // same type
                        sortedItems[index - 1].getType() == layerSettings.getType() -> mutableListOf(
                            Pair(
                                layerSettings,
                                ViewType.LAYER
                            )
                        )
                        // default case
                        else -> mutableListOf(
                            Pair(
                                layerSettings,
                                ViewType.LAYER
                            )
                        )
                    }
                }
                .flatMap { it.asSequence() }
                .toList()

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
                return sortedItems.size
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
        this.selectedItems.putAll(selectedLayersSettings.map {
            Pair(
                it.getPrimarySource(),
                it
            )
        })

        notifyDataSetChanged()
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: LayerSettings)
    }

    inner class LayerOnlineHeaderViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_layer_header,
                parent,
                false
            )
    ) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val icon: ImageView = itemView.findViewById(android.R.id.icon)
        private val switch: SwitchCompat = itemView.findViewById(android.R.id.toggle)

        init {
            switch.setOnClickListener { view ->
                (selectedItems.values + items.filter { it.first.isOnline() }
                    .map { it.first }).firstOrNull { it.isOnline() }
                    ?.also {
                        selectedItems[it.getPrimarySource()] =
                            it.copy(properties = it.properties.copy(active = (view as SwitchCompat).isChecked))
                    }

                listener.onSelectedLayersSettings(selectedItems.values.toList())

                notifyDataSetChanged()
            }
        }

        override fun bind(item: LayerSettings) {
            title.setText(R.string.alert_dialog_layers_type_tiles_online)
            icon.setImageResource(R.drawable.ic_layer_online)

            with(switch) {
                visibility = View.VISIBLE
                isChecked = selectedItems.values.any { it.isOnline() && it.properties.active }
            }
        }
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

        init {
            itemView.setOnClickListener { view ->
                val checkbox: CheckBox = view.findViewById(android.R.id.checkbox)

                val layerSettings = view.tag as LayerSettings
                val isAlreadySelected =
                    selectedItems.values.any { it.source == layerSettings.source }

                if (isAlreadySelected) {
                    selectedItems.remove(layerSettings.getPrimarySource())
                    checkbox.isChecked = false
                } else {
                    if (layerSettings.isOnline()) {
                        selectedItems.values
                            .filter { it.isOnline() }
                            .forEach {
                                selectedItems.remove(it.getPrimarySource())
                            }
                    }

                    selectedItems[layerSettings.getPrimarySource()] = layerSettings
                    checkbox.isChecked = true
                }

                notifyDataSetChanged()

                listener.onSelectedLayersSettings(selectedItems.values.toList())
            }
        }

        override fun bind(item: LayerSettings) {
            title.text = item.label
            checkBox.isChecked = selectedItems.values.any { it.source == item.source }

            with(itemView) {
                tag = item
                isEnabled =
                    if (item.isOnline()) selectedItems.values.any { it.isOnline() && it.properties.active }
                    else selectedItems.values.none { it.source == item.source && !it.properties.active }
            }
        }
    }

    enum class ViewType {
        HEADER_LAYER_ONLINE,
        HEADER_LAYER,
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