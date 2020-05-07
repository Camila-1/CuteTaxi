package ua.com.cuteteam.cutetaxi.fragments.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_map_driver.view.*
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.common.prepareDistance
import ua.com.cuteteam.cutetaxi.data.database.DbEntries
import ua.com.cuteteam.cutetaxi.data.entities.Order
import ua.com.cuteteam.cutetaxi.data.entities.OrderStatus
import ua.com.cuteteam.cutetaxi.dialogs.InfoDialog
import ua.com.cuteteam.cutetaxi.dialogs.RateDialog
import ua.com.cuteteam.cutetaxi.repositories.DriverRepository
import ua.com.cuteteam.cutetaxi.viewmodels.DriverViewModel
import ua.com.cuteteam.cutetaxi.viewmodels.viewmodelsfactories.DriverViewModelFactory

private const val TAG = "Cute.DriverFragment"

class DriverMapFragment : Fragment() {

    private val model by lazy {
        ViewModelProvider(requireActivity(), DriverViewModelFactory(DriverRepository()))
            .get(DriverViewModel::class.java)
    }

    private val ratingCallback by lazy {
        object : RateDialog.OnRateCallback {
            override fun onRate(rating: Float, ratingBar: RatingBar) {
                model.rate(rating)
            }
        }
    }

    private val activeOrderObserver = Observer<Order> {
        when (it?.orderStatus) {
            OrderStatus.CANCELLED -> {
                hideUI()
                showCancelDialog()
                model.closeOrder()
            }
            OrderStatus.FINISHED -> {
                hideUI()
                showRateDialog()
                model.closeOrder()
            }
            OrderStatus.ACCEPTED -> {
                showUI()
                fillInfo(it)
            }
            OrderStatus.STARTED -> {
                changeButtons()
                fillInfo(it)
            }
            else -> hideUI()
        }
    }

    override fun onCreateView(
        inflator: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflator.inflate(R.layout.fragment_map_driver, parent, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideUI()
        view.btn_orders_list.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_new_orders)
        }
        view.btn_order_accept.setOnClickListener {
            model.updateOrder(DbEntries.Orders.Fields.ORDER_STATUS, OrderStatus.STARTED)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        model.countOfOrders.observe(requireActivity(), Observer { count ->
            with(view?.cart_badge) {
                if (view?.btn_order_accept?.visibility == View.GONE) {
                    this?.visibility = if (count != 0) View.VISIBLE else View.GONE
                    this?.text = count.toString()
                }
            }
        })

        model.activeOrder.observe(requireActivity(), activeOrderObserver)
    }

    private fun hideUI() {
        view?.btn_order_accept?.visibility = View.GONE
        view?.info_boxes?.order_info_price?.visibility = View.INVISIBLE
        view?.info_boxes?.order_info_distance?.visibility = View.INVISIBLE
        view?.bottom_sheet?.visibility = View.GONE
    }

    private fun showUI() {
        view?.btn_order_accept?.visibility = View.VISIBLE
        view?.btn_orders_list?.visibility = View.GONE
        view?.cart_badge?.visibility = View.GONE
        view?.info_boxes?.order_info_price?.visibility = View.VISIBLE
        view?.info_boxes?.order_info_distance?.visibility = View.VISIBLE
        view?.bottom_sheet?.visibility = View.VISIBLE
    }

    private fun changeButtons() {
        showUI()
        view?.btn_order_accept?.visibility = View.GONE
        view?.btn_orders_list?.visibility = View.VISIBLE
        view?.cart_badge?.visibility = View.VISIBLE
    }

    private fun fillInfo(order: Order) {
        view?.order_info_price?.text =
            requireActivity().getString(R.string.currency_UAH, order.price.toString())
        view?.order_info_distance?.text = prepareDistance(requireActivity(), order)
        view?.origin_address?.text = order.addressStart?.address
        view?.dest_address?.text = order.addressDestination?.address
        view?.invalidate()
    }

    private fun showCancelDialog() = activity?.supportFragmentManager?.let {
        InfoDialog.show(
            fm = it,
            title = getString(R.string.dialog_title_order_is_changed),
            message = getString(R.string.dialog_text_order_was_cancelled)
        )
    }

    private fun showRateDialog() = activity?.supportFragmentManager?.let {
        RateDialog.show(
            fm = it,
            callback = ratingCallback
        )
    }
}