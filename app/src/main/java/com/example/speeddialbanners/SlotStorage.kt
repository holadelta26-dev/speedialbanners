package com.example.speeddialbanners

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "speed_dial_prefs"
private const val KEY_SLOTS = "slots_json"
const val SLOT_COUNT = 8

object SlotStorage {

    fun load(context: Context): MutableList<SpeedDialSlot> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SLOTS, null)

        val slots = MutableList(SLOT_COUNT) { SpeedDialSlot(it) }

        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getInt("slotId")
                if (id in 0 until SLOT_COUNT) {
                    slots[id] = SpeedDialSlot(
                        slotId = id,
                        contactName = obj.optString("contactName", null),
                        phoneNumber = obj.optString("phoneNumber", null),
                        bannerUri = obj.optString("bannerUri", null)
                    )
                }
            }
        }
        return slots
    }

    fun save(context: Context, slots: List<SpeedDialSlot>) {
        val array = JSONArray()
        slots.forEach { slot ->
            val obj = JSONObject()
            obj.put("slotId", slot.slotId)
            obj.put("contactName", slot.contactName ?: "")
            obj.put("phoneNumber", slot.phoneNumber ?: "")
            obj.put("bannerUri", slot.bannerUri ?: "")
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SLOTS, array.toString()).apply()
    }
}
