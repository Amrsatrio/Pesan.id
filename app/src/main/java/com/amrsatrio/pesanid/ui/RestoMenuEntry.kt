package com.amrsatrio.pesanid.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.amrsatrio.pesanid.R
import com.amrsatrio.pesanid.RestoMenuItem
import com.amrsatrio.pesanid.databinding.EntryRestoMenuBinding

class RestoMenuEntry @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
	lateinit var binding: EntryRestoMenuBinding

	override fun onFinishInflate() {
		super.onFinishInflate()
		binding = EntryRestoMenuBinding.bind(this)
	}

	fun bind(item: RestoMenuItem) {
		binding.thumbnail.setImageDrawable(context.getDrawable(resources.getIdentifier(item.image, "drawable", context.packageName)) ?: ColorDrawable(0xFFFF00FF.toInt()))
		binding.title.text = item.name
		binding.line1.visibility = View.GONE
		binding.price.visibility = View.GONE
	}

	companion object {
		fun inflate(inflater: LayoutInflater, root: ViewGroup? = null) =
			inflater.inflate(R.layout.resto_list_entry, root) as RestoMenuEntry
	}
}