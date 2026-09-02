package com.example.expensetracker.viewmodel

import android.os.Parcel
import android.os.Parcelable

sealed class ScanState {

    data object Idle : ScanState()

    data object Scanning : ScanState()

    data class Success(
        val result: ParsedReceipt
    ) : ScanState()

    data class Error(
        val message: String
    ) : ScanState()
}

data class ParsedReceipt(
    val amountCents: Long? = null,
    val merchantName: String? = null,
    val date: Long? = null,
    val suggestedCategory: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        amountCents = parcel.readValue(Long::class.java.classLoader) as? Long,
        merchantName = parcel.readString(),
        date = parcel.readValue(Long::class.java.classLoader) as? Long,
        suggestedCategory = parcel.readString()
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.writeValue(amountCents)
        parcel.writeString(merchantName)
        parcel.writeValue(date)
        parcel.writeString(suggestedCategory)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ParsedReceipt> {

        override fun createFromParcel(
            parcel: Parcel
        ): ParsedReceipt = ParsedReceipt(parcel)

        override fun newArray(
            size: Int
        ): Array<ParsedReceipt?> = arrayOfNulls(size)
    }
}
