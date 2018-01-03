package co.vandierendonck.trionfinfc.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import co.vandierendonck.trionfinfc.R
import co.vandierendonck.trionfinfc.db.Card
import co.vandierendonck.trionfinfc.db.Rank
import co.vandierendonck.trionfinfc.db.Suit
import co.vandierendonck.trionfinfc.db.Trick
import co.vandierendonck.trionfinfc.models.HandViewModel
import kotlinx.android.synthetic.main.activity_hand.*
import kotlinx.android.synthetic.main.content_hand.*
import kotlinx.android.synthetic.main.manual_card_dialog.view.*
import kotlinx.android.synthetic.main.suit_dialog.view.*
import org.jetbrains.anko.AlertDialogBuilder
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import kotlin.experimental.and


class HandActivity : AppCompatActivity() {
    private lateinit var viewModel: HandViewModel
    private var mNfcAdapter: NfcAdapter? = null
    private var LOCK: Object = Object()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hand)

        val trickId = intent.getLongExtra("trick_id", -1)
        if (trickId == -1L) {
            longToast("Problem while loading hand, returning to game overview")
            onBackPressed()
        }

        viewModel = ViewModelProviders.of(this, HandViewModel.HandListModelFactory(this.application, trickId)).get(HandViewModel::class.java)
        viewModel.getCardList().observe(this@HandActivity, Observer {
            if (it != null) {
                hand_view.addItems(it)

                val trick = viewModel.getTrick().value
                if (trick != null) {
                    var score1 = 0
                    var score2 = 0

                    it.chunked(4).forEach {
                        if (it.count() == 4) {
                            val ret = winnerAndScore(it, trick)
                            val winner = (it[0].playerId + ret.first - 1) % 4 + 1
                            if ((winner - 1) % 2 == 0) {
                                score1 += ret.second
                            } else {
                                score2 += ret.second
                            }
                        }
                    }

                    if (it.count() == 32) {
                        trick.winner = if (score1 >= score2) {
                            0
                        } else {
                            1
                        }
                    } else {
                        trick.winner = -1
                    }

                    viewModel.updateScore(trick, score1, score2)

                    synchronized(LOCK) {
                        LOCK.notify()
                    }
                }
            }
        })
        viewModel.getTrick().observe(this@HandActivity, Observer {
            if (it != null) {
                score_team1.text = it.score1.toString()
                score_team2.text = it.score2.toString()
                when (it.trump) {
                    Suit.HEARTS -> trump.text = "\u2665"
                    Suit.DIAMONDS -> trump.text = "\u2666"
                    Suit.SPADES -> trump.text = "\u2660"
                    Suit.CLUBS -> trump.text = "\u2663"
                    Suit.NONE -> trump.text = "\u00D8"
                    Suit.UNDEFINED -> trump.text = "\uD83E\uDD14"
                }
                when (it.multiplier) {
                    2 -> multiplier_fab.setImageResource(R.drawable.multiplier2)
                    4 -> multiplier_fab.setImageResource(R.drawable.multiplier4)
                    else -> multiplier_fab.setImageResource(R.drawable.multiplier1)
                }
            }
        })

        trump.setOnClickListener {
            viewModel.getCardList().value?.let {
                if (it.count() == 0) {
                    val dB = AlertDialogBuilder(this@HandActivity)
                    val vi = layoutInflater.inflate(R.layout.suit_dialog, null)

                    vi.hearts.setOnClickListener {
                        viewModel.updateTrump(Suit.HEARTS)
                        dB.dismiss()
                    }
                    vi.spades.setOnClickListener {
                        viewModel.updateTrump(Suit.SPADES)
                        dB.dismiss()
                    }
                    vi.diamonds.setOnClickListener {
                        viewModel.updateTrump(Suit.DIAMONDS)
                        dB.dismiss()
                    }
                    vi.clubs.setOnClickListener {
                        viewModel.updateTrump(Suit.CLUBS)
                        dB.dismiss()
                    }
                    vi.no_trump.setOnClickListener {
                        viewModel.updateTrump(Suit.NONE)
                        dB.dismiss()
                    }

                    dB.customView(vi)
                    dB.show()
                }
            }
        }
        multiplier_fab.setOnClickListener {
            viewModel.incrementMultiplier()
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivityForResult(intent, 0)
        } else {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 200)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val data = NFCUtil.retrieveNFCMessage(this.intent)
        extractNFC(data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                if (Settings.System.canWrite(this)) {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                    Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 200)
                }
            }
            else -> {}
        }
    }

    override fun onDestroy() {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        }
        super.onDestroy()
    }

    override fun onPause() {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        }
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
        super.onPause()
    }

    override fun onRestart() {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 200)
        }
        super.onRestart()
    }

    override fun onResume() {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 200)
        }
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            R.id.action_write -> {
                val dB = AlertDialogBuilder(this@HandActivity)
                val vi = layoutInflater.inflate(R.layout.manual_card_dialog, null)

                val suitAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("Spades", "Hearts", "Clubs", "Diamonds"))
                suitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vi.spinner_suit.adapter = suitAdapter

                val rankAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("7", "8", "9", "Jack", "Queen", "King", "Ace", "Manille"))
                rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vi.spinner_rank.adapter = rankAdapter

                val typeAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("Card", "Back"))
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vi.spinner_type.adapter = typeAdapter

                vi.add_button.setOnClickListener {
                    var content = when (vi.spinner_type.selectedItem.toString()) {
                        "Card" -> 0b000
                        "Back" -> 0b001
                        else -> 0b110 // denote error
                    }
                    content = content shl 2
                    content += when (vi.spinner_suit.selectedItem.toString()) {
                        "Spades" -> 0b00
                        "Hearts" -> 0b01
                        "Clubs" -> 0b10
                        else -> 0b11
                    }
                    content = content shl 3
                    content += when (vi.spinner_rank.selectedItem.toString()) {
                        "7" -> 0
                        "8" -> 1
                        "9" -> 2
                        "Jack" -> 3
                        "Queen" -> 4
                        "King" -> 5
                        "Ace" -> 6
                        else -> 7
                    }
                    dB.dismiss()
                    if (content == 0b00111111 && trump.text != "\uD83E\uDD14") {
                        doAsync {
                            val range = ArrayList<Int>(32)
                            (0..31).toCollection(range)
                            range.shuffle()
                            range.forEach { i ->
                                extractNFC(i.toByte())
                                synchronized(LOCK) {
                                    LOCK.wait()
                                }
                            }
                        }
                    } else {
                        extractNFC(content.toByte())
                    }
                }

                dB.customView(vi)
                dB.show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun winnerAndScore(it: List<Card>, trick: Trick): Pair<Int, Int> {
        var tableScore = 0
        var winnerOffset = 0
        it.forEachWithIndex { index, card ->
            tableScore += card.getScore()
            if (card.suit == it[winnerOffset].suit && card.rank > it[winnerOffset].rank) {
                winnerOffset = index
            } else if (card.suit == trick.trump && it[winnerOffset].suit != trick.trump) {
                winnerOffset = index
            }
        }
        return Pair(winnerOffset, tableScore)
    }

    private fun extractNFC(data: Byte) {
        viewModel.getCardList().value?.let {
            if (it.count() == 32) {
                flashSides(R.color.colorAccent)
                return
            }
        }
        val type = (data and 0xE0.toByte()).toInt() shr 5
        val suit = (data and 0x18.toByte()).toInt() shr 3
        val rank = (data and 0x07.toByte()).toInt()
        val cards = viewModel.getCardList().value
        if (cards != null) {
            when (type) {
                0 -> {
                    val lastCard: Card? = try {
                        cards.last()
                    } catch (e: NoSuchElementException) {
                        null
                    }
                    var newPlayerId = 0
                    var trickId = 0L
                    when {
                        lastCard == null -> viewModel.getTrick().value?.let {
                            viewModel.getTrick().value?.let {
                                trickId = it.id
                                if (it.trump == Suit.UNDEFINED) {
                                    viewModel.updateTrump(Suit.values()[suit])
                                    return
                                }
                            }
                            newPlayerId = (it.number) % 4 + 1
                        }
                        cards.count() % 4 == 0 ->  {
                            cards.chunked(4).last().let {
                                val trick = viewModel.getTrick().value
                                if (trick != null) {
                                    trickId = trick.id
                                    val ret = winnerAndScore(it, trick)
                                    newPlayerId = (it[0].playerId + ret.first - 1) % 4 + 1
                                }
                            }
                        }
                        else -> {
                            viewModel.getTrick().value?.let {
                                trickId = it.id
                            }
                            newPlayerId = (lastCard.playerId % 4) + 1
                        }
                    }
                    val card = Card(trickId, cards.count(), newPlayerId)

                    card.suit = Suit.values()[suit]
                    card.rank = Rank.values()[rank]

                    viewModel.insertItem(card)

                    flashSides(R.color.green)
                }
                0b001 -> {
                    if (cards.count() > 0) {
                        viewModel.deleteItem(cards.last())
                    } else {
                        viewModel.updateTrump(Suit.UNDEFINED)
                    }

                    flashSides(R.color.green)
                }
                0b110 -> {
                    flashSides(R.color.colorAccent, R.color.green)
                }
                else -> flashSides(R.color.colorAccent)
            }
        } else {
            flashSides(R.color.colorAccent)
        }
    }

    private fun flashSides(resId: Int) {
        doAsync {
            uiThread {
                left_flashing.setBackgroundResource(resId)
                right_flashing.setBackgroundResource(resId)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(R.color.background)
                right_flashing.setBackgroundResource(R.color.background)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(resId)
                right_flashing.setBackgroundResource(resId)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(R.color.background)
                right_flashing.setBackgroundResource(R.color.background)
            }
        }
    }

    private fun flashSides(resId1: Int, resId2: Int) {
        doAsync {
            uiThread {
                left_flashing.setBackgroundResource(resId1)
                right_flashing.setBackgroundResource(resId1)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(R.color.background)
                right_flashing.setBackgroundResource(R.color.background)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(resId2)
                right_flashing.setBackgroundResource(resId2)
            }
            SystemClock.sleep(200)
            uiThread {
                left_flashing.setBackgroundResource(R.color.background)
                right_flashing.setBackgroundResource(R.color.background)
            }
        }
    }

}
