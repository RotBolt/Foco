package com.pervysage.thelimitbreaker.foco.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "contact_info"
)
data class ContactInfo(
        var name: String,

        @PrimaryKey
        var number: String
)