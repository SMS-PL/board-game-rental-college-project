package sms.pollub.gamerental.client.queries

data class GetClientsQuery(
    val search: String? = null,
    val sortBy: String = "lastName"
)

