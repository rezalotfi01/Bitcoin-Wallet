package com.github.rezalotfi01.bwallet.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.rezalotfi01.bwallet.R
import com.github.rezalotfi01.bwallet.data.db.WalletRecord
import com.github.rezalotfi01.bwallet.di.ViewModelFactory
import com.github.rezalotfi01.bwallet.ui.edit.EditDialog
import com.github.rezalotfi01.bwallet.ui.edit.EditDialogInterface
import com.github.rezalotfi01.bwallet.utils.DaggerLifecycleFragment
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.main_fragment.view.*
import javax.inject.Inject


abstract class MainFragment : DaggerLifecycleFragment(), EditDialogInterface {

    @Inject lateinit var viewModelFactory: ViewModelFactory
    private lateinit var _thisView:View
    protected lateinit var viewModel: MainViewModel

    abstract fun getViewModelClass():Class<out ViewModel>

    companion object {
        val TAG = MainFragment::class.java.simpleName
    }

    override fun onAttach(context: Context?) {
        //DI activity injection first
        AndroidInjection.inject(activity)
        super.onAttach(context)
    }

    private lateinit var adapter: MainListAdapter

    abstract fun getEmptyText():CharSequence

    protected fun showUndoSnackbar(deletedRecord: WalletRecord, undoText:String) {
        val snackbar = Snackbar.make(_thisView, undoText, Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.undo), {
            viewModel.addRecord(deletedRecord)
        })
        snackbar.view.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSnackbar,null))
        snackbar.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _thisView = inflater.inflate(R.layout.main_fragment, container, false)

        _thisView.emptyListText.text = getEmptyText()
        _thisView.textSpannableInfo.text = getSpannableInfo(_thisView.context)

        return _thisView
    }

    private fun getSpannableInfo(context: Context): SpannableString {
        val text = SpannableString(getString(R.string.first_instruction))
        val addIcon = ContextCompat.getDrawable(context,R.drawable.ic_add_white_24dp)
        val favIcon= ContextCompat.getDrawable(context,R.drawable.ic_favorite_white_24dp)

        addIcon?.setBounds(0, 0, (addIcon.intrinsicWidth * 0.9f).toInt(), (addIcon.intrinsicHeight * 0.9f).toInt())
        favIcon?.setBounds(0,0, (favIcon.intrinsicWidth * 0.9f).toInt(), (favIcon.intrinsicHeight * 0.9f).toInt())
        val spanAdd = ImageSpan(addIcon, ImageSpan.ALIGN_BOTTOM)
        val spanFav = ImageSpan(favIcon,ImageSpan.ALIGN_BOTTOM)
        val pAdd = text.indexOf('+')
        val pFav = text.indexOf('*')
        text.setSpan(spanAdd, pAdd, pAdd+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        text.setSpan(spanFav, pFav, pFav+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        return text
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass()) as MainViewModel

        adapter = MainListAdapter(context!!, viewModel, this)

        //Fragment Lifecycle Hack from https://medium.com/@BladeCoder/architecture-components-pitfalls-part-1-9300dd969808
        val thisLifecycleOwner = getViewLifecycleOwner()
        if (thisLifecycleOwner != null) {

            viewModel.deletedItem.observe(thisLifecycleOwner, Observer deletedItemObserver@{
                deletedRecord ->
                    if (deletedRecord  == null) return@deletedItemObserver
                    showUndoSnackbar(deletedRecord, getString(R.string.address_deleted))
            })

            viewModel.data.observe(thisLifecycleOwner, Observer<List<WalletRecord>> dbObserver@{

                if (it == null || it.isEmpty()) {
                    _thisView.layoutEmptyList.visibility = View.VISIBLE
                } else {
                    _thisView.layoutEmptyList.visibility = View.GONE
                }

                adapter.submitList(it)
            })

        }

        val recyclerView = _thisView.recyclerViewMain

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter
        adapter.submitList(null)

        val mDividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(mDividerItemDecoration)
    }

    override fun showEditDialog(address:String, nickname:String) {
        EditDialog.newInstance(address,nickname).show(fragmentManager, EditDialog.TAG)
    }
}
