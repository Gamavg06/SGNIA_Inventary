package com.example.inventariocarniceria.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        InventoryCaptureEntity::class,
        StockEntryEntity::class,
        BranchOrderEntity::class,
        LocalProductEntity::class,
        SupplierEntity::class,
        BranchEntity::class,
        PriceAuditEntity::class,
        DespieceTransformacionEntity::class,
        DespieceDetalleEntity::class,
        SaleTicketEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun inventoryCaptureDao(): InventoryCaptureDao
    abstract fun stockEntryDao(): StockEntryDao
    abstract fun branchOrderDao(): BranchOrderDao
    abstract fun localProductDao(): LocalProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun branchDao(): BranchDao
    abstract fun priceAuditDao(): PriceAuditDao
    abstract fun despieceDao(): DespieceDao
    abstract fun saleTicketDao(): SaleTicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN orderUnitType TEXT NOT NULL DEFAULT 'KG'")
                db.execSQL("ALTER TABLE products ADD COLUMN weightPerBoxKg REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE products ADD COLUMN unitsPerBox INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE products ADD COLUMN parentProductCode INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE products ADD COLUMN currentStockUnits REAL NOT NULL DEFAULT 0.0")

                db.execSQL("ALTER TABLE inventory_captures ADD COLUMN orderedUnits REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE inventory_captures ADD COLUMN unitType TEXT NOT NULL DEFAULT 'KG'")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_products ADD COLUMN code INTEGER NOT NULL DEFAULT 85")
                db.execSQL("ALTER TABLE local_products ADD COLUMN investmentName TEXT NOT NULL DEFAULT 'Botanas SGNIA'")
                db.execSQL("ALTER TABLE local_products ADD COLUMN piecesPerBolsita INTEGER NOT NULL DEFAULT 6")
                db.execSQL("ALTER TABLE local_products ADD COLUMN quantityToMake INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sale_tickets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        operationNumber TEXT NOT NULL,
                        ticketBarcode TEXT NOT NULL,
                        branchId TEXT NOT NULL DEFAULT 'Sucursal Principal SGNIA',
                        productCode INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        weightKg REAL NOT NULL,
                        pricePerKg REAL NOT NULL,
                        totalAmount REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'COMPLETADA',
                        reversalReason TEXT,
                        reversalTimestamp INTEGER,
                        cashierName TEXT NOT NULL DEFAULT 'Encargado de Turno'
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sale_tickets_operationNumber ON sale_tickets(operationNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sale_tickets_ticketBarcode ON sale_tickets(ticketBarcode)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_carniceria.db"
                )
                .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Base de datos oficial inicia 100% limpia y en blanco para el Dueño.
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Sin auto-inserts de prueba
            }
        }
    }
}
