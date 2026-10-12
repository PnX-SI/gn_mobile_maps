package fr.geonature.maps.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.geonature.maps.R
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.presentation.LayerViewModel

/**
 * Custom [DialogFragment] to show a bottom sheet to let the user select [LayerState.Layer] to show
 * on the map.
 *
 * @author S. Grimault
 */
class LayerSettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var listener: OnLayerSettingsDialogFragmentListener? = null
    private var adapter: LayerSettingsRecyclerViewAdapter? = null

    private val layerViewModel: LayerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.bottom_sheet_layers,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        view.findViewById<Toolbar>(R.id.toolbar)
            .apply {
                setTitle(R.string.alert_dialog_layers_title)
                inflateMenu(R.menu.layer_add)
                setNavigationOnClickListener {
                    dismiss()
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_add_layer -> {
                            listener?.onAddLayer()
                            dismiss()
                            true
                        }

                        else -> false
                    }
                }
            }

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty)
            .apply {
                setText(R.string.alert_dialog_layers_no_data)
            }

        // Set the adapter
        adapter = LayerSettingsRecyclerViewAdapter(object :
            LayerSettingsRecyclerViewAdapter.OnLayerRecyclerViewAdapterListener {

            override fun onSelectedLayers(
                layers: List<LayerState.SelectedLayer>,
                useOnlineLayers: Boolean
            ) {
                listener?.onSelectedLayers(
                    layers,
                    useOnlineLayers
                )
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView.isVisible == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LayerSettingsBottomSheetDialogFragment.adapter
        }
            .also {
                it.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        (it.layoutManager as LinearLayoutManager).orientation
                    )
                )
            }

        // observe all layers and update adapter
        layerViewModel.allLayers.observe(viewLifecycleOwner) { layers ->
            adapter?.setItems(layers)
        }
    }

    fun setOnLayerSettingsDialogFragmentListener(listener: OnLayerSettingsDialogFragmentListener) {
        this.listener = listener
    }

    /**
     * Callback used by [LayerSettingsBottomSheetDialogFragment].
     */
    interface OnLayerSettingsDialogFragmentListener {

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
         * Called when we want to add a layer.
         */
        fun onAddLayer()
    }

    companion object {
        const val ARG_USE_ONLINE_LAYERS = "arg_use_online_layers"

        /**
         * Use this factory method to create a new instance of [LayerSettingsBottomSheetDialogFragment].
         *
         * @return A new instance of [LayerSettingsBottomSheetDialogFragment]
         */
        @JvmStatic
        fun newInstance(useOnlineLayers: Boolean) =
            LayerSettingsBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(
                        ARG_USE_ONLINE_LAYERS,
                        useOnlineLayers
                    )
                }
            }
    }
}

