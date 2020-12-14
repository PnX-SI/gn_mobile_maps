package fr.geonature.maps.sample.settings

import android.app.Application
import fr.geonature.maps.sample.settings.io.OnAppSettingsJsonReaderListenerImpl
import fr.geonature.commons.settings.AppSettingsViewModel as BaseAppSettingsViewModel

/**
 * [AppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsViewModel(application: Application) : BaseAppSettingsViewModel<AppSettings>(
    application,
    OnAppSettingsJsonReaderListenerImpl()
)
