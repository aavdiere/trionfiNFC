package co.vandierendonck.trionfinfc.views

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import co.vandierendonck.trionfinfc.R
import kotlinx.android.synthetic.main.content_nfc_write.*
import org.jetbrains.anko.toast

class NfcWriteActivity : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_write)

        val suitAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("Spades", "Hearts", "Clubs", "Diamonds"))
        suitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_suit.adapter = suitAdapter

        val rankAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("7", "8", "9", "Jack", "Queen", "King", "Ace", "Manille"))
        rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_rank.adapter = rankAdapter

        val typeAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf("Card", "Back"))
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_type.adapter = typeAdapter

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
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
                toast("Not valid here")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var content = when (spinner_type.selectedItem.toString()) {
            "Card" -> 0b000
            "Back" -> 0b001
            else -> 0b110 // denote error
        }
        content = content shl 2
        content += when (spinner_suit.selectedItem.toString()) {
            "Spades" -> 0b00
            "Hearts" -> 0b01
            "Clubs" -> 0b10
            else -> 0b11
        }
        content = content shl 3
        content += when (spinner_rank.selectedItem.toString()) {
            "7" -> 0
            "8" -> 1
            "9" -> 2
            "Jack" -> 3
            "Queen" -> 4
            "King" -> 5
            "Ace" -> 6
            else -> 7
        }
        val messageWrittenSuccessfully = NFCUtil.createNFCMessage(content.toByte(), intent)
        if (messageWrittenSuccessfully) {
            toast("Successfully written " + content.toByte().toString() + " to tag")
            if (spinner_rank.selectedItemPosition == spinner_rank.count - 1) {
                spinner_rank.setSelection(0)
                spinner_suit.setSelection((spinner_suit.selectedItemPosition + 1) % spinner_suit.count)
            } else {
                spinner_rank.setSelection(spinner_rank.selectedItemPosition + 1)
            }
        } else {
            toast("Something went wrong writing " + content.toByte().toString() + " to the tag, try again")
        }
    }
}
