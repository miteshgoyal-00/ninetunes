package app.ninetunes.providers.innertube.models.bodies

import app.ninetunes.providers.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context = Context.DefaultWeb,
    val query: String,
    val params: String
)
