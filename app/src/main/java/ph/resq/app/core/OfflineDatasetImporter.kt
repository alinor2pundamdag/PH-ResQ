package ph.resq.app.core

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/**
 * Imports user-selected JSON into the local offline caches. Dataset metadata is provenance supplied
 * by the file, not a cryptographic signature; the UI must not label an import cryptographically verified.
 */
object OfflineDatasetImporter {
    private const val MAX_FILE_BYTES = 10 * 1024 * 1024

    sealed class ParsedDataset {
        data class Directory(val metadata: DirectoryDataset, val records: List<DirectoryRecord>) : ParsedDataset()
        data class Advisories(val records: List<CachedAdvisory>) : ParsedDataset()
        data class EvacuationCenters(val records: List<EvacuationCenter>) : ParsedDataset()
    }

    fun importDataset(context: Context, uri: Uri, directory: OfflineDataRepository, hazards: HazardDataRepository): String {
        val json = readBounded(context, uri)
        return when (val dataset = parseJson(json)) {
            is ParsedDataset.Directory -> {
                directory.replaceDirectory(dataset.records, dataset.metadata)
                "Imported ${dataset.records.size} directory records (metadata is not signature-verified)."
            }
            is ParsedDataset.Advisories -> {
                hazards.replaceAdvisories(dataset.records)
                "Imported ${dataset.records.size} cached advisories; verify source and freshness before relying on them."
            }
            is ParsedDataset.EvacuationCenters -> {
                hazards.replaceEvacuationCenters(dataset.records)
                "Imported ${dataset.records.size} evacuation-center records; status may be stale."
            }
        }
    }

    internal fun parseJson(json: String): ParsedDataset {
        require(json.length <= MAX_FILE_BYTES) { "JSON file exceeds the 10 MB limit." }
        val root = JSONObject(json)
        return when (root.requiredString("type")) {
            "directory" -> {
                val records = root.requiredArray("records").toObjects { item ->
                    DirectoryRecord(
                        id = item.requiredString("id"),
                        name = item.requiredString("name"),
                        category = item.requiredString("category"),
                        phone = item.optionalString("phone"),
                        latitude = item.optionalDouble("latitude"),
                        longitude = item.optionalDouble("longitude")
                    )
                }
                ParsedDataset.Directory(
                    DirectoryDataset(
                        sourceUrl = root.requiredString("sourceUrl"),
                        license = root.requiredString("license"),
                        version = root.requiredString("version"),
                        verifiedAt = root.getLong("verifiedAt")
                    ),
                    records
                )
            }
            "advisories" -> ParsedDataset.Advisories(root.requiredArray("records").toObjects { item ->
                CachedAdvisory(
                    id = item.requiredString("id"),
                    title = item.requiredString("title"),
                    severity = runCatching { AdvisorySeverity.valueOf(item.requiredString("severity").uppercase()) }
                        .getOrElse { throw IllegalArgumentException("Unknown advisory severity.") },
                    body = item.requiredString("body"),
                    sourceUrl = item.requiredString("sourceUrl"),
                    publishedAt = item.getLong("publishedAt"),
                    fetchedAt = item.getLong("fetchedAt"),
                    expiresAt = item.getLong("expiresAt")
                )
            })
            "evacuation_centers" -> ParsedDataset.EvacuationCenters(root.requiredArray("records").toObjects { item ->
                EvacuationCenter(
                    id = item.requiredString("id"),
                    name = item.requiredString("name"),
                    address = item.requiredString("address"),
                    latitude = item.optionalDouble("latitude"),
                    longitude = item.optionalDouble("longitude"),
                    status = runCatching { EvacuationStatus.valueOf(item.requiredString("status").uppercase()) }
                        .getOrElse { throw IllegalArgumentException("Unknown evacuation-center status.") },
                    sourceUrl = item.requiredString("sourceUrl"),
                    verifiedAt = item.getLong("verifiedAt")
                )
            })
            else -> throw IllegalArgumentException("Unsupported dataset type. Use directory, advisories, or evacuation_centers.")
        }
    }

    private fun readBounded(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open selected file.")
        input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_FILE_BYTES) { "JSON file exceeds the 10 MB limit." }
                output.write(buffer, 0, count)
            }
            return output.toString(Charsets.UTF_8.name())
        }
    }

    private fun JSONObject.requiredString(key: String): String {
        val value = optString(key, "").trim()
        require(value.isNotEmpty() && value != "null") { "Missing required field: $key" }
        return value
    }

    private fun JSONObject.optionalString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).trim().takeIf { it.isNotEmpty() }

    private fun JSONObject.optionalDouble(key: String): Double? =
        if (!has(key) || isNull(key)) null else getDouble(key)

    private fun JSONObject.requiredArray(key: String): JSONArray {
        val array = optJSONArray(key) ?: throw IllegalArgumentException("Missing array: $key")
        require(array.length() in 1..250_000) { "Array '$key' must contain 1..250000 records." }
        return array
    }

    private fun <T> JSONArray.toObjects(parse: (JSONObject) -> T): List<T> {
        val records = ArrayList<T>(length())
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: throw IllegalArgumentException("Record $index is not a JSON object.")
            records.add(parse(item))
        }
        return records
    }
}
