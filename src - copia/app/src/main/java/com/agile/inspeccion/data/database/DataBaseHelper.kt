package com.agile.inspeccion.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.data.service.Grupo
import java.io.ByteArrayOutputStream

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "seal2.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE grupo (
                inspeccion INTEGER,
                tecnicoId INTEGER,
                cantidad INTEGER
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE detalle (
                id INTEGER,
                contrato INTEGER,
                medidor INTEGER,
                nombres TEXT,
                direccion TEXT,
                inspeccionId INTEGER,
                latitud REAL,
                longitud REAL,
                tecnicoAsignado INTEGER,
                                 lectura TEXT DEFAULT '',
     observacion INTEGER DEFAULT 0,
     latitudSave REAL DEFAULT 0,
     longitudSave REAL  DEFAULT 0,
     fechaSave  TEXT DEFAULT '',

     actualizado INTEGER  DEFAULT 0
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE detalleImagen (
                id  INTEGER PRIMARY KEY AUTOINCREMENT,
                detalleid INTEGER,
                foto BLOB,
                tipo INTEGER
            )
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS grupo")
        db.execSQL("DROP TABLE IF EXISTS detalle")
        db.execSQL("DROP TABLE IF EXISTS detalleImagen")
        onCreate(db)
    }

    fun addImage(foto: Bitmap, detalleid: Int, tipo: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        val stream = ByteArrayOutputStream()
        foto.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        values.put("foto", byteArray)
        values.put("detalleid", detalleid)
        values.put("tipo", tipo)
        val id = db.insert("detalleImagen", null, values)
        db.close()
        return id
    }

    fun insertGrupo(grupo: Grupo) {
        writableDatabase.use { db ->
            db.execSQL(
                "INSERT INTO grupo (inspeccion, tecnicoId, cantidad) VALUES (?, ?, ?)",
                arrayOf(grupo.inspeccion, grupo.tecnicoId, grupo.cantidad)
            )
        }
    }

    fun getAllGrupo(): List<Grupo> {
        val grupoList = mutableListOf<Grupo>()
        val selectQuery = "SELECT * FROM grupo"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val grupo = Grupo(
                        it.getInt(it.getColumnIndexOrThrow("inspeccion")),
                        it.getInt(it.getColumnIndexOrThrow("tecnicoId")),
                        it.getInt(it.getColumnIndexOrThrow("cantidad"))
                    )
                    grupoList.add(grupo)
                } while (it.moveToNext())
            }
        }
        return grupoList
    }

    fun insertDetalle(detalle: Detalle) {
        writableDatabase.use { db ->


            db.execSQL(
                "INSERT INTO detalle (id, contrato, medidor, nombres, direccion, inspeccionId, latitud, longitud, tecnicoAsignado, lectura, observacion, latitudSave, longitudSave, fechaSave, actualizado) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,   ?,?,?,?,?,?)",
                arrayOf(
                    detalle.id,
                    detalle.contrato,
                    detalle.medidor,
                    detalle.nombres,
                    detalle.direccion,
                    detalle.inspeccionId,
                    detalle.latitud,
                    detalle.longitud,
                    detalle.tecnicoAsignado,

                    "",
                    0,
                    0,
                    0,
                    "",
                    0
                )
            )
        }
    }

    fun deleteAllGrupo(): Int {
        val db = this.writableDatabase
        return db.delete("grupo", null, null)
    }

    fun deleteAllDetalle(): Int {
        val db = this.writableDatabase
        return db.delete("detalle", null, null)
    }

    fun getDetalleByInspeccionId(inspeccionId: Int): List<Detalle> {
        val operacionList = mutableListOf<Detalle>()
        val selectQuery = "SELECT * FROM Detalle WHERE inspeccionId = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(inspeccionId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Detalle(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getInt(it.getColumnIndexOrThrow("contrato")),
                        it.getInt(it.getColumnIndexOrThrow("medidor")),
                        it.getString(it.getColumnIndexOrThrow("nombres")),
                        it.getString(it.getColumnIndexOrThrow("direccion")),
                        it.getInt(it.getColumnIndexOrThrow("inspeccionId")),
                        it.getDouble(it.getColumnIndexOrThrow("latitud")),
                        it.getDouble(it.getColumnIndexOrThrow("longitud")),
                        it.getInt(it.getColumnIndexOrThrow("tecnicoAsignado")),

                        it.getString(it.getColumnIndexOrThrow("lectura")),
                        it.getInt(it.getColumnIndexOrThrow("observacion")),
                        it.getDouble(it.getColumnIndexOrThrow("latitudSave")),
                        it.getDouble(it.getColumnIndexOrThrow("longitudSave")),
                        it.getString(it.getColumnIndexOrThrow("fechaSave")),
                        it.getInt(it.getColumnIndexOrThrow("actualizado")),

                        )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getDetalleById(id: Int): Detalle? {

        val selectQuery = "SELECT * FROM Detalle WHERE id = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(id.toString()))
        return cursor.use {
            if (it.moveToFirst()) {
                Detalle(
                    it.getInt(it.getColumnIndexOrThrow("id")),
                    it.getInt(it.getColumnIndexOrThrow("contrato")),
                    it.getInt(it.getColumnIndexOrThrow("medidor")),
                    it.getString(it.getColumnIndexOrThrow("nombres")),
                    it.getString(it.getColumnIndexOrThrow("direccion")),
                    it.getInt(it.getColumnIndexOrThrow("inspeccionId")),
                    it.getDouble(it.getColumnIndexOrThrow("latitud")),
                    it.getDouble(it.getColumnIndexOrThrow("longitud")),
                    it.getInt(it.getColumnIndexOrThrow("tecnicoAsignado")),

                    it.getString(it.getColumnIndexOrThrow("lectura")),
                    it.getInt(it.getColumnIndexOrThrow("observacion")),
                    it.getDouble(it.getColumnIndexOrThrow("latitudSave")),
                    it.getDouble(it.getColumnIndexOrThrow("longitudSave")),
                    it.getString(it.getColumnIndexOrThrow("fechaSave")),
                    it.getInt(it.getColumnIndexOrThrow("actualizado")),
                )
            } else null
        }
    }

    fun getSiguiente(id: Int): Detalle? {

        val selectQuery = "SELECT * FROM Detalle WHERE id > ? AND actualizado = 0"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(id.toString()))
        return cursor.use {
            if (it.moveToFirst()) {
                Detalle(
                    it.getInt(it.getColumnIndexOrThrow("id")),
                    it.getInt(it.getColumnIndexOrThrow("contrato")),
                    it.getInt(it.getColumnIndexOrThrow("medidor")),
                    it.getString(it.getColumnIndexOrThrow("nombres")),
                    it.getString(it.getColumnIndexOrThrow("direccion")),
                    it.getInt(it.getColumnIndexOrThrow("inspeccionId")),
                    it.getDouble(it.getColumnIndexOrThrow("latitud")),
                    it.getDouble(it.getColumnIndexOrThrow("longitud")),
                    it.getInt(it.getColumnIndexOrThrow("tecnicoAsignado")),

                    it.getString(it.getColumnIndexOrThrow("lectura")),
                    it.getInt(it.getColumnIndexOrThrow("observacion")),
                    it.getDouble(it.getColumnIndexOrThrow("latitudSave")),
                    it.getDouble(it.getColumnIndexOrThrow("longitudSave")),
                    it.getString(it.getColumnIndexOrThrow("fechaSave")),
                    it.getInt(it.getColumnIndexOrThrow("actualizado")),
                )
            } else null
        }
    }

    fun search(column: String, search: String): List<Detalle> {
        val operacionList = mutableListOf<Detalle>()
        val selectQuery = "SELECT * FROM Detalle WHERE ${column} like '%${search}%'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Detalle(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getInt(it.getColumnIndexOrThrow("contrato")),
                        it.getInt(it.getColumnIndexOrThrow("medidor")),
                        it.getString(it.getColumnIndexOrThrow("nombres")),
                        it.getString(it.getColumnIndexOrThrow("direccion")),
                        it.getInt(it.getColumnIndexOrThrow("inspeccionId")),
                        it.getDouble(it.getColumnIndexOrThrow("latitud")),
                        it.getDouble(it.getColumnIndexOrThrow("longitud")),
                        it.getInt(it.getColumnIndexOrThrow("tecnicoAsignado")),

                        it.getString(it.getColumnIndexOrThrow("lectura")),
                        it.getInt(it.getColumnIndexOrThrow("observacion")),
                        it.getDouble(it.getColumnIndexOrThrow("latitudSave")),
                        it.getDouble(it.getColumnIndexOrThrow("longitudSave")),
                        it.getString(it.getColumnIndexOrThrow("fechaSave")),
                        it.getInt(it.getColumnIndexOrThrow("actualizado")),
                    )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun updateOperacion(detalle: Detalle): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("lectura", detalle.lectura)
            put("observacion", detalle.observacion)
            put("latitudSave", detalle.latitudSave)
            put("longitudSave", detalle.longitudSave)
            put("fechaSave", detalle.fechaSave)
            put("actualizado", 1)
        }
        return db.update("Detalle", values, "id = ?", arrayOf(detalle.id.toString()))
    }

    fun updateDetalle(
        id: Int,
        lectura: String,
        observacion: String,
        latitudSave: Double,
        longitudSave: Double,
        fechaSave: String
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("lectura", lectura)
            put("observacion", observacion)
            put("latitudSave", latitudSave)
            put("longitudSave", longitudSave)
            put("fechaSave", fechaSave)
            put("actualizado", 1)
        }
        return db.update("Detalle", values, "id = ?", arrayOf(id.toString()))
    }


}
/*
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "SEALOperacionesComerciales.db"

        private const val TABLE_GRUPO = "grupo"
        private const val TABLE_OPERACION = "operacion"

        // Columnas comunes
        private const val KEY_ID = "id"

        // Columnas de GRUPO
        private const val KEY_INSPECCION = "inspeccion"
        private const val KEY_TECNICO = "tecnico"
        private const val KEY_CANTIDAD = "cantidad"

        // Columnas de OPERACION
        private const val KEY_MEDIDOR = "medidor"
        private const val KEY_NOMBRES = "nombres"
        private const val KEY_DIRECCION = "direccion"
        private const val KEY_GRUPOID = "grupoid"
        private const val KEY_LATITUD = "latitud"
        private const val KEY_LONGITUD = "longitud"


        private val CREATE_TABLE_GRUPO = """
            CREATE TABLE $TABLE_GRUPO (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_INSPECCION INTEGER NOT NULL,
                $KEY_TECNICO INTEGER NOT NULL,
                $KEY_CANTIDAD INTEGER NOT NULL
            )
        """.trimIndent()

        private val CREATE_TABLE_OPERACION = """
            CREATE TABLE $TABLE_OPERACION (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_MEDIDOR INTEGER NOT NULL,
                $KEY_TECNICO INTEGER NOT NULL,
                $KEY_NOMBRES TEXT NOT NULL,
                $KEY_DIRECCION TEXT NOT NULL,
                $KEY_INSPECCION INTEGER NOT NULL,
                $KEY_GRUPOID INTEGER NOT NULL,
                $KEY_LATITUD REAL NOT NULL,
                $KEY_LONGITUD REAL NOT NULL
            )
        """.trimIndent()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_GRUPO)
        db.execSQL(CREATE_TABLE_OPERACION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GRUPO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_OPERACION")
        onCreate(db)
    }

    // CRUD para GRUPO

    fun insertGrupo(inspeccion: Int, tecnico: Int, cantidad: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_INSPECCION, inspeccion)
            put(KEY_TECNICO, tecnico)
            put(KEY_CANTIDAD, cantidad)
        }
        return db.insert(TABLE_GRUPO, null, values)
    }

    fun getGrupo(id: Int): Grupo? {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_GRUPO, arrayOf(KEY_ID, KEY_INSPECCION, KEY_TECNICO, KEY_CANTIDAD),
            "$KEY_ID=?", arrayOf(id.toString()), null, null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Grupo(
                    it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_INSPECCION)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_TECNICO)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_CANTIDAD))
                )
            } else null
        }
    }

    fun getAllGrupos(): List<Grupo> {
        val grupoList = mutableListOf<Grupo>()
        val selectQuery = "SELECT * FROM $TABLE_GRUPO"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val grupo = Grupo(
                        it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_INSPECCION)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_TECNICO)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_CANTIDAD))
                    )
                    grupoList.add(grupo)
                } while (it.moveToNext())
            }
        }
        return grupoList
    }

    fun updateGrupo(grupo: Grupo): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_INSPECCION, grupo.inspeccion)
            put(KEY_TECNICO, grupo.tecnico)
            put(KEY_CANTIDAD, grupo.cantidad)
        }
        return db.update(TABLE_GRUPO, values, "$KEY_ID = ?", arrayOf(grupo.id.toString()))
    }

    fun deleteGrupo(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_GRUPO, "$KEY_ID = ?", arrayOf(id.toString()))
    }

    // CRUD para OPERACION

    fun insertOperacion(medidor: Int, tecnico: Int, nombres: String, direccion: String,
                        inspeccion: Int, grupoid: Int, latitud: Double, longitud: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_MEDIDOR, medidor)
            put(KEY_TECNICO, tecnico)
            put(KEY_NOMBRES, nombres)
            put(KEY_DIRECCION, direccion)
            put(KEY_INSPECCION, inspeccion)
            put(KEY_GRUPOID, grupoid)
            put(KEY_LATITUD, latitud)
            put(KEY_LONGITUD, longitud)

        }
        return db.insert(TABLE_OPERACION, null, values)
    }

    fun getOperacion(id: Int): Operacion? {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_OPERACION,
            arrayOf(KEY_ID, KEY_MEDIDOR, KEY_TECNICO, KEY_NOMBRES, KEY_DIRECCION,
                KEY_INSPECCION, KEY_GRUPOID, KEY_LATITUD, KEY_LONGITUD),
            "$KEY_ID=?", arrayOf(id.toString()), null, null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Operacion(
                    it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_MEDIDOR)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_TECNICO)),
                    it.getString(it.getColumnIndexOrThrow(KEY_NOMBRES)),
                    it.getString(it.getColumnIndexOrThrow(KEY_DIRECCION)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_INSPECCION)),
                    it.getInt(it.getColumnIndexOrThrow(KEY_GRUPOID)),
                    it.getDouble(it.getColumnIndexOrThrow(KEY_LATITUD)),
                    it.getDouble(it.getColumnIndexOrThrow(KEY_LONGITUD))
                )
            } else null
        }
    }

    fun getAllOperaciones(): List<Operacion> {
        val operacionList = mutableListOf<Operacion>()
        val selectQuery = "SELECT * FROM $TABLE_OPERACION"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Operacion(
                        it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_MEDIDOR)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_TECNICO)),
                        it.getString(it.getColumnIndexOrThrow(KEY_NOMBRES)),
                        it.getString(it.getColumnIndexOrThrow(KEY_DIRECCION)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_INSPECCION)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_GRUPOID)),
                        it.getDouble(it.getColumnIndexOrThrow(KEY_LATITUD)),
                        it.getDouble(it.getColumnIndexOrThrow(KEY_LONGITUD))
                    )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getOperacionesByGrupoId(grupoId: Int): List<Operacion> {
        val operacionList = mutableListOf<Operacion>()
        val selectQuery = "SELECT * FROM $TABLE_OPERACION WHERE $KEY_GRUPOID = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(grupoId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Operacion(
                        it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_MEDIDOR)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_TECNICO)),
                        it.getString(it.getColumnIndexOrThrow(KEY_NOMBRES)),
                        it.getString(it.getColumnIndexOrThrow(KEY_DIRECCION)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_INSPECCION)),
                        it.getInt(it.getColumnIndexOrThrow(KEY_GRUPOID)),
                        it.getDouble(it.getColumnIndexOrThrow(KEY_LATITUD)),
                        it.getDouble(it.getColumnIndexOrThrow(KEY_LONGITUD))
                    )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }



    fun deleteOperacion(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_OPERACION, "$KEY_ID = ?", arrayOf(id.toString()))
    }


}

data class Grupo(
    val id: Int,
    val inspeccion: Int,
    val tecnico: Int,
    val cantidad: Int
)

data class Operacion(
    val id: Int,
    val medidor: Int,
    val tecnico: Int,
    val nombres: String,
    val direccion: String,
    val inspeccion: Int,
    val grupoid: Int,
    val latitud: Double,
    val longitud: Double
)
*/