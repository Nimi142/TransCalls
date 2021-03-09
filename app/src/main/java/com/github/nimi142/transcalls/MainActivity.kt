package com.github.nimi142.transcalls

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var logs: MutableList<List<Any?>>
    private val allCalls: Uri = Uri.parse("content://call_log/calls")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Request permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Alert user
                // Request permissions
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG),
                        1)
            }
        }


        try {
            val pInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            findViewById<TextView>(R.id.buildVersionLabel).text = getString(R.string.build_version) + " " + pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


    }

    fun chooseImportFile(v: View) {
        val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 2)
    }

    fun exportLogs(v: View) {
        logs = mutableListOf()
        try {
            val c: Cursor = applicationContext.contentResolver.query(allCalls, null, null, null, null)!!
            c.moveToFirst()
            logs.add(c.columnNames.toList())
            while (c.moveToNext()) {
                val currentRow: MutableList<Any?> = mutableListOf()
                for (i in 0 until c.columnCount) {
                    val objType = c.getType(i)
                    currentRow.add(when (objType) {
                        Cursor.FIELD_TYPE_NULL -> null
                        Cursor.FIELD_TYPE_BLOB -> c.getBlob(i)
                        Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                        Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                        Cursor.FIELD_TYPE_STRING -> c.getString(i)
                        else -> null
                    })
                }
                logs.add(currentRow)
            }
            c.close()
        }
        catch (e: IndexOutOfBoundsException) {
            Toast.makeText(applicationContext, getString(R.string.export_failed_no_logs), Toast.LENGTH_LONG).show()
        }

        // Save logs
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/serialized"
            putExtra(Intent.EXTRA_TITLE, "Call Logs ${SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)}.ser")
        }
        startActivityForResult(intent, 3)
    }

    @Suppress("UNCHECKED_CAST")
    fun importFromFile(location: Uri) {
        var logsToImport: MutableList<List<Any?>> = mutableListOf()
        contentResolver.openFileDescriptor(location, "r")?.use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use {
                val objStream = ObjectInputStream(it)
                logsToImport = objStream.readObject() as MutableList<List<Any?>>
                objStream.close()
                it.close()
            }
        }
        val contentValuesList: MutableList<ContentValues> = mutableListOf()
        for (rowInd in 1 until logsToImport.size) {
            val newContentValues = ContentValues()
            for (colInd in logsToImport[0].indices) {
                // Ignoring fields import doesn't accept
                if (logsToImport[0][colInd] == "phone_account_hidden") {
                    continue
                }
                // Formatting values to wanted input format
                val valAtInd: Any = logsToImport[rowInd][colInd] ?: continue
                when (valAtInd) {
                    is String -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Int -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Boolean -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Byte -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is ByteArray -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Double -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Float -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Long -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                    is Short -> {
                        newContentValues.put(logsToImport[0][colInd] as String, valAtInd)
                    }
                }
            }
            contentValuesList.add(newContentValues)
        }
        applicationContext.contentResolver.bulkInsert(allCalls, contentValuesList.toTypedArray())
        Toast.makeText(applicationContext, getString(R.string.import_success), Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val selectedfile: Uri? = data?.data // URI Location
            importFromFile(selectedfile!!)
        }
        if (requestCode == 3 && resultCode == RESULT_OK) {
            val resUri: Uri = data!!.data!!
            try {
                contentResolver.openFileDescriptor(resUri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use {
                        val outStream= ObjectOutputStream(it)
                        outStream.writeObject(logs)
                        outStream.close()
                        it.close()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Toast.makeText(this, getString(R.string.export_success), Toast.LENGTH_LONG).show()
        }
    }

    fun openGitHub(v: View) {
        val uri = Uri.parse("")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}