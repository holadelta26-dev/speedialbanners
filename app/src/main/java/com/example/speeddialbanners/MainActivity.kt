package com.example.speeddialbanners

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var slots: MutableList<SpeedDialSlot>
    private lateinit var adapter: SpeedDialAdapter

    // Casilla que se está configurando en este momento
    private var slotBeingEdited: SpeedDialSlot? = null
    // Banner recién elegido (si el usuario está cambiando la foto en este flujo)
    private var pendingBannerUri: String? = null
    // Qué hacer justo después de que el usuario elija una foto
    private var afterBannerPicked: (() -> Unit)? = null

    // --- Selectores del sistema ---

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* algunos proveedores no lo soportan */ }

            pendingBannerUri = uri.toString()
            afterBannerPicked?.invoke()
        } else {
            // Canceló la elección de foto
            slotBeingEdited = null
            afterBannerPicked = null
        }
    }

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@registerForActivityResult
        val uri = data.data ?: return@registerForActivityResult
        val slot = slotBeingEdited ?: return@registerForActivityResult

        var name: String? = null
        var number: String? = null

        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (nameIdx >= 0) name = it.getString(nameIdx)
                if (numIdx >= 0) number = it.getString(numIdx)
            }
        }

        if (number == null) {
            Toast.makeText(this, R.string.error_no_number, Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        saveSlot(slot, name, number)
    }

    private val requestCallPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCallNumber?.let { doCall(it) }
        } else {
            Toast.makeText(this, R.string.error_call_permission, Toast.LENGTH_SHORT).show()
        }
        pendingCallNumber = null
    }

    private var pendingCallNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        slots = SlotStorage.load(this)

        adapter = SpeedDialAdapter(
            slots = slots,
            onShortPress = { slot -> startAssignFlow(slot) },
            onLongPress = { slot -> callSlot(slot) }
        )

        findViewById<RecyclerView>(R.id.rvSlots).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = this@MainActivity.adapter
        }
    }

    /** Toca una casilla: vacía = asignar desde cero; con datos = elegir qué cambiar. */
    private fun startAssignFlow(slot: SpeedDialSlot) {
        if (slot.isEmpty) {
            beginNewAssignment(slot)
        } else {
            AlertDialog.Builder(this)
                .setTitle(slot.contactName ?: slot.phoneNumber)
                .setItems(
                    arrayOf(
                        getString(R.string.option_change_number),
                        getString(R.string.option_change_photo),
                        getString(R.string.action_remove)
                    )
                ) { _, which ->
                    when (which) {
                        0 -> {
                            slotBeingEdited = slot
                            pendingBannerUri = null // conservamos la foto actual
                            chooseNumberSource(slot)
                        }
                        1 -> changeBannerOnly(slot)
                        2 -> {
                            slot.contactName = null
                            slot.phoneNumber = null
                            slot.bannerUri = null
                            SlotStorage.save(this, slots)
                            adapter.refresh()
                        }
                    }
                }
                .show()
        }
    }

    /** Flujo para una casilla vacía: primero foto, luego número (contacto o manual). */
    private fun beginNewAssignment(slot: SpeedDialSlot) {
        slotBeingEdited = slot
        afterBannerPicked = { chooseNumberSource(slot) }
        Toast.makeText(this, R.string.pick_banner_first, Toast.LENGTH_SHORT).show()
        pickImageLauncher.launch("image/*")
    }

    /** Cambia solo la foto, conservando el número/nombre ya guardados. */
    private fun changeBannerOnly(slot: SpeedDialSlot) {
        slotBeingEdited = slot
        afterBannerPicked = {
            slot.bannerUri = pendingBannerUri
            SlotStorage.save(this, slots)
            adapter.refresh()
            slotBeingEdited = null
            pendingBannerUri = null
        }
        pickImageLauncher.launch("image/*")
    }

    /** Pregunta si el número se toma de la agenda o se escribe a mano. */
    private fun chooseNumberSource(slot: SpeedDialSlot) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_number_source_title)
            .setItems(
                arrayOf(
                    getString(R.string.option_from_contacts),
                    getString(R.string.option_manual)
                )
            ) { _, which ->
                when (which) {
                    0 -> pickContactLauncher.launch(
                        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    )
                    1 -> showManualNumberDialog(slot)
                }
            }
            .setOnCancelListener {
                slotBeingEdited = null
                pendingBannerUri = null
            }
            .show()
    }

    /** Diálogo simple para escribir nombre (opcional) y número a mano. */
    private fun showManualNumberDialog(slot: SpeedDialSlot) {
        val padding = (16 * resources.displayMetrics.density).toInt()

        val etName = EditText(this).apply {
            hint = getString(R.string.hint_name)
            setText(slot.contactName ?: "")
        }
        val etNumber = EditText(this).apply {
            hint = getString(R.string.hint_number)
            inputType = InputType.TYPE_CLASS_PHONE
            setText(slot.phoneNumber ?: "")
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            gravity = Gravity.CENTER
            addView(etName)
            addView(etNumber)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.option_manual)
            .setView(container)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val number = etNumber.text.toString().trim()
                if (number.isEmpty()) {
                    Toast.makeText(this, R.string.error_no_number, Toast.LENGTH_SHORT).show()
                    slotBeingEdited = null
                    pendingBannerUri = null
                    return@setPositiveButton
                }
                val name = etName.text.toString().trim().ifEmpty { null }
                saveSlot(slot, name, number)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                slotBeingEdited = null
                pendingBannerUri = null
            }
            .show()
    }

    private fun saveSlot(slot: SpeedDialSlot, name: String?, number: String) {
        slot.contactName = name
        slot.phoneNumber = number
        slot.bannerUri = pendingBannerUri ?: slot.bannerUri
        SlotStorage.save(this, slots)
        adapter.refresh()

        slotBeingEdited = null
        pendingBannerUri = null
        afterBannerPicked = null
    }

    /** Mantener presionado: llama directo, pidiendo permiso CALL_PHONE si hace falta. */
    private fun callSlot(slot: SpeedDialSlot) {
        val number = slot.phoneNumber ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            doCall(number)
        } else {
            pendingCallNumber = number
            requestCallPermission.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun doCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        startActivity(intent)
    }
}
