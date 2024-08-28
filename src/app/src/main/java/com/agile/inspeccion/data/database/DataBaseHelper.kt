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

data class Result(
    val inspeccion: Int,
    val total: Int,
    val pendientes: Int,
    val inspeccionados: Int,
    val enviados: Int,

    val imagenes_enviadas: Int,
    val imagenes_no_enviadas: Int


)

data class Foto(
    val id: Int,
    val foto: ByteArray,
    val detalleid: Int,
    val tipo: Int,
    val enviado: Int

)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "seal4.db"
        private const val DATABASE_VERSION = 13
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
                uniqueId TEXT,
                contrato INTEGER,
                medidor TEXT,
                nombres TEXT,
                ruta TEXT,
                direccion TEXT,
                nim TEXT,
                inspeccionId INTEGER,
                latitud REAL,
                longitud REAL,
                tecnicoAsignado INTEGER,
                
                
                lectura TEXT DEFAULT '',
                observacion INTEGER DEFAULT 0,
                latitudSave REAL DEFAULT 0,
                longitudSave REAL  DEFAULT 0,
                fechaSave  TEXT DEFAULT '',
                actualizado INTEGER  DEFAULT 0,
                enviado INTEGER  DEFAULT 0,
                observado INTEGER  DEFAULT 0
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE detalleImagen (
                id  INTEGER PRIMARY KEY AUTOINCREMENT,
                detalleid INTEGER,
                foto BLOB,
                tipo INTEGER,
                enviado INTEGER  DEFAULT 0
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
        foto.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        values.put("foto", byteArray)
        values.put("detalleid", detalleid)
        values.put("tipo", tipo)
        values.put("enviado", 0)
        val id = db.insert("detalleImagen", null, values)
        db.close()
        return id
    }


    fun getFotoNoEnviado(): List<Foto> {
        val operacionList = mutableListOf<Foto>()
        val selectQuery = "SELECT * FROM detalleImagen WHERE enviado = 0"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Foto(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getBlob(it.getColumnIndexOrThrow("foto")),
                        it.getInt(it.getColumnIndexOrThrow("detalleid")),
                        it.getInt(it.getColumnIndexOrThrow("tipo")),
                        it.getInt(it.getColumnIndexOrThrow("enviado")),

                        )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
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
        try {
            writableDatabase.use { db ->


                db.execSQL(
                    "INSERT INTO detalle (id, uniqueId, contrato, medidor, nombres, ruta, direccion, nim, inspeccionId, latitud, longitud, tecnicoAsignado, lectura, observacion, latitudSave, longitudSave, fechaSave, actualizado, enviado,observado) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?,?,?,?,?,?,?,?)",
                    arrayOf(
                        detalle.id,
                        detalle.uniqueId,
                        detalle.contrato,
                        detalle.medidor,
                        detalle.nombres,
                        detalle.ruta,
                        detalle.direccion,
                        detalle.nim,
                        detalle.inspeccionId,
                        detalle.latitud,
                        detalle.longitud,
                        detalle.tecnicoAsignado,

                        "",
                        0,
                        0,
                        0,
                        "",
                        0,
                        0,
                        0
                    )
                )
            }
        } catch (e: Exception) {
            var error = e.toString()
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

    fun getDetalleByInspeccionId(
        inspeccionId: Int,
        showPending: Boolean,
        showInspected: Boolean,
        showAll: Boolean
    ): List<Detalle> {
        val operacionList = mutableListOf<Detalle>()
        var selectQuery = ""
        if (showPending) {
            selectQuery = "SELECT * FROM Detalle WHERE inspeccionId = ? AND actualizado = 0"
        } else if (showInspected) {
            selectQuery = "SELECT * FROM Detalle WHERE inspeccionId = ? AND actualizado = 1"
        } else if (showAll) {
            selectQuery = "SELECT * FROM Detalle WHERE inspeccionId = ?"
        }

        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(inspeccionId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Detalle(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getString(it.getColumnIndexOrThrow("uniqueId")),
                        it.getInt(it.getColumnIndexOrThrow("contrato")),
                        it.getString(it.getColumnIndexOrThrow("medidor")),
                        it.getString(it.getColumnIndexOrThrow("nombres")),
                        it.getString(it.getColumnIndexOrThrow("ruta")),
                        it.getString(it.getColumnIndexOrThrow("direccion")),
                        it.getString(it.getColumnIndexOrThrow("nim")),
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
                        it.getInt(it.getColumnIndexOrThrow("enviado")),

                        )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getDetalleNoEnviado(): List<Detalle> {
        val operacionList = mutableListOf<Detalle>()
        val selectQuery = "SELECT * FROM Detalle WHERE enviado = ?  AND actualizado = 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf("0".toString()))
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Detalle(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getString(it.getColumnIndexOrThrow("uniqueId")),
                        it.getInt(it.getColumnIndexOrThrow("contrato")),
                        it.getString(it.getColumnIndexOrThrow("medidor")),
                        it.getString(it.getColumnIndexOrThrow("nombres")),
                        it.getString(it.getColumnIndexOrThrow("ruta")),
                        it.getString(it.getColumnIndexOrThrow("direccion")),
                        it.getString(it.getColumnIndexOrThrow("nim")),
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
                        it.getInt(it.getColumnIndexOrThrow("enviado")),

                        )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getGrupo(): List<Result> {
        val operacionList = mutableListOf<Result>()
        val selectQuery = """
            SELECT 
    g.inspeccion,
    g.tecnicoId,
    g.cantidad,
    COALESCE(a.actualizado_0, 0) AS cantidad_actualizado_0,
    COALESCE(a.actualizado_1, 0) AS cantidad_actualizado_1,
    COALESCE(e.enviado_0, 0) AS cantidad_enviado_0,
    COALESCE(e.enviado_1, 0) AS cantidad_enviado_1,
    COALESCE(di.total_imagenes, 0) AS total_imagenes,
    COALESCE(di.imagenes_enviadas, 0) AS imagenes_enviadas,
    COALESCE(di.imagenes_no_enviadas, 0) AS imagenes_no_enviadas
FROM 
    grupo g
LEFT JOIN (
    SELECT 
        inspeccionId,
        SUM(CASE WHEN actualizado = 0 THEN 1 ELSE 0 END) AS actualizado_0,
        SUM(CASE WHEN actualizado = 1 THEN 1 ELSE 0 END) AS actualizado_1
    FROM 
        detalle
    GROUP BY 
        inspeccionId
) a ON g.inspeccion = a.inspeccionId
LEFT JOIN (
    SELECT 
        inspeccionId,
        SUM(CASE WHEN enviado = 0 THEN 1 ELSE 0 END) AS enviado_0,
        SUM(CASE WHEN enviado = 1 THEN 1 ELSE 0 END) AS enviado_1
    FROM 
        detalle
    GROUP BY 
        inspeccionId
) e ON g.inspeccion = e.inspeccionId
LEFT JOIN (
    SELECT 
        d.inspeccionId,
        COUNT(di.id) AS total_imagenes,
        SUM(CASE WHEN di.enviado = 1 THEN 1 ELSE 0 END) AS imagenes_enviadas,
        SUM(CASE WHEN di.enviado = 0 THEN 1 ELSE 0 END) AS imagenes_no_enviadas
    FROM 
        detalle d
    LEFT JOIN 
        detalleImagen di ON d.id = di.detalleid
    GROUP BY 
        d.inspeccionId
) di ON g.inspeccion = di.inspeccionId;

        """.trimIndent()
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val operacion = Result(
                        it.getInt(it.getColumnIndexOrThrow("inspeccion")),//inspeccion
                        it.getInt(it.getColumnIndexOrThrow("cantidad")),//total
                        it.getInt(it.getColumnIndexOrThrow("cantidad_actualizado_0")),//pendientes
                        it.getInt(it.getColumnIndexOrThrow("cantidad_actualizado_1")),//inspeccionados
                        it.getInt(it.getColumnIndexOrThrow("cantidad_enviado_1")),//enviados
                        it.getInt(it.getColumnIndexOrThrow("imagenes_enviadas")),
                        it.getInt(it.getColumnIndexOrThrow("imagenes_no_enviadas")),
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
                    it.getString(it.getColumnIndexOrThrow("uniqueId")),
                    it.getInt(it.getColumnIndexOrThrow("contrato")),
                    it.getString(it.getColumnIndexOrThrow("medidor")),
                    it.getString(it.getColumnIndexOrThrow("nombres")),
                    it.getString(it.getColumnIndexOrThrow("ruta")),
                    it.getString(it.getColumnIndexOrThrow("direccion")),
                    it.getString(it.getColumnIndexOrThrow("nim")),
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
                    it.getInt(it.getColumnIndexOrThrow("enviado")),
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
                    it.getString(it.getColumnIndexOrThrow("uniqueId")),
                    it.getInt(it.getColumnIndexOrThrow("contrato")),
                    it.getString(it.getColumnIndexOrThrow("medidor")),
                    it.getString(it.getColumnIndexOrThrow("nombres")),
                    it.getString(it.getColumnIndexOrThrow("ruta")),
                    it.getString(it.getColumnIndexOrThrow("direccion")),
                    it.getString(it.getColumnIndexOrThrow("nim")),
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
                    it.getInt(it.getColumnIndexOrThrow("enviado")),
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
                        it.getString(it.getColumnIndexOrThrow("uniqueId")),
                        it.getInt(it.getColumnIndexOrThrow("contrato")),
                        it.getString(it.getColumnIndexOrThrow("medidor")),
                        it.getString(it.getColumnIndexOrThrow("nombres")),
                        it.getString(it.getColumnIndexOrThrow("ruta")),
                        it.getString(it.getColumnIndexOrThrow("direccion")),
                        it.getString(it.getColumnIndexOrThrow("nim")),
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
                        it.getInt(it.getColumnIndexOrThrow("enviado")),
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

    fun updateDetalleByUniqueId(
        UniqueId: String
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("enviado", 1)
        }
        return db.update("Detalle", values, "uniqueId = ?", arrayOf(UniqueId))
    }

    fun updateObservation(
        contrato: Int
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("enviado", 0)
            put("observado", 1)
        }
        return db.update("Detalle", values, "contrato = ?", arrayOf(contrato.toString()))
    }

    fun updateDetalleFotoByUniqueId(
        Id: Int
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("enviado", 1)
        }
        return db.update("detalleImagen", values, "id = ?", arrayOf(Id.toString()))
    }
}
