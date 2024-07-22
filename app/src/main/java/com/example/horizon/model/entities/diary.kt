package com.example.horizon.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "diary")
data class diary(
 @PrimaryKey(autoGenerate = true)
 val id : Int,
 @ColumnInfo(name = "title")
 val title : String,
 @ColumnInfo(name = "date_time" )
 var dateTime : String,
 @ColumnInfo(name="subtitle")
 var subtitle : String,
 @ColumnInfo(name = "note_text")
 var noteText: String,
 @ColumnInfo(name = "image_path")
 var imagePath: String,
 @ColumnInfo(name ="color")
 var color: String,
 @ColumnInfo(name ="web_link")
 var webLink: String
)