package ua.com.cuteteam.cutetaxi.extentions

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.mutation(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}