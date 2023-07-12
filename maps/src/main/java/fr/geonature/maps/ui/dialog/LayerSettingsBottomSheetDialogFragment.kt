package fr.geonature.maps.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings

/**
 * Custom [DialogFragment] to show a bottom sheet to let the user to select [LayerSettings] to show
 * on the map.
 *
 * @author S. Grimault
 */
class LayerSettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var listener: OnLayerSettingsDialogFragmentListener? = null
    private var adapter: LayerSettingsRecyclerViewAdapter? = null

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

            override fun onSelectedLayersSettings(layersSettings: List<LayerSettings>) {
                listener?.onSelectedLayersSettings(layersSettings)
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView.visibility == View.VISIBLE == show) {
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
    }

    fun setOnLayerSettingsDialogFragmentListener(listener: OnLayerSettingsDialogFragmentListener) {
        this.listener = listener
    }

    override fun onResume() {
        super.onResume()

        adapter?.also {
            it.setItems(
                arguments?.getParcelableArrayCompat<LayerSettings>(ARG_LAYERS)
                    ?.asList() ?: emptyList()
            )
            it.setSelectedLayers(
                arguments?.getParcelableArrayCompat<LayerSettings>(ARG_LAYERS_SELECTION)
                    ?.asList() ?: emptyList()
            )
        }
    }

    /**
     * Callback used by [LayerSettingsBottomSheetDialogFragment].
     */
    interface OnLayerSettingsDialogFragmentListener {

        /**
         * Called when a list of [LayerSettings] were been selected.
         *
         * @param layersSettings the selected list of [LayerSettings]
         */
        fun onSelectedLayersSettings(layersSettings: List<LayerSettings>)

        /**
         * Called when we want to add a layer.
         */
        fun onAddLayer()
    }

    companion object {
        const val ARG_LAYERS = "arg_layers"
        const val ARG_LAYERS_SELECTION = "arg_layers_selection"

        /**
         * Use this factory method to create a new instance of [LayerSettingsBottomSheetDialogFragment].
         *
         * @return A new instance of [LayerSettingsBottomSheetDialogFragment]
         */
        @JvmStatic
        fun newInstance(
            layersSettings: List<LayerSettings>,
            selection: List<LayerSettings> = emptyList()
        ) = LayerSettingsBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArray(
                    ARG_LAYERS,
                    layersSettings.toTypedArray()
                )
                putParcelableArray(
                    ARG_LAYERS_SELECTION,
                    selection.toTypedArray()
                )
            }
        }
    }
}