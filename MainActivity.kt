package com.agrawalmedical.expiry

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

data class Medicine(val id: Long, val name: String, val batch: String, val qty: Int, val expiry: String)

class MedicineDb(c: Context) : SQLiteOpenHelper(c, "medicines.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE medicines(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, batch TEXT, qty INTEGER, expiry TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(name: String, batch: String, qty: Int, expiry: String) {
        writableDatabase.execSQL("INSERT INTO medicines(name,batch,qty,expiry) VALUES(?,?,?,?)", arrayOf(name,batch,qty,expiry))
    }
    fun all(): List<Medicine> {
        val out = mutableListOf<Medicine>()
        readableDatabase.rawQuery("SELECT id,name,batch,qty,expiry FROM medicines ORDER BY expiry", null).use { c ->
            while (c.moveToNext()) out.add(Medicine(c.getLong(0),c.getString(1),c.getString(2),c.getInt(3),c.getString(4)))
        }
        return out
    }
    fun delete(id: Long) { writableDatabase.delete("medicines","id=?", arrayOf(id.toString())) }
}

class MainActivity : Activity() {
    private lateinit var db: MedicineDb
    private lateinit var list: LinearLayout
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = MedicineDb(this)
        createChannel()
        askNotificationPermission()
        scheduleReminder()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20,20,20,20) }
        root.addView(TextView(this).apply { text = "Agrawal Medical"; textSize = 26f; setTypeface(null, Typeface.BOLD) })
        root.addView(TextView(this).apply { text = "दवाइयों की एक्सपायरी ट्रैकिंग"; textSize = 15f; setPadding(0,4,0,16) })
        val name = field("दवाई का नाम")
        val batch = field("बैच नंबर")
        val qty = field("मात्रा")
        val expiry = field("एक्सपायरी तारीख (YYYY-MM-DD)")
        root.addView(name); root.addView(batch); root.addView(qty); root.addView(expiry)
        root.addView(Button(this).apply {
            text = "दवाई सेव करें"
            setOnClickListener {
                val n = name.text.toString().trim()
                val e = expiry.text.toString().trim()
                val q = qty.text.toString().toIntOrNull()
                val valid = try { fmt.isLenient = false; fmt.parse(e) != null } catch (_: Exception) { false }
                if (n.isEmpty() || !valid || q == null || q < 0) {
                    Toast.makeText(this@MainActivity, "नाम, सही तारीख और मात्रा भरें", Toast.LENGTH_LONG).show()
                } else {
                    db.add(n,batch.text.toString(),q,e)
                    name.setText(""); batch.setText(""); qty.setText(""); expiry.setText("")
                    render()
                    Toast.makeText(this@MainActivity, "दवाई सेव हो गई", Toast.LENGTH_SHORT).show()
                }
            }
        })
        root.addView(TextView(this).apply { text = "दवाई की सूची"; textSize = 20f; setTypeface(null, Typeface.BOLD); setPadding(0,18,0,8) })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this); scroll.addView(list); root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
        render()
    }
    private fun field(hintText: String) = EditText(this).apply { hint = hintText; textSize = 16f }
    private fun render() {
        list.removeAllViews()
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }
        db.all().forEach { m ->
            val days = try {
                val d = Calendar.getInstance().apply { time = fmt.parse(m.expiry)!! }
                ((d.timeInMillis - today.timeInMillis) / 86400000L).toInt()
            } catch (_: Exception) { 99999 }
            val status = when { days < 0 -> "एक्सपायर हो चुकी"; days <= 7 -> "7 दिन के अंदर"; days <= 30 -> "30 दिन के अंदर"; days <= 90 -> "90 दिन के अंदर"; else -> "अभी ठीक है" }
            val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0,10,0,10) }
            row.addView(TextView(this).apply { text = "${m.name}  |  मात्रा: ${m.qty}"; textSize = 17f; setTypeface(null,Typeface.BOLD) })
            row.addView(TextView(this).apply { text = "बैच: ${m.batch}  |  एक्सपायरी: ${m.expiry}\nस्थिति: $status"; textSize = 14f })
            row.addView(Button(this).apply { text = "हटाएँ"; setOnClickListener { db.delete(m.id); render() } })
            list.addView(row)
            list.addView(View(this).apply { setBackgroundColor(0xFFDDDDDD.toInt()) }, LinearLayout.LayoutParams(-1,1))
        }
        if (db.all().isEmpty()) list.addView(TextView(this).apply { text = "अभी कोई दवाई नहीं जोड़ी गई।"; textSize = 15f })
    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("expiry_alerts","Medicine expiry alerts",NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
    }
    private fun scheduleReminder() {
        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 1); set(Calendar.SECOND,0) }
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }
}
