package ua.com.cuteteam.cutetaxi.data.room_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ua.com.cuteteam.cutetaxi.data.room_database.entities.FavoriteOrder

@Database(entities = [FavoriteOrder::class], version = 1, exportSchema = false)
@TypeConverters(RoomTypeConverters::class)
abstract class OrdersDatabase : RoomDatabase() {

    abstract fun favoriteOrdersDao(): FavoriteOrdersDao

    companion object {
        @Volatile
        private var instance: OrdersDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                OrdersDatabase::class.java, "orders.db"
            ).build()
    }
}