package ua.com.cuteteam.cutetaxi.fragments.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_driver_orders.view.*
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.fragments.adapters.OrdersAdapter
import ua.com.cuteteam.cutetaxi.repositories.DriverRepository
import ua.com.cuteteam.cutetaxi.viewmodels.DriverViewModel
import ua.com.cuteteam.cutetaxi.viewmodels.viewmodelsfactories.DriverViewModelFactory

private const val TAG = "Cute.OrdersFragment"

class DriverOrdersFragment : Fragment() {

    private lateinit var mAdapter: OrdersAdapter

    private val model by lazy {
        ViewModelProvider(requireActivity(), DriverViewModelFactory(DriverRepository()))
            .get(DriverViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_driver_orders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = OrdersAdapter().apply {
            setAcceptListener(requireActivity() as OrdersAdapter.OnOrderAccept)
        }

        with(view.new_orders) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = mAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model.orders.observe(requireActivity(), Observer {
            mAdapter.setOrders(it)
        })
    }
}