package com.example.inventariocarniceria.data.sync

import android.content.Context
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * GESTOR DE SINCRONIZACIÓN EN LA NUBE - SUPABASE
 * Base de datos del proyecto: sgnia-pos-master
 *
 * Características:
 * - 100% Offline-First: Las operaciones locales nunca se interrumpen si no hay internet.
 * - Aislamiento estricto por Dueño (owner_hw_id): Ninguna sucursal ni dueño puede ver
 *   datos de otra carnicería.
 */
object SupabaseSyncManager {
    private const val BASE_URL = "https://zwwlwqraowpqlosvudpi.supabase.co/rest/v1"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp3d2x3cXJhb3dwcWxvc3Z1ZHBpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI4ODA0MjAsImV4cCI6MjEwODQ1NjQyMH0.7c5HzT887ITrBgPG9UApctA7NEkt3-orHdRJxLFjt0A"

    private const val PREFS_NAME = "sgnia_sync_prefs"
    private const val KEY_LINKED_OWNER = "linked_owner_hw_id"

    fun setLinkedOwner(context: Context, ownerHwId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LINKED_OWNER, ownerHwId.trim())
            .apply()
    }

    fun getLinkedOwner(context: Context): String {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LINKED_OWNER, "") ?: ""
        if (stored.isNotBlank()) return stored
        // Respaldo transparente: si no hay un dueño central vinculado, la terminal usa su propio hardware ID
        return com.example.inventariocarniceria.data.licensing.LicenseManager.getHardwareId(context)
    }

    // --- 1. SUCURSALES (CREACIÓN Y CONSULTA) ---
    suspend fun pushBranch(ownerHwId: String, branch: BranchEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("owner_hw_id", ownerHwId.trim())
                put("branch_name", branch.branchName.trim())
                put("manager_name", branch.managerName.trim())
                put("phone", branch.phone.trim())
                put("address", branch.address.trim())
                put("password", branch.password.trim())
                put("is_active", branch.isActive)
            }
            val code = makePostRequest("$BASE_URL/branches", json.toString())
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchBranchesForOwner(ownerHwId: String): List<BranchEntity> = withContext(Dispatchers.IO) {
        if (ownerHwId.isBlank()) return@withContext emptyList()
        try {
            val url = "$BASE_URL/branches?owner_hw_id=eq.${ownerHwId.trim()}&select=*"
            val resText = makeGetRequest(url)
            val jsonArray = JSONArray(resText)
            val list = mutableListOf<BranchEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    BranchEntity(
                        id = obj.optLong("id", 0L),
                        branchName = obj.optString("branch_name"),
                        managerName = obj.optString("manager_name"),
                        phone = obj.optString("phone"),
                        address = obj.optString("address"),
                        password = obj.optString("password", "1234"),
                        isActive = obj.optBoolean("is_active", true)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- 2. VENTAS Y PESAJES EN VIVO DESDE LA BÁSCULA ---
    suspend fun pushCapture(ownerHwId: String, branchName: String, capture: InventoryCaptureEntity): Boolean = withContext(Dispatchers.IO) {
        if (ownerHwId.isBlank()) return@withContext false
        try {
            val json = JSONObject().apply {
                put("owner_hw_id", ownerHwId.trim())
                put("branch_name", branchName.trim())
                put("product_code", capture.productCode)
                put("product_name", capture.productName)
                put("captured_weight", capture.capturedWeight)
                put("unit_type", capture.unitType)
                put("timestamp", capture.timestamp)
            }
            val code = makePostRequest("$BASE_URL/inventory_captures", json.toString())
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    // --- 3. CATÁLOGO MAESTRO DE CORTES (PLUs Y PRECIOS) ---
    suspend fun pushProducts(ownerHwId: String, products: List<ProductEntity>): Boolean = withContext(Dispatchers.IO) {
        if (ownerHwId.isBlank() || products.isEmpty()) return@withContext true
        try {
            val jsonArray = JSONArray()
            for (p in products) {
                val obj = JSONObject().apply {
                    put("owner_hw_id", ownerHwId.trim())
                    put("code", p.code)
                    put("name", p.name)
                    put("price", p.price)
                    put("category", p.category)
                }
                jsonArray.put(obj)
            }
            val url = "$BASE_URL/products?on_conflict=owner_hw_id,code"
            val code = makePostRequest(url, jsonArray.toString(), upsert = true)
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun pullProducts(ownerHwId: String): List<ProductEntity> = withContext(Dispatchers.IO) {
        if (ownerHwId.isBlank()) return@withContext emptyList()
        try {
            val url = "$BASE_URL/products?owner_hw_id=eq.${ownerHwId.trim()}&select=*"
            val resText = makeGetRequest(url)
            val jsonArray = JSONArray(resText)
            val list = mutableListOf<ProductEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ProductEntity(
                        code = obj.getInt("code"),
                        name = obj.getString("name"),
                        price = obj.optDouble("price", 0.0),
                        category = obj.optString("category", "General")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- MOTOR HTTP NATIVO SIN DEPENDENCIAS EXTERNAS ---
    private fun makePostRequest(urlString: String, jsonBody: String, upsert: Boolean = false): Int {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.doOutput = true
        conn.setRequestProperty("apikey", ANON_KEY)
        conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")
        conn.setRequestProperty("Content-Type", "application/json")
        val prefer = if (upsert) "resolution=merge-duplicates,return=minimal" else "return=minimal"
        conn.setRequestProperty("Prefer", prefer)

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(jsonBody) }
        val code = conn.responseCode
        conn.disconnect()
        return code
    }

    private fun makeGetRequest(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.setRequestProperty("apikey", ANON_KEY)
        conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")
        conn.setRequestProperty("Accept", "application/json")

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        conn.disconnect()
        return text
    }
}
