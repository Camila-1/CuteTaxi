package ua.com.cuteteam.cutetaxiproject.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ua.com.cuteteam.cutetaxiproject.R

/**
 * A simple [Fragment] subclass.
 */
class HeadPieceFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_head_piece, container, false)
    }


}
