package com.amrsatrio.pesanid

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.postDelayed
import androidx.core.view.ScrollingView
import androidx.core.view.setMargins
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.amrsatrio.pesanid.databinding.*
import com.amrsatrio.pesanid.ui.QuantityPicker
import com.amrsatrio.pesanid.ui.RestoListEntry
import com.amrsatrio.util.StringUtil
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.internal.ViewUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.text.DateFormat
import java.util.*

abstract class SkeletonFragment<T : ViewBinding> : Fragment() {
	protected var _binding: Pair<FragmentSkeletonBinding, T>? = null
	protected val skeleton get() = _binding!!.first
	protected val content get() = _binding!!.second

	abstract fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean): T

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val skeleton = FragmentSkeletonBinding.inflate(inflater, container, false)
		val content = inflateContent(inflater, skeleton.root, false)
		var toAdd = content.root
		if (toAdd !is ScrollingView) {
			toAdd = NestedScrollView(toAdd.context).apply {
				isFillViewport = true
				addView(toAdd, -1, -2)
			}
		}
		skeleton.root.addView(toAdd, CoordinatorLayout.LayoutParams(-1, -1).apply {
			behavior = ScrollingViewBehavior()
		})
		_binding = skeleton to content
		return skeleton.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	protected fun normalAppBar() {
		skeleton.collapsingToolbar.apply {
			val ta = context.obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.actionBarSize))
			layoutParams.height = ta.getDimension(0, 0f).toInt()
			ta.recycle()
			isTitleEnabled = false
		}
	}

	protected fun homeAsUp() {
		skeleton.toolbar.apply {
			val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
			navigationIcon = typedArray.getDrawable(0)
			typedArray.recycle()
			setNavigationOnClickListener { findNavController().popBackStack() }
		}
	}

	protected var title: CharSequence
		get() = skeleton.toolbar.title
		set(value) {
			skeleton.toolbar.title = value
		}
}

class HomeFragment : SkeletonFragment<FragmentHomeBinding>() {
	override fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) =
		FragmentHomeBinding.inflate(inflater, container, attachToRoot)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		title = "Beranda"

		for (item in restos.values) {
			content.list.addView(RestoListEntry.inflate(layoutInflater).apply {
				bind(item)
				setOnClickListener {
					findNavController().navigate(HomeFragmentDirections.toResto(item.id))
				}
			}, -1, -2)
		}
	}
}

class RestoFragment : SkeletonFragment<FragmentRestoBinding>() {
	val args by navArgs<RestoFragmentArgs>()
	lateinit var resto: Resto
	private var chosenTable = ""
		set(value) {
			field = value
			content.chosenTableName.text = "Meja $value"
			updateOrderVisibility()
		}
	private val itemsToOrder = mutableListOf<OrderItem>()

	// Views
	private lateinit var fab: View

	override fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) =
		FragmentRestoBinding.inflate(inflater, container, attachToRoot)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		resto = restos[args.resto]!!

		// Order FAB
		fab = ExtendedFloatingActionButton(view.context).apply {
			icon = context.getDrawable(R.drawable.ic_chevron_right_24dp)
			text = "Order"
			setOnClickListener {
				val orderId = "F%08d".format(++orderCnt)
				val newOrder = Order(orderId, resto, Date(), Order.Status.PENDING, chosenTable, itemsToOrder)
				orders[orderId] = newOrder
				findNavController().navigate(RestoFragmentDirections.toBooking(orderId))
			}
		}
		skeleton.root.addView(fab, CoordinatorLayout.LayoutParams(-2, -2).apply {
			gravity = Gravity.BOTTOM or Gravity.END
			setMargins(ViewUtils.dpToPx(requireContext(), 16).toInt())
		})

		normalAppBar()
		homeAsUp()
		content.txtRestoName.text = resto.name
		val ctx = requireContext()
		content.thumbnail.setImageDrawable(ctx.getDrawable(resources.getIdentifier(resto.image, "drawable", ctx.packageName)) ?: ColorDrawable(0xFFFF00FF.toInt()))
		content.txtRating.text = "%.1f".format(resto.rating)
		content.txtDistance.text = "%.1f km".format(resto.distanceMeters / 1000f)

		content.btnTableChoose.setOnClickListener { showTablePicker() }

		// Populate menu
		val restoMenu = dummyMenu
		val categories = mutableMapOf<String, MutableList<RestoMenuItem>>()
		for (category in restoMenu.categories.values) {
			categories[category.id] = mutableListOf()
		}

		for (item in restoMenu.items.values) {
			categories[item.categoryId]?.add(item)
		}

		content.list.removeAllViews()
		for ((categoryId, menus) in categories) {
			content.list.addView(EntryRestoMenuHeaderBinding.inflate(layoutInflater, content.list, false).apply {
				root.text = restoMenu.categories[categoryId]?.name ?: "???"
			}.root)
			val ll = LinearLayout(context).apply {
				dividerDrawable = context.getDrawable(R.drawable.divider_horizontal)
				orientation = LinearLayout.VERTICAL
				showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
				content.list.addView(this, -1, -2)
			}
			for (menu in menus) {
				ll.addView(EntryRestoMenuBinding.inflate(layoutInflater, ll, false).apply {
					root.binding.qtyPicker.root.onQuantityChangedListener = QuantityPicker.OnQuantityChangedListener {
						updateOrderVisibility()
					}
					root.bind(menu)
					root.tag = menu.id
				}.root)
			}
		}

		updateOrderVisibility()
	}

	private fun showTablePicker() {
		val sheet = BottomSheetDialog(requireContext())
		val binding = ModalTablePickerBinding.inflate(layoutInflater, null, false)
		binding.list.apply {
			removeAllViews()
			for (table in dummyTables) {
				addView(EntryTableBinding.inflate(layoutInflater, this, false).apply {
					tableNumber.text = table.name
					colorBox.setBackgroundColor(if (table.available) 0xFF5DEA2C.toInt() else 0xFFFF0000.toInt())
					status.text = if (table.available) "Tersedia" else "Tidak Tersedia"
					root.isEnabled = table.available
					root.setOnClickListener {
						chosenTable = table.id
						sheet.dismiss()
					}
				}.root)
			}
		}
		sheet.setContentView(binding.root)
		sheet.show()
	}

	private fun updateOrderVisibility() {
		itemsToOrder.clear()

		// walk the list
		for (i in 0 until content.list.childCount) {
			val child = content.list.getChildAt(i)
			if (child is LinearLayout) {
				for (j in 0 until child.childCount) {
					val item = child.getChildAt(j) as ViewGroup
					if (item.tag != null) {
						val qty = item.findViewById<QuantityPicker>(R.id.qtyPicker).quantity
						if (qty > 0) {
							itemsToOrder.add(OrderItem(dummyMenuItems[item.tag as String]!!, qty, ""))
						}
					}
				}
			}
		}

		fab.visibility = if (itemsToOrder.isNotEmpty() && chosenTable.isNotEmpty()) View.VISIBLE else View.GONE
	}
}

class BookingFragment : SkeletonFragment<FragmentOrderBinding>() {
	override fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) =
		FragmentOrderBinding.inflate(inflater, container, attachToRoot)

	private val args by navArgs<BookingFragmentArgs>()
	private val handler = Handler(Looper.getMainLooper())
	private lateinit var order: Order
	private var waitingStartTime = 0L
	private var waitingExpirationTime = 0L

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		normalAppBar()
		skeleton.appBar.visibility = View.GONE
		homeAsUp()
		order = orders[args.order]!!
		setQrCode()
		content.orderId.text = order.id
		content.btnCta.setOnClickListener { findNavController().popBackStack() }
		content.timestamp.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(order.date)

		update()
		handler.postDelayed(5000L, this) {
			order.status = Order.Status.CONFIRMED
			waitingStartTime = System.currentTimeMillis()
			waitingExpirationTime = waitingStartTime + (15L * 60L * 1000L)
			update()
			handler.postDelayed(20000L, this) {
				order.status = Order.Status.COMPLETED
				update()
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		handler.removeCallbacksAndMessages(this)
	}

	private fun update() {
		// Status
		when (order.status) {
			Order.Status.CANCELLED -> {
				content.statusTitle.text = "Dibatalkan :("
				content.statusSubtitle.text = "Pesanan ini telah dibatalkan."
				content.icon.setImageResource(com.google.android.material.R.drawable.mtrl_ic_cancel)
			}

			Order.Status.PENDING -> {
				content.statusTitle.text = "Menunggu konfirmasi"
				content.statusSubtitle.text = "Pesanan ini sedang menunggu konfirmasi dari resto."
				content.icon.setImageResource(R.drawable.ic_pending_24)
			}

			Order.Status.CONFIRMED -> {
				content.statusTitle.text = "Meja Anda sudah siap!"
				content.statusSubtitle.text = "Silahkan menuju ke restoran."
				content.icon.setImageResource(R.drawable.ic_fastfood_24)
			}

			Order.Status.COMPLETED -> {
				content.statusTitle.text = "Pesanan Anda telah selesai!"
				content.statusSubtitle.text = "Terima kasih telah menggunakan Pesan.id!"
				content.icon.setImageResource(R.drawable.ic_check_24)
			}
		}
		content.orderDetails.visibility = if (order.status == Order.Status.CONFIRMED) {
			content.tableNumber.text = order.tableId
			content.timeRemainingText.setTextSupplier {
				content.timeRemainingProgress.apply {
					max = ((waitingExpirationTime - waitingStartTime) / 1000L).toInt()
					progress = ((waitingExpirationTime - System.currentTimeMillis()) / 1000L).toInt()
				}
				StringUtil.formatElapsedTime(requireContext(), (waitingExpirationTime - System.currentTimeMillis()).toDouble(), true)
			}
			View.VISIBLE
		} else View.GONE
	}

	private fun setQrCode() {
		val qrcodeSize = ViewUtils.dpToPx(requireContext(), 96).toInt()
		val bmp = encodeQrCode(order.id, qrcodeSize)
		content.imgQr.setImageBitmap(bmp)
	}

	fun encodeQrCode(contents: String, size: Int): Bitmap? {
		val hints = hashMapOf<EncodeHintType, Any>()
		if (!isIso88591(contents)) {
			hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8.name()
		}
		val qrBits = MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, size, size, hints)
		val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
		for (x in 0 until size) {
			for (y in 0 until size) {
				bitmap.setPixel(x, y, if (qrBits[x, y]) Color.BLACK else Color.WHITE)
			}
		}
		return bitmap
	}

	private fun isIso88591(contents: String): Boolean {
		val encoder = Charsets.ISO_8859_1.newEncoder()
		return encoder.canEncode(contents)
	}
}

class OrderHistoryFragment : SkeletonFragment<FragmentOrderHistoryBinding>() {
	override fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) =
		FragmentOrderHistoryBinding.inflate(inflater, container, attachToRoot)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		title = "Pemesanan"
		content.list.adapter = Adapter()
		content.list.layoutManager = LinearLayoutManager(requireContext())
		content.list.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
	}

	private inner class Adapter : RecyclerView.Adapter<OrderViewHolder>() {
		private val items = orders.values.sortedByDescending { it.date }

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
			return OrderViewHolder(EntryOrderHistoryBinding.inflate(layoutInflater))
		}

		override fun getItemCount(): Int {
			return items.size
		}

		override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
			holder.bind(items[position])
		}
	}

	private class OrderViewHolder(val binding: EntryOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
		fun bind(item: Order) {
			binding.apply {
				title.text = item.resto.name
				line1.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(item.date)
//				txtOrderStatus.text = item.status
			}
		}
	}
}

class ProfileFragment : SkeletonFragment<FragmentProfileBinding>() {
	override fun inflateContent(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) =
		FragmentProfileBinding.inflate(inflater, container, attachToRoot)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		title = "Profil"
	}
}

class Resto(val id: String, val name: String, val image: String, val distanceMeters: Int, val rating: Float)

class RestoTable(val id: String, val name: String, val available: Boolean)

class RestoMenu(val restoId: String, val categories: Map<String, RestoMenuCategory>, val items: Map<String, RestoMenuItem>)

class RestoMenuCategory(val id: String, val name: String, val sortOrder: Int)

class RestoMenuItem(val id: String, val name: String, val image: String, val categoryId: String)

class Order(val id: String, val resto: Resto, val date: Date, var status: Status, val tableId: String, val items: List<OrderItem>) {
	enum class Status {
		CANCELLED, PENDING, CONFIRMED, COMPLETED
	}
}

class OrderItem(val menuItem: RestoMenuItem, val quantity: Int, val notes: String)

val restos = listOf(
	Resto("1", "Subway", "logo1", 100, 4.8f),
	Resto("2", "McDonalds", "logo2", 300, 5.0f),
	Resto("3", "Solaria", "logo3", 500, 4.8f),
	Resto("4", "Bakmi GM", "logo4", 700, 4.7f),
	Resto("5", "Ichiban Sushi", "logo5", 900, 4.6f),
	Resto("6", "Sederhana", "logo6", 1200, 4.5f),
	Resto("7", "Es Teler 77", "logo7", 1400, 4.7f),
).associateBy { it.id }

val dummyTables = listOf(
	RestoTable("1", "1", true),
	RestoTable("2", "2", true),
	RestoTable("3", "3", false),
	RestoTable("4", "4", false),
	RestoTable("5", "5", false),
	RestoTable("6", "6", true),
)

val dummyMenuCategories = mutableListOf(
	RestoMenuCategory("food", "Makanan", 1),
	RestoMenuCategory("drink", "Minuman", 2),
	RestoMenuCategory("utility", "Alat Makan & Utilitas", 3),
).associateBy { it.id }

val dummyMenuItems = mutableListOf(
	RestoMenuItem("1", "Steak", "makanan1", "food"),
	RestoMenuItem("2", "Nasi Goreng", "makanan2", "food"),
	RestoMenuItem("3", "Paket Burger", "makanan3", "food"),
	RestoMenuItem("4", "Boba Tea", "minuman1", "drink"),
	RestoMenuItem("5", "Cappuccino", "minuman2", "drink"),
	RestoMenuItem("6", "Es Lemon Tea", "minuman3", "drink"),
	RestoMenuItem("7", "Kursi Bayi", "peralatan1", "utility"),
	RestoMenuItem("8", "Mangkuk", "peralatan2", "utility"),
	RestoMenuItem("9", "Sendok & Garpu", "peralatan3", "utility"),
).associateBy { it.id }

val dummyMenu = RestoMenu("1", dummyMenuCategories, dummyMenuItems)

var orderCnt = 0

val orders = mutableMapOf(
	"dummy" to Order("dummy", restos["1"]!!, Date(), Order.Status.COMPLETED, "4", emptyList()),
)