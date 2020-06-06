package com.smqpro.zetnews.view.web

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.smqpro.zetnews.R
import kotlinx.android.synthetic.main.fragment_web.*

class WebFragment : Fragment(R.layout.fragment_web) {
    private val args: WebFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPage()
    }

    private fun setPage() {
        web.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClientE(web_progress)
            loadUrl(args.result.webUrl)
//            setOnKeyListener { view, code, keyEvent ->
//                if (code == KeyEvent.KEYCODE_BACK && canGoBack()) {
//                    goBack()
//                }
//                false
//            } // TODO handle back button
        }
    }

    class WebChromeClientE(private val progressBar: ProgressBar) : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
            if (newProgress == 100) progressBar.visibility = View.GONE
        }

    }

}