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
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerType
import org.tinylog.kotlin.Logger

/**
 * Default [RecyclerView.Adapter] for [LayerState].
 *
 * @author S. Grimault
 */
class LayerSettingsRecyclerViewAdapter(private val listener: OnLayerRecyclerViewAdapterListener) :
    RecyclerView.Adapter<LayerSettingsRecyclerViewAdapter.AbstractViewHolder>() {

    private val items = mutableListOf<Pair<LayerState, ViewType>>()

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        return when (ViewType.entries[viewType]) {
            ViewType.HEADER_LAYER_ONLINE -> LayerOnlineHeaderViewHolder(parent)
            ViewType.HEADER_LAYER -> LayerHeaderViewHolder(parent)
            else -> LayerViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(
        holder: AbstractViewHolder,
        position: Int
    ) {
        holder.bind(items[position].first)
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].second.ordinal
    }

    /**
     * Sets layers.
     */
    fun setItems(
        newItems: List<LayerState>,
        notify: Boolean = false
    ) {
        val sortedItems = newItems.sorted()

        Logger.debug {
            "layers:\n${
                sortedItems.joinToString("\n") {
                    when (it) {
                        is LayerState.Layer -> "\t'${it.settings.label}': ${it.source} (active: ${it.active})"
                        is LayerState.SelectedLayer -> "\t'${it.settings.label}': ${it.source} (selected: true, active: ${it.active})"
                        is LayerState.Error -> "\t'${it.getLayerSettings().label}': ${it.getLayerSettings().label} (error: true)"
                    }
                }
            }"
        }

        val newItemsWithViewType = sortedItems.asSequence()
            .mapIndexed { index, layerState ->
                when {
                    // first item
                    index == 0 -> mutableListOf(
                        Pair(
                            layerState,
                            if (layerState.getLayerSettings()
                                    .isOnline()
                            ) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                        ),
                        Pair(
                            layerState,
                            ViewType.LAYER
                        )
                    )
                    // same type but one of them refer to an online source
                    sortedItems[index - 1].getLayerSettings()
                        .getType() == layerState.getLayerSettings()
                        .getType() && sortedItems[index - 1].getLayerSettings()
                        .isOnline() != layerState.getLayerSettings()
                        .isOnline() -> mutableListOf(
                        Pair(
                            layerState,
                            if (layerState.getLayerSettings()
                                    .isOnline()
                            ) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                        ),
                        Pair(
                            layerState,
                            ViewType.LAYER
                        )
                    )
                    // different type
                    sortedItems[index - 1].getLayerSettings()
                        .getType() != layerState.getLayerSettings()
                        .getType() -> mutableListOf(
                        Pair(
                            layerState,
                            if (layerState.getLayerSettings()
                                    .isOnline()
                            ) ViewType.HEADER_LAYER_ONLINE else ViewType.HEADER_LAYER
                        ),
                        Pair(
                            layerState,
                            ViewType.LAYER
                        )
                    )
                    // same type
                    sortedItems[index - 1].getLayerSettings()
                        .getType() == layerState.getLayerSettings()
                        .getType() -> mutableListOf(
                        Pair(
                            layerState,
                            ViewType.LAYER
                        )
                    )
                    // default case
                    else -> mutableListOf(
                        Pair(
                            layerState,
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
            }

            return
        }

        if (newItemsWithViewType.isEmpty()) {
            val count = itemCount
            this.items.clear()
            notifyItemRangeRemoved(
                0,
                count
            )

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@LayerSettingsRecyclerViewAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return newItemsWithViewType.size
            }

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@LayerSettingsRecyclerViewAdapter.items[oldItemPosition].first.getLayerSettings().source == newItemsWithViewType[newItemPosition].first.getLayerSettings().source
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

        if (notify) {
            listener.onSelectedLayers(this.items.filter { it.second == ViewType.LAYER }
                .map { it.first }
                .filterIsInstance<LayerState.SelectedLayer>()
                .filter { it.active },
                this.items.filter { it.second == ViewType.LAYER }
                    .map { it.first }
                    .any {
                        when (it) {
                            is LayerState.Layer -> it.getLayerSettings()
                                .isOnline() && it.active

                            is LayerState.SelectedLayer -> it.getLayerSettings()
                                .isOnline() && it.active

                            else -> false
                        }
                    })
        }
    }

    /**
     * Whether to use online layers to show on the map.
     */
    private fun useOnlineLayers(useOnlineLayers: Boolean) {
        setItems(this.items.filter { it.second == ViewType.LAYER }
            .map { it.first }
            .map {
                when (it) {
                    is LayerState.Layer -> if (it.getLayerSettings()
                            .isOnline()
                    ) it.copy(active = useOnlineLayers) else it

                    is LayerState.SelectedLayer -> if (it.getLayerSettings()
                            .isOnline()
                    ) it.copy(active = useOnlineLayers) else it

                    is LayerState.Error -> it
                }
            },
            notify = true
        )
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: LayerState)
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
            // activate or not selection of online layers
            switch.setOnClickListener { view ->
                useOnlineLayers((view as SwitchCompat).isChecked)
            }
        }

        override fun bind(item: LayerState) {
            title.setText(R.string.alert_dialog_layers_type_tiles_online)
            icon.setImageResource(R.drawable.ic_layer_online)

            with(switch) {
                visibility = View.VISIBLE
                isChecked = items.any {
                    when (val layerState = it.first) {
                        is LayerState.Layer -> layerState.settings.isOnline() && layerState.active
                        is LayerState.SelectedLayer -> layerState.settings.isOnline() && layerState.active
                        is LayerState.Error -> false
                    }
                }
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

        override fun bind(item: LayerState) {
            when (item.getLayerSettings()
                .getType()) {
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
                checkbox.isChecked = !checkbox.isChecked

                val layerSettings = view.tag as LayerSettings

                setItems(
                    items.filter { it.second == ViewType.LAYER }
                        .map {
                            if (it.first.getLayerSettings() == layerSettings) when (val layerState =
                                it.first) {
                                is LayerState.Layer -> if (checkbox.isChecked) layerState.select() else layerState
                                is LayerState.SelectedLayer -> if (checkbox.isChecked) layerState else layerState.toLayer()
                                is LayerState.Error -> layerState
                            }
                            else when (val layerState = it.first) {
                                is LayerState.Layer -> layerState
                                is LayerState.SelectedLayer -> if (checkbox.isChecked && layerState.getLayerSettings()
                                        .isOnline()
                                ) layerState.toLayer() else layerState

                                is LayerState.Error -> layerState
                            }
                        },
                    notify = true,
                )
            }
        }

        override fun bind(item: LayerState) {
            title.text = item.getLayerSettings().label
            checkBox.isChecked = item is LayerState.SelectedLayer

            with(itemView) {
                tag = item.getLayerSettings()
                isEnabled = when (item) {
                    is LayerState.Layer -> item.active
                    is LayerState.SelectedLayer -> item.active
                    is LayerState.Error -> false
                }
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
         * Called when a list of [LayerState.SelectedLayer] were been selected.
         *
         * @param layers the selected list of [LayerState.SelectedLayer]
         */
        fun onSelectedLayers(
            layers: List<LayerState.SelectedLayer>,
            useOnlineLayers: Boolean
        )

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}