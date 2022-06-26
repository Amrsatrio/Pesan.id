package com.amrsatrio.pesanid.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.amrsatrio.pesanid.R
import com.amrsatrio.pesanid.databinding.QuantityPickerBinding

class QuantityPicker @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
	private lateinit var binding: QuantityPickerBinding
	var onQuantityChangedListener: OnQuantityChangedListener? = null

	var quantity: Int = 0
		set(value) {
			val callListener = field != value
			field = value.coerceIn(min, max)
			update()
			if (callListener) {
				onQuantityChangedListener?.onQuantityChanged(field)
			}
		}

	var min: Int = 0
		set(value) {
			field = value
			quantity = quantity
		}

	var max: Int = 99
		set(value) {
			field = value
			quantity = quantity
		}

	override fun onFinishInflate() {
		super.onFinishInflate()
		binding = QuantityPickerBinding.bind(this)

		binding.btnDecrement.setOnClickListener { quantity -= 1 }
		binding.btnIncrement.setOnClickListener { quantity += 1 }

		update()
	}

	companion object {
		fun inflate(inflater: LayoutInflater, root: ViewGroup? = null) =
			inflater.inflate(R.layout.resto_list_entry, root) as QuantityPicker
	}

	private fun update() {
		binding.txtQuantity.text = quantity.toString()
		binding.btnDecrement.isEnabled = quantity > min
		binding.btnIncrement.isEnabled = quantity < max
	}

	fun interface OnQuantityChangedListener {
		fun onQuantityChanged(quantity: Int)
	}
}