package co.vandierendonck.trionfinfc.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import co.vandierendonck.trionfinfc.R
import co.vandierendonck.trionfinfc.db.Trick
import co.vandierendonck.trionfinfc.models.TrickListViewModel
import com.daimajia.swipe.SwipeLayout
import kotlinx.android.synthetic.main.activity_trick.*
import kotlinx.android.synthetic.main.content_trick.*
import kotlinx.android.synthetic.main.list_row.*
import kotlinx.android.synthetic.main.list_row.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class TrickActivity : AppCompatActivity() {
    private lateinit var viewModel: TrickListViewModel
    private lateinit var listViewAdapter: ListAdapter<Trick>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trick)

        val gameId = intent.getLongExtra("game_id", -1)
        if (gameId == -1L) {
            longToast("Problem while loading trick, returning to game overview")
            onBackPressed()
        }

        listViewAdapter = ListAdapter(this, ArrayList(),
                fillIn = { view: View, trick: Trick, position: Int ->
                    view.multiplier.visibility = View.VISIBLE
                    view.title.text = this@TrickActivity.getString(R.string.trick_list_title, position + 1)
                    view.subtitle.text = if (trick.winner == -1)
                        "In progress..."
                    else
                        "Team " +
                                (if (trick.winner == 0) "1" else "2") +
                                " (" +
                                trick.score1.toString() +
                                " - " +
                                trick.score2.toString() +
                                ")"
                    view.multiplier.text = if (trick.multiplier != 1)
                        "x " + trick.multiplier
                    else ""
                },
                onClickListener = View.OnClickListener {
                    val trick: Trick? = it.tag as Trick?
                    if (swipe_layout.openStatus == SwipeLayout.Status.Close
                            && trick != null) {
                        startActivity<HandActivity>("trick_id" to trick.id)
                    }
                },
                editClickListener = null,
                deleteClickListener = View.OnClickListener {
                    listViewAdapter.closeAllItems()

                    val trick = it.tag as Trick?
                    if (trick != null) {
                        if (trick.number == listViewAdapter.count - 1) {
                            alert {
                                title("Are you sure?")

                                negativeButton(R.string.dialog_negative, { cancel() })
                                positiveButton(R.string.dialog_positive, {
                                    viewModel.deleteItem(trick)
                                })
                            }.show()
                        } else {
                            toast("Only the last trick can be deleted")
                        }
                    } else {
                        toast("Something went wrong while trying to delete the trick")
                    }
                })

        list.adapter = listViewAdapter

        viewModel = ViewModelProviders.of(this, TrickListViewModel.TrickListModelFactory(this.application, gameId)).get(TrickListViewModel::class.java)
        viewModel.getTrickList().observe(this@TrickActivity, Observer {
            if (it != null) {
                listViewAdapter.addItems(it)
                var score1 = 0
                var score2 = 0
                it.forEach {
                    score1 += if (it.winner == 0) (it.score1 - 30) * it.multiplier else 0
                    score2 += if (it.winner == 1) (it.score2 - 30) * it.multiplier else 0
                }

                val game = viewModel.getGame().value
                if (game != null)
                    viewModel.updateScore(game, score1, score2)
            }
        })
        viewModel.getGame().observe(this@TrickActivity, Observer {
            if (it != null) {
                score_team1.text = it.score1.toString()
                score_team2.text = it.score2.toString()
            }
        })

        add_fab.setOnClickListener {
            if (listViewAdapter.last?.winner != -1)
                viewModel.insertItem(Trick(gameId, listViewAdapter.count))
            else
                toast("Cannot create new trick when previous trick is still in progress")
        }
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
                startActivity<NfcWriteActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
