package fr.geonature.maps.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings

/**
 * [DialogFragment] to let the user to select [LayerSettings] to show on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LayerSettingsDialogFragment : DialogFragment() {

    private var listener: OnLayerSettingsDialogFragmentListener? = null
    private var adapter: LayerSettingsRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (parentFragment is OnLayerSettingsDialogFragmentListener) {
            listener = parentFragment as OnLayerSettingsDialogFragmentListener
        } else {
            throw RuntimeException("$parentFragment must implement OnLayerSettingsDialogFragmentListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val view = View.inflate(
            context,
            R.layout.fragment_recycler_view,
            null
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyTextView = view.findViewById<TextView>(R.id.emptyTextView)
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
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    emptyTextView.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LayerSettingsDialogFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }

        return AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.alert_dialog_layers_title)
            .setNegativeButton(
                R.string.alert_dialog_layers_close,
                null
            )
            .create()
    }

    override fun onResume() {
        super.onResume()

        adapter?.also {
            it.setItems(arguments?.getParcelableArrayList(ARG_LAYERS) ?: emptyList())
            it.setSelectedLayers(
                arguments?.getParcelableArrayList(ARG_LAYERS_SELECTION) ?: emptyList()
            )
        }
    }

    /**
     * Callback used by [LayerSettingsDialogFragment].
     */
    interface OnLayerSettingsDialogFragmentListener {

        /**
         * Called when a list of [LayerSettings] were been selected.
         *
         * @param layersSettings the selected list of [LayerSettings]
         */
        fun onSelectedLayersSettings(layersSettings: List<LayerSettings>)
    }

    companion object {
        const val ARG_LAYERS = "arg_layers"
        const val ARG_LAYERS_SELECTION = "arg_layers_selection"

        /**
         * Use this factory method to create a new instance of [LayerSettingsDialogFragment].
         *
         * @return A new instance of [LayerSettingsDialogFragment]
         */
        @JvmStatic
        fun newInstance(
            layersSettings: List<LayerSettings>,
            selection: List<LayerSettings> = emptyList()
        ) = LayerSettingsDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    ARG_LAYERS,
                    ArrayList(layersSettings)
                )
                putParcelableArrayList(
                    ARG_LAYERS_SELECTION,
                    ArrayList(selection)
                )
            }
        }
    }
}