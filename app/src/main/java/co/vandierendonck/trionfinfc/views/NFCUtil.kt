package co.vandierendonck.trionfinfc.views

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException

class NFCUtil {
    companion object {
        fun createNFCMessage(payload: Byte, intent: Intent?) : Boolean {
            val pathPrefix = "vandierendonck.co:trionfinfc"
            val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(), ByteArray(0), arrayOf(payload).toByteArray())
            val nfcMessage = NdefMessage(arrayOf(nfcRecord))
            intent?.let {
                val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                return writeMessageToTag(nfcMessage, tag)
            }
            return false
        }

        private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {
            try {
                val nDefTag = Ndef.get(tag)

                nDefTag?.let {
                    it.connect()
                    if (it.maxSize < nfcMessage.toByteArray().size) {
                        //Message to large to write to NFC tag
                        return false
                    }
                    if (it.isWritable) {
                        it.writeNdefMessage(nfcMessage)
                        it.close()
                        //Message is written to tag
                        return true
                    } else {
                        //NFC tag is read-only
                        return false
                    }
                }

                val nDefFormatableTag = NdefFormatable.get(tag)

                nDefFormatableTag?.let {
                    try {
                        it.connect()
                        it.format(nfcMessage)
                        it.close()
                        //The data is written to the tag
                        return true
                    } catch (e: IOException) {
                        //Failed to format tag
                        return false
                    }
                }
                //NDEF is not supported
                return false
            } catch (e: Exception) {
                //Write operation has failed
            }
            return false
        }

        private fun getNDefMessages(intent: Intent): Array<NdefMessage> {
            val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            rawMessage?.let {
                return rawMessage.map {
                    it as NdefMessage
                }.toTypedArray()
            }
            // Unknown tag type
            val empty = byteArrayOf()
            val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
            val msg = NdefMessage(arrayOf(record))
            return arrayOf(msg)
        }

        fun retrieveNFCMessage(intent: Intent?): Byte {
            intent?.let {
                if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
                    val nDefMessages = getNDefMessages(intent)
                    nDefMessages[0].records?.let {
                        it.forEach {
                            it?.payload.let {
                                it?.let { return it[0] }
                            }
                        }
                    }
                }
            }
            return 0xFF.toByte()
        }

        fun <T>enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType : Class<T>) {
            val pendingIntent = PendingIntent.getActivity(activity, 0,
                    Intent(activity,classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
            val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            val filters = arrayOf(nfcIntentFilter)

            val techLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
        }

        fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
            nfcAdapter.disableForegroundDispatch(activity)
        }
    }
}