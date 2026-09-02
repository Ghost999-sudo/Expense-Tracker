package com.example.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExpenseEntity::class,
        UserEntity::class,
        CategoryBudgetEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun userDao(): UserDao

    abstract fun categoryBudgetDao(): CategoryBudgetDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "passwordHash TEXT NOT NULL, " +
                        "salt TEXT NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS category_budgets (" +
                        "category TEXT NOT NULL PRIMARY KEY, " +
                        "monthlyLimit INTEGER NOT NULL, " +
                        "updatedAt INTEGER NOT NULL DEFAULT 0, " +
                        "isSynced INTEGER NOT NULL DEFAULT 1)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {

                db.execSQL("ALTER TABLE expenses ADD COLUMN serverId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE category_budgets ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE category_budgets ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 1")
            }
        }

        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}