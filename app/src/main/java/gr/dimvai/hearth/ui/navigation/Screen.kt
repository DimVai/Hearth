package gr.dimvai.hearth.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Dashboard : Screen()
    
    @Serializable
    data object AddConnection : Screen()
    
    @Serializable
    data class EditConnection(val connectionId: String) : Screen()
    
    @Serializable
    data object Settings : Screen()
}
