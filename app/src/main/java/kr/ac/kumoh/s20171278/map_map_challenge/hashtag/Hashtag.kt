package kr.ac.kumoh.s20171278.map_map_challenge.hashtag

import android.content.Context
import android.content.Intent
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SearchActivity

class Hashtag(ctx:Context):ClickableSpan() {
    private var mClickEventListener : ClickEventListener? = null
    lateinit private var context:Context
    lateinit private var textPaint:TextPaint

    interface ClickEventListener {
        fun onClickEvent(data:String)
    }
    init{
        context = ctx
    }
    fun setOnClickEventListener(listener:ClickEventListener) {
        mClickEventListener = listener
    }
    override fun updateDrawState(ds:TextPaint) {
        textPaint = ds
        ds.setColor(ds.linkColor)
        ds.setARGB(255, 30, 144, 255)
    }
    override fun onClick(widget:View) {
        val tv : TextView = widget as TextView
        val s : Spanned = tv.getText() as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        val theWord:String = s.subSequence(start + 1, end).toString()
    //    Toast.makeText(context, theWord, Toast.LENGTH_LONG).show()
        val intent: Intent = Intent(context, SearchActivity::class.java)
        intent.putExtra("type", theWord)
        intent.putExtra("keyword", 1)
        context.startActivity(intent)
        mClickEventListener?.onClickEvent(theWord)
    }
}