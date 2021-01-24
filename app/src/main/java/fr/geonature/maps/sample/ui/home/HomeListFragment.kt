package fr.geonature.maps.sample.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.maps.sample.R
import fr.geonature.maps.settings.MapSettings

/**
 * A fragment representing a list of [MenuItem].
 */
class HomeListFragment : Fragment() {

    private var listener: OnHomeListFragmentListener? = null
    private var adapter: MenuItemRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_home_list,
            container,
            false
        )

        // Set the adapter
        if (view is RecyclerView) {
            adapter = MenuItemRecyclerViewAdapter(object :
                MenuItemRecyclerViewAdapter.OnMenuItemRecyclerViewAdapterListener {
                override fun onClick(menuItem: MenuItem) {
                    listener?.onSelectedMapSettings(menuItem.mapSettings)
                }
            })

            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@HomeListFragment.adapter
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        (layoutManager as LinearLayoutManager).orientation
                    )
                )
            }
        }
        
        loadMenuItems()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnHomeListFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnHomeListFragmentListener")
        }
    }

    private fun loadMenuItems() {
        adapter?.setItems(
            listOf(
                MenuItem(
                    getString(R.string.home_menu_entry_default),
                    MapSettings.Builder.newInstance()
                        .minZoomLevel(3.0)
                        .zoom(5.0)
                        .build()
                ),
                MenuItem(getString(R.string.home_menu_entry_from_storage))
            )
        )
    }

    /**
     * Callback used by [HomeListFragment].
     */
    interface OnHomeListFragmentListener {
        fun onSelectedMapSettings(mapSettings: MapSettings?)
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeListFragment()
    }
}