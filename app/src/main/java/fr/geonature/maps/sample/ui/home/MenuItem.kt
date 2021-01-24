package fr.geonature.maps.sample.ui.home

import fr.geonature.maps.settings.MapSettings

/**
 * Describes a menu entry.
 */
data class MenuItem(val label: String, val mapSettings: MapSettings? = null)