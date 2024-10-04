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

data class Video(
    val id: Int,
    val detalleid: Int,
    val ruta: String


)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "seal18.db"
        private const val DATABASE_VERSION = 18
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE grupo (
                inspeccion INTEGER,
                tecnicoId INTEGER,
                cantidad INTEGER,
                tipo String
            )
        """
        )
//tipo = IN, GI

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
                observado INTEGER  DEFAULT 0,
                
                reset  TEXT DEFAULT '',
                mdhfpa  TEXT DEFAULT '',
                eatp  TEXT DEFAULT '',
                eahpp  TEXT DEFAULT '',
                mdhpp  TEXT DEFAULT '',
                mdhpa  TEXT DEFAULT '',
                eahfpp  TEXT DEFAULT '',
                mdhfpp  TEXT DEFAULT '',
                erp  TEXT DEFAULT '',
                eatc  TEXT DEFAULT '',
                eahpc  TEXT DEFAULT '',
                mdhpc  TEXT DEFAULT '',
                eahfpc  TEXT DEFAULT '',
                mdhfpc  TEXT DEFAULT '',
                erc  TEXT DEFAULT '',
                
                tipolec TEXT DEFAULT '',
                tipolecman TEXT DEFAULT '',
                sed TEXT DEFAULT '',
                
                ubicacion TEXT DEFAULT '',
                perfilCarga TEXT DEFAULT ''
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

        db.execSQL(
            """
            CREATE TABLE video (
                id  INTEGER PRIMARY KEY AUTOINCREMENT,
                detalleid INTEGER,
                ruta TEXT
            )
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS grupo")
        db.execSQL("DROP TABLE IF EXISTS detalle")
        db.execSQL("DROP TABLE IF EXISTS detalleImagen")
        db.execSQL("DROP TABLE IF EXISTS video")
        onCreate(db)
    }

    fun addVideo( detalleid: Int, ruta: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("detalleid", detalleid)
        values.put("ruta", ruta)
        val id = db.insert("video", null, values)
        db.close()
        return id
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
                    try{
                    val operacion = Foto(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getBlob(it.getColumnIndexOrThrow("foto")),
                        it.getInt(it.getColumnIndexOrThrow("detalleid")),
                        it.getInt(it.getColumnIndexOrThrow("tipo")),
                        it.getInt(it.getColumnIndexOrThrow("enviado")),

                        )
                    operacionList.add(operacion)

                    } catch (e: Exception) {
                        var _error = "Error: ${e.message}"
                    }
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getFotoDetalle(detalleid: Int): List<Foto> {
        val operacionList = mutableListOf<Foto>()
        val selectQuery = "SELECT * FROM detalleImagen WHERE detalleid = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(detalleid.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    try{
                        val operacion = Foto(
                            it.getInt(it.getColumnIndexOrThrow("id")),
                            it.getBlob(it.getColumnIndexOrThrow("foto")),
                            it.getInt(it.getColumnIndexOrThrow("detalleid")),
                            it.getInt(it.getColumnIndexOrThrow("tipo")),
                            it.getInt(it.getColumnIndexOrThrow("enviado")),

                            )
                        operacionList.add(operacion)

                    } catch (e: Exception) {
                        var _error = "Error: ${e.message}"
                    }
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getVideoDetalle(detalleid: Int): String {

        val selectQuery = "SELECT * FROM video WHERE detalleid = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(detalleid.toString()))
        var video: String = ""
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    try{
                        video =  it.getString(it.getColumnIndexOrThrow("ruta"))


                    } catch (e: Exception) {
                        var _error = "Error: ${e.message}"
                    }
                } while (it.moveToNext())
            }
        }
        return video
    }

    fun insertGrupo(grupo: Grupo) {
        writableDatabase.use { db ->
            db.execSQL(
                "INSERT INTO grupo (inspeccion, tecnicoId, cantidad, tipo) VALUES (?, ?, ?, ?)",
                arrayOf(grupo.inspeccion, grupo.tecnicoId, grupo.cantidad, grupo.tipo)
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
                        it.getInt(it.getColumnIndexOrThrow("cantidad")),
                        it.getString(it.getColumnIndexOrThrow("tipo"))
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
                    "INSERT INTO detalle (id, uniqueId, contrato, medidor, nombres, ruta, direccion, nim, inspeccionId, latitud, longitud, tecnicoAsignado, lectura, observacion, latitudSave, longitudSave, fechaSave, actualizado, enviado,observado,reset,mdhfpa,eatp,eahpp,mdhpp,mdhpa,eahfpp,mdhfpp,erp,eatc,eahpc,mdhpc,eahfpc,mdhfpc,erc,tipolec,tipolecman, sed,ubicacion,perfilCarga) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",



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
                        0,"","","","","","","","","","","","","","","","","",detalle.sed,"",""
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
                        it.getString(it.getColumnIndexOrThrow("reset")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpa")),
                        it.getString(it.getColumnIndexOrThrow("eatp")),
                        it.getString(it.getColumnIndexOrThrow("eahpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpa")),
                        it.getString(it.getColumnIndexOrThrow("eahfpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpp")),
                        it.getString(it.getColumnIndexOrThrow("erp")),
                        it.getString(it.getColumnIndexOrThrow("eatc")),
                        it.getString(it.getColumnIndexOrThrow("eahpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhpc")),
                        it.getString(it.getColumnIndexOrThrow("eahfpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpc")),
                        it.getString(it.getColumnIndexOrThrow("erc")),

                        it.getString(it.getColumnIndexOrThrow("tipolec")),
                        it.getString(it.getColumnIndexOrThrow("tipolecman")),
                        it.getString(it.getColumnIndexOrThrow("sed")),

                        it.getString(it.getColumnIndexOrThrow("ubicacion")),
                        it.getString(it.getColumnIndexOrThrow("perfilCarga")),



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
        val cursor = db.rawQuery(selectQuery, arrayOf("0"))
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
                        it.getString(it.getColumnIndexOrThrow("reset")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpa")),
                        it.getString(it.getColumnIndexOrThrow("eatp")),
                        it.getString(it.getColumnIndexOrThrow("eahpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpa")),
                        it.getString(it.getColumnIndexOrThrow("eahfpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpp")),
                        it.getString(it.getColumnIndexOrThrow("erp")),
                        it.getString(it.getColumnIndexOrThrow("eatc")),
                        it.getString(it.getColumnIndexOrThrow("eahpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhpc")),
                        it.getString(it.getColumnIndexOrThrow("eahfpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpc")),
                        it.getString(it.getColumnIndexOrThrow("erc")),
                        it.getString(it.getColumnIndexOrThrow("tipolec")),
                        it.getString(it.getColumnIndexOrThrow("tipolecman")),
                        it.getString(it.getColumnIndexOrThrow("sed")),
                        it.getString(it.getColumnIndexOrThrow("ubicacion")),
                        it.getString(it.getColumnIndexOrThrow("perfilCarga")),
                        )
                    operacionList.add(operacion)
                } while (it.moveToNext())
            }
        }
        return operacionList
    }

    fun getGrupo(tipo: String): List<Result> {
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
) di ON g.inspeccion = di.inspeccionId WHERE tipo = ?;

        """.trimIndent()
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(tipo))
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
                    it.getString(it.getColumnIndexOrThrow("reset")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpa")),
                    it.getString(it.getColumnIndexOrThrow("eatp")),
                    it.getString(it.getColumnIndexOrThrow("eahpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhpa")),
                    it.getString(it.getColumnIndexOrThrow("eahfpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpp")),
                    it.getString(it.getColumnIndexOrThrow("erp")),
                    it.getString(it.getColumnIndexOrThrow("eatc")),
                    it.getString(it.getColumnIndexOrThrow("eahpc")),
                    it.getString(it.getColumnIndexOrThrow("mdhpc")),
                    it.getString(it.getColumnIndexOrThrow("eahfpc")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpc")),
                    it.getString(it.getColumnIndexOrThrow("erc")),
                    it.getString(it.getColumnIndexOrThrow("tipolec")),
                    it.getString(it.getColumnIndexOrThrow("tipolecman")),
                    it.getString(it.getColumnIndexOrThrow("sed")),
                    it.getString(it.getColumnIndexOrThrow("ubicacion")),
                    it.getString(it.getColumnIndexOrThrow("perfilCarga")),
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
                    it.getString(it.getColumnIndexOrThrow("reset")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpa")),
                    it.getString(it.getColumnIndexOrThrow("eatp")),
                    it.getString(it.getColumnIndexOrThrow("eahpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhpa")),
                    it.getString(it.getColumnIndexOrThrow("eahfpp")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpp")),
                    it.getString(it.getColumnIndexOrThrow("erp")),
                    it.getString(it.getColumnIndexOrThrow("eatc")),
                    it.getString(it.getColumnIndexOrThrow("eahpc")),
                    it.getString(it.getColumnIndexOrThrow("mdhpc")),
                    it.getString(it.getColumnIndexOrThrow("eahfpc")),
                    it.getString(it.getColumnIndexOrThrow("mdhfpc")),
                    it.getString(it.getColumnIndexOrThrow("erc")),
                    it.getString(it.getColumnIndexOrThrow("tipolec")),
                    it.getString(it.getColumnIndexOrThrow("tipolecman")),
                    it.getString(it.getColumnIndexOrThrow("sed")),
                    it.getString(it.getColumnIndexOrThrow("ubicacion")),
                    it.getString(it.getColumnIndexOrThrow("perfilCarga")),
                    )
            } else null
        }
    }

    fun search( search: String): List<Detalle> {
        val operacionList = mutableListOf<Detalle>()
        val selectQuery = "SELECT * FROM Detalle WHERE " + search
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
                        it.getString(it.getColumnIndexOrThrow("reset")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpa")),
                        it.getString(it.getColumnIndexOrThrow("eatp")),
                        it.getString(it.getColumnIndexOrThrow("eahpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhpa")),
                        it.getString(it.getColumnIndexOrThrow("eahfpp")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpp")),
                        it.getString(it.getColumnIndexOrThrow("erp")),
                        it.getString(it.getColumnIndexOrThrow("eatc")),
                        it.getString(it.getColumnIndexOrThrow("eahpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhpc")),
                        it.getString(it.getColumnIndexOrThrow("eahfpc")),
                        it.getString(it.getColumnIndexOrThrow("mdhfpc")),
                        it.getString(it.getColumnIndexOrThrow("erc")),
                        it.getString(it.getColumnIndexOrThrow("tipolec")),
                        it.getString(it.getColumnIndexOrThrow("tipolecman")),
                        it.getString(it.getColumnIndexOrThrow("sed")),
                        it.getString(it.getColumnIndexOrThrow("ubicacion")),
                        it.getString(it.getColumnIndexOrThrow("perfilCarga")),
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
        fechaSave: String,
        reset: String,
        mdhfpa: String,
        eatp: String,
        eahpp: String,
        mdhpp: String,
        mdhpa: String,
        eahfpp: String,
        mdhfpp: String,
        erp: String,
        eatc: String,
        eahpc: String,
        mdhpc: String,
        eahfpc: String,
        mdhfpc: String,
        erc: String,

        tipolec: String,
        tipolecman: String,

        ubicacion: String,
        perfilCarga: String

    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("lectura", lectura)
            put("observacion", observacion)
            put("latitudSave", latitudSave)
            put("longitudSave", longitudSave)
            put("fechaSave", fechaSave)
            put("actualizado", 1)
            put("reset", reset)
            put("mdhfpa", mdhfpa)
            put("eatp", eatp)
            put("eahpp", eahpp)
            put("mdhpp", mdhpp)
            put("mdhpa", mdhpa)
            put("eahfpp", eahfpp)
            put("mdhfpp", mdhfpp)
            put("erp", erp)
            put("eatc", eatc)
            put("eahpc", eahpc)
            put("mdhpc", mdhpc)
            put("eahfpc", eahfpc)
            put("mdhfpc", mdhfpc)
            put("erc", erc)

            put("tipolec", tipolec)
            put("tipolecman", tipolecman)

            put("ubicacion", ubicacion)
            put("perfilCarga", perfilCarga)
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
