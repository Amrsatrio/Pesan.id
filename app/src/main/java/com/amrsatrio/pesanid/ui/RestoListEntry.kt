package com.amrsatrio.pesanid.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.amrsatrio.pesanid.HomeFragmentDirections
import com.amrsatrio.pesanid.R
import com.amrsatrio.pesanid.Resto
import com.amrsatrio.pesanid.databinding.RestoListEntryBinding

class RestoListEntry @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
	private lateinit var binding: RestoListEntryBinding

	override fun onFinishInflate() {
		super.onFinishInflate()
		binding = RestoListEntryBinding.bind(this)
	}

	fun bind(resto: Resto) {
		binding.thumbnail.setImageDrawable(context.getDrawable(resources.getIdentifier(resto.image, "drawable", context.packageName)) ?: ColorDrawable(0xFFFF00FF.toInt()))
		binding.name.text = resto.name
		binding.distance.text = "%.1f km".format(resto.distanceMeters / 1000f)
		binding.rating.text = "%.1f".format(resto.rating)
	}

	companion object {
		fun inflate(inflater: LayoutInflater, root: ViewGroup? = null) =
			inflater.inflate(R.layout.resto_list_entry, root) as RestoListEntry
	}
}