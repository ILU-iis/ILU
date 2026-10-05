package com.iluiis.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iluiis.app.api.ChatMessage
import com.iluiis.app.api.OpenRouterRepository
import com.iluiis.app.database.AppDatabase
import com.iluiis.app.database.ChatMessageEntity
import com.iluiis.app.database.GratitudeEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ChatActivity"
    }

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ChatAdapter
    lateinit var messageList: ArrayList<Message>
    lateinit var sharedPreferences: SharedPreferences
    lateinit var database: AppDatabase

    // Flag apakah hari ini sudah isi gratitude
    private var gratitudeSavedToday = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Fix keyboard covering input bar on modern Android (API 30+)
        val rootLayout = findViewById<LinearLayout>(R.id.rootChatLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, maxOf(imeInsets.bottom, navInsets.bottom))
            insets
        }

        sharedPreferences = getSharedPreferences("ILU", MODE_PRIVATE)
        database = AppDatabase.getDatabase(this)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)
        val btnRecap = findViewById<TextView>(R.id.btnRecap)
        val layoutTypingIndicator = findViewById<View>(R.id.layoutTypingIndicator)

        // Mascot Header Bobbing Animation (Floating up & down smoothly)
        findViewById<android.widget.ImageView>(R.id.ivMascotHeader)?.let { mascot ->
            val bobbingAnimator = android.animation.ObjectAnimator.ofFloat(mascot, "translationY", 0f, -6f, 0f).apply {
                duration = 2000
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        val username = sharedPreferences.getString("username", null)

        // Cek tanggal hari ini
        val todayKey = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        val todayFormatted = java.text.SimpleDateFormat(
            "dd MMMM yyyy",
            java.util.Locale("id", "ID")
        ).format(java.util.Date())

        val lastDate = sharedPreferences.getString("last_gratitude_date", "")
        gratitudeSavedToday = (todayKey == lastDate)

        tvWelcome.text = "ILU"

        // Setup RecyclerView
        messageList = ArrayList()
        adapter = ChatAdapter(messageList)
        recyclerView = findViewById(R.id.recyclerChat)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Sinkronisasi DB & Load riwayat chat hari ini (jika ada)
        lifecycleScope.launch {
            val (hasTodayGratitudeRecord, todayChatList) = withContext(Dispatchers.IO) {
                try {
                    val allGratitude = database.gratitudeDao().getAll()
                    val hasRecord = allGratitude.any { it.date == todayFormatted }
                    val messages = database.chatMessageDao().getMessagesByDate(todayKey)
                    Pair(hasRecord, messages)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal membaca database saat inisialisasi chat", e)
                    Pair(false, emptyList<ChatMessageEntity>())
                }
            }

            if (gratitudeSavedToday && !hasTodayGratitudeRecord) {
                Log.w(TAG, "SharedPreferences mencatat sudah isi hari ini, tetapi Room DB kosong. Mereset gratitudeSavedToday menjadi false.")
                gratitudeSavedToday = false
            }

            if (todayChatList.isNotEmpty()) {
                Log.d(TAG, "Ditemukan ${todayChatList.size} pesan chat hari ini ($todayKey) di DB. Memuat pesan...")
                for (entity in todayChatList) {
                    messageList.add(Message(entity.text, entity.isUser))
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            } else {
                Log.d(TAG, "Belum ada chat hari ini ($todayKey). Memulai sapaan awal AI...")
                startAiOpeningGreeting(username, layoutTypingIndicator, btnSend, todayKey)
            }
        }

        // Interactive Button Press Scale Animation
        btnSend.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()
                }
            }
            false
        }

        // Tombol kirim pesan
        btnSend.setOnClickListener {

            val text = etMessage.text.toString().trim()

            if (text.isNotEmpty()) {

                messageList.add(Message(text, true))
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)

                etMessage.text.clear()

                // 1. Simpan pesan user ke database Room lokal
                saveChatMessageToDb(text, isUser = true, date = todayKey)

                // 2. Lapisan 1: Deteksi Krisis Lokal (CrisisDetector)
                if (CrisisDetector.isCrisis(text)) {
                    Log.w(TAG, "Deteksi krisis lokal terpicu oleh pesan user! Mengirim respons krisis & mencatat log.")

                    val periodId = PeriodManager.getCurrentPeriodId(this, username)
                    val triggerKeyword = CrisisDetector.getTriggerKeyword(text)
                    CrisisLogger.logCrisisEvent(detectedBy = "keyword", triggerKeyword = triggerKeyword, username = username, periodId = periodId)

                    val crisisReply = CrisisDetector.CRISIS_RESPONSE
                    messageList.add(Message(crisisReply, false))
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)

                    // Simpan balasan krisis ke DB lokal
                    saveChatMessageToDb(crisisReply, isUser = false, date = todayKey)
                    return@setOnClickListener
                }

                // 3. Jika bukan krisis, kirim ke AI via API
                lifecycleScope.launch {
                    layoutTypingIndicator.visibility = View.VISIBLE
                    btnSend.isEnabled = false

                    // Konversi riwayat chat ke format ChatMessage untuk konteks AI
                    // Batasi history maksimal 10 pesan terakhir agar context window bersih
                    val chatHistory = messageList.dropLast(1).takeLast(10).map { msg ->
                        ChatMessage(
                            role = if (msg.isUser) "user" else "assistant",
                            content = msg.text
                        )
                    }

                    val reply = withContext(Dispatchers.IO) {
                        OpenRouterRepository.sendMessage(text, chatHistory)
                    }

                    layoutTypingIndicator.visibility = View.GONE
                    btnSend.isEnabled = true

                    val cleanReply = processAiReply(reply)

                    // Lapisan 2: Cek jika AI juga mendeteksi krisis dari respons
                    if (cleanReply.contains("+62 877 0324 4632") || cleanReply.contains("psikiater")) {
                        Log.w(TAG, "AI membalas dengan nomor psikiater. Mencatat log krisis ke Firebase.")
                        val periodId = PeriodManager.getCurrentPeriodId(this@ChatActivity, username)
                        CrisisLogger.logCrisisEvent(detectedBy = "ai", username = username, periodId = periodId)
                    }

                    messageList.add(Message(cleanReply, false))
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)

                    // Simpan balasan AI ke DB lokal
                    saveChatMessageToDb(cleanReply, isUser = false, date = todayKey)
                }
            }
        }

        // Tombol rekap
        btnRecap.setOnClickListener {
            startActivity(Intent(this, RecapActivity::class.java))
        }
    }

    /**
     * Memulai sapaan pembuka AI pada sesi hari baru
     */
    private fun startAiOpeningGreeting(
        username: String?,
        layoutTypingIndicator: View,
        btnSend: ImageButton,
        todayKey: String
    ) {
        lifecycleScope.launch {
            layoutTypingIndicator.visibility = View.VISIBLE
            btnSend.isEnabled = false

            val gratitudeStatus = if (gratitudeSavedToday)
                "Pengguna SUDAH mengisi gratitude hari ini. Jangan tanyakan 3 hal baik lagi. Langsung ngobrol santai biasa saja."
            else
                "Pengguna BELUM mengisi gratitude hari ini. Jika obrolan berlangsung santai dan harinya terasa baik, SEGERA tanyakan 3 hal baik yang dirasakan hari ini (tanyakan lebih awal setelah 3-5 pesan mengalir, jangan menunda-nunda)."

            val openingPrompt = """
Ini adalah awal percakapan hari ini. Nama pengguna: ${username ?: "Teman"}.
$gratitudeStatus
WAJIB DIPATUHI UNTUK SAPAAN PEMBUKA:
1. Sapa nama pengguna secara hangat, santai, dan lembut seperti sahabat dekat. DILARANG menggunakan kata "lagi" dan DILARANG menggunakan emoji matahari (🌻) pada sapaan awal ini.
2. TANYAKAN DUA HAL UTAMA SECARA MENGALIR:
   - Bagaimana perasaannya hari ini?
   - Apakah ada hal yang ingin diceritakan?
   (Contoh variasi natural: "Halo ${username ?: "Teman"}! Gimana perasaanmu hari ini? Ada yang mau kamu ceritain ke aku?", "Hai ${username ?: "Teman"}, senang bisa ngobrol sama kamu. Gimana keadaan atau perasaanmu hari ini? Ada cerita yang pengen kamu bagi?")
3. Gunakan Bahasa Indonesia santai yang lembut, alami, dan tidak kaku.
""".trimIndent()

            val reply = withContext(Dispatchers.IO) {
                OpenRouterRepository.sendMessage(openingPrompt)
            }

            layoutTypingIndicator.visibility = View.GONE
            btnSend.isEnabled = true

            val cleanReply = processAiReply(reply)
            messageList.add(Message(cleanReply, false))
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(messageList.size - 1)

            // Simpan pesan sapaan AI ke DB lokal
            saveChatMessageToDb(cleanReply, isUser = false, date = todayKey)
        }
    }

    /**
     * Menyimpan satu pesan ke database Room lokal dan Firebase Firestore secara asinkron
     */
    private fun saveChatMessageToDb(text: String, isUser: Boolean, date: String) {
        val username = sharedPreferences.getString("username", null)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                database.chatMessageDao().insertMessage(
                    ChatMessageEntity(
                        date = date,
                        text = text,
                        isUser = isUser
                    )
                )
                Log.d(TAG, "Tersimpan di DB chat lokal: isUser=$isUser, date=$date")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menyimpan pesan chat ke DB lokal", e)
            }
        }

        // Sync ke Firebase Firestore
        val periodId = PeriodManager.getCurrentPeriodId(this, username)
        FirebaseSync.syncChatMessage(date = date, text = text, isUser = isUser, username = username, periodId = periodId)
    }

    /**
     * Memproses balasan AI:
     * - Mendeteksi tag [GRATITUDE_SAVED:hal1|hal2|hal3]
     * - Jika ada tag: simpan hal baik ke database dan SharedPreferences, hapus tag dari teks
     * - Jika tidak ada tag: kembalikan teks asli
     */
    private fun processAiReply(reply: String): String {
        Log.d(TAG, "Memproses balasan AI mentah: $reply")

        var cleaned = reply

        // 1. Coba cari dengan format lengkap ber-kurung siku [GRATITUDE_SAVED: ...]
        val bracketRegex = Regex("""\[GRATITUDE_SAVED:\s*(.+?)\s*\]""", RegexOption.IGNORE_CASE)
        var match = bracketRegex.find(cleaned)

        // 2. Jika tidak ditemukan, coba cari format tanpa kurung siku GRATITUDE_SAVED: ...
        if (match == null) {
            val noBracketRegex = Regex("""GRATITUDE_SAVED:\s*(.+)""", RegexOption.IGNORE_CASE)
            match = noBracketRegex.find(cleaned)
        }

        if (match != null) {
            Log.d(TAG, "Tag GRATITUDE_SAVED ditemukan: ${match.value}")
            if (!gratitudeSavedToday) {
                val gratitudeData = match.groupValues[1].trim()

                // Split berdasarkan pemisah '|', ',', ';', atau newline '\n'
                val rawParts = gratitudeData.split(Regex("""[|,;\n]"""))
                    .map { it.trim().trim(']', '[') }
                    .filter { it.isNotEmpty() }

                if (rawParts.isNotEmpty()) {
                    val good1 = rawParts.getOrElse(0) { "-" }
                    val good2 = rawParts.getOrElse(1) { "-" }
                    val good3 = rawParts.getOrElse(2) { "-" }

                    Log.d(TAG, "Menyimpan gratitude: good1='$good1', good2='$good2', good3='$good3'")
                    saveGratitude(good1, good2, good3)
                    gratitudeSavedToday = true
                } else {
                    Log.w(TAG, "Tag ditemukan tapi isinya kosong setelah diparsing: $gratitudeData")
                }
            } else {
                Log.d(TAG, "Gratitude sudah pernah disimpan hari ini, abaikan eksekusi simpan ulang.")
            }

            // Hapus tag khusus dari balasan
            cleaned = cleaned.replace(match.value, "")
        } else {
            Log.d(TAG, "Tidak ada tag GRATITUDE_SAVED dalam balasan AI.")
        }

        // HAPUS SEMUA SISA THINKING PROCESS, NOTA / CATATAN SYSTEM, DAN KURUNG SIKU AGAR BEBAS DARI KARAKTER UNWANTED:
        cleaned = cleaned.replace(Regex("""(?s)<think>.*?</think>"""), "")
        cleaned = cleaned.replace(Regex("""\[.*?\]"""), "")
        cleaned = cleaned.replace(Regex("""(?i)\(catatan:.*?\)"""), "")
        cleaned = cleaned.replace(Regex("""(?i)\(note:.*?\)"""), "")
        cleaned = cleaned.replace(Regex("""(?i)catatan:.*"""), "")
        cleaned = cleaned.replace("[", "").replace("]", "")

        return cleaned.trim()
    }

    private fun saveGratitude(
        good1: String,
        good2: String,
        good3: String
    ) {

        val username = sharedPreferences.getString("username", null)
        val today = java.text.SimpleDateFormat(
            "dd MMMM yyyy",
            java.util.Locale("id", "ID")
        ).format(java.util.Date())

        val todayKey = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        // Simpan ke SharedPreferences agar Widget bisa sinkron langsung
        val gratitudeList = arrayListOf(good1, good2, good3)
        val gson = Gson()
        sharedPreferences.edit()
            .putString("gratitude", gson.toJson(gratitudeList))
            .putString("last_gratitude_date", todayKey)
            .apply()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    database.gratitudeDao().insert(
                        GratitudeEntity(
                            date = today,
                            good1 = good1,
                            good2 = good2,
                            good3 = good3
                        )
                    )
                    Log.d(TAG, "Berhasil menyimpan GratitudeEntity ke Room Database untuk tanggal: $today")
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal menginsert GratitudeEntity ke Database", e)
                }
            }

            ILUHomeWidget.updateAllWidgets(this@ChatActivity)
        }

        // Sync ke Firebase Firestore
        val periodId = PeriodManager.getCurrentPeriodId(this, username)
        FirebaseSync.syncGratitude(date = today, good1 = good1, good2 = good2, good3 = good3, username = username, periodId = periodId)
    }
}
