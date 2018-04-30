package co.vandierendonck.trionfinfc.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import co.vandierendonck.trionfinfc.R
import co.vandierendonck.trionfinfc.db.Game
import co.vandierendonck.trionfinfc.models.GameListViewModel
import com.daimajia.swipe.SwipeLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.list_row.*
import kotlinx.android.synthetic.main.list_row.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import android.content.Intent



class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: GameListViewModel
    private lateinit var listViewAdapter: ListAdapter<Game>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        listViewAdapter = ListAdapter(this, ArrayList(),
                fillIn = { view: View, game: Game, _: Int ->
                    view.title.text = game.name
                    view.subtitle.text = this@MainActivity.getString(R.string.game_subtitle, game.score1, game.score2)
                },
                onClickListener = View.OnClickListener {
                    val game: Game? = it.tag as Game?
                    if (swipe_layout.openStatus == SwipeLayout.Status.Close
                            && game != null) {
                        startActivity<TrickActivity>("game_id" to game.id)
                    }
                },
                editClickListener = View.OnClickListener {
                    listViewAdapter.closeAllItems()
                    alert {
                        val editText = EditText(this@MainActivity)
                        editText.requestFocus()

                        title("New name")
                        customView(editText)

                        negativeButton(R.string.dialog_negative, { cancel() })
                        positiveButton(R.string.dialog_positive, {
                            val game = it.tag as Game?

                            if (game != null)
                                viewModel.changeName(game, editText.text.toString())
                            else
                                toast("Name of game could not be changed")
                        })
                    }.show()
                },
                deleteClickListener = View.OnClickListener {
                    listViewAdapter.closeAllItems()
                    alert {
                        title("Are you sure?")

                        negativeButton(R.string.dialog_negative, { cancel() })
                        positiveButton(R.string.dialog_positive, {
                            val game = it.tag as Game?

                            if (game != null)
                                viewModel.deleteItem(game)
                            else
                                toast("Something went wrong while trying to delete the game")
                        })
                    }.show()
                })

        list.adapter = listViewAdapter
        list.emptyView = empty_list

        viewModel = ViewModelProviders.of(this).get(GameListViewModel::class.java)
        viewModel.getGameList().observe(this@MainActivity, Observer {
            if (it != null) listViewAdapter.addItems(it)
        })

        add_fab.setOnClickListener {
            alert {
                val editText = EditText(this@MainActivity)
                editText.requestFocus()

                title("Name of the game")
                customView(editText)

                negativeButton(R.string.dialog_negative, { cancel() })
                positiveButton(R.string.dialog_positive, {
                    viewModel.insertItem(Game(editText.text.toString()))
                })
            }.show()
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
            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                return true
            }
            R.id.action_write -> {
                startActivity<NfcWriteActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
