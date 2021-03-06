package com.example.mibibliotecav2.registro

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mibibliotecav2.R
import com.example.mibibliotecav2.model.remote.UsersRemote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")


    private val REQUEST_IMAGE_CAPTURE = 4
    private val REQUEST_IMAGE_CAPTURE1 = 12
    private var url = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        BT_examinar_reg.setOnClickListener {
            escoger(REQUEST_IMAGE_CAPTURE, REQUEST_IMAGE_CAPTURE1)


        }


        BT_enviar_reg.setOnClickListener {
            val nombrereg = ET_nombre_reg.text.toString()
            val correoreg = ET_correo_reg.text.toString()
            val celularreg = ET_celular_reg.text.toString()
            val contrasenareg = ET_contrasena_reg.text.toString()
            val repcontrasenareg = ET_repita_contrasena_reg.text.toString()
            val generoreg = if (RB_masculino_reg.isChecked) "Masculino" else "Femenino"
            val ciudadreg = SP_ciudad_residencia.selectedItem.toString()
            val notificacionesreg = CB_notificaciones_reg.isChecked

            if (nombrereg.isEmpty() || correoreg.isEmpty() || celularreg.isEmpty() || contrasenareg.isEmpty()
                || repcontrasenareg.isEmpty() || generoreg.isEmpty() || ciudadreg.isEmpty()
            ) {
                Toast.makeText(this, "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                if (contrasenareg.length < 6) {
                    Toast.makeText(
                        this,
                        "La contraseña debe ser de minimo 6 digitos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (contrasenareg != repcontrasenareg) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                } else if (!isValidEmailId(correoreg)) {
                    Toast.makeText(this, "El correo ingresado no es válido", Toast.LENGTH_SHORT)
                        .show()
                } else if (celularreg.length != 10) {
                    Toast.makeText(this, "Numero de celular incorrecto", Toast.LENGTH_SHORT).show()
                } else {
                    mAuth.createUserWithEmailAndPassword(correoreg, contrasenareg)
                        .addOnCompleteListener(
                            this
                        ) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information

                                //Toast.makeText(this, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                                val user = mAuth.currentUser
                                val idusr = user?.uid
                                crearUsuarioEnBaseDeDatos(
                                    idusr,
                                    nombrereg,
                                    correoreg,
                                    celularreg,
                                    ciudadreg,
                                    notificacionesreg
                                )
                                Toast.makeText(this, "Espere un momento", Toast.LENGTH_LONG).show()


                            } else {
                                var errorfirebase: String = task.exception!!.message.toString()
                                when (errorfirebase) {
                                    "The email address is already in use by another account." ->
                                        Toast.makeText(
                                            this,
                                            "El usuario ya existe, ingrese otro correo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else -> Toast.makeText(this, errorfirebase, Toast.LENGTH_SHORT)
                                        .show()
                                }

                                Toast.makeText(this, "Registro fallido", Toast.LENGTH_SHORT).show()
                            }
                        }

                }
            }
        }

    }

    private fun dispatchTakePictureIntent(requestImage: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                startActivityForResult(takePictureIntent, requestImage)
            }

        }
    }

    private fun cargarImagen(requestImagePortada: Int) {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).also { takePictureIntent ->
            takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                startActivityForResult(takePictureIntent, requestImagePortada)
            }

        }

    }


    private fun isValidEmailId(email: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    private fun escoger(requestImage: Int, requestImage1: Int) {
        val builder = AlertDialog.Builder(this)
        val items = arrayOfNulls<CharSequence>(2)
        items[0] = "Cámara"
        items[1] = "Galería"
        builder.setTitle("Seleccione una foto")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent(requestImage)
                    1 -> cargarImagen(requestImage1)
                }
            }
        builder.create()
        builder.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "bien", Toast.LENGTH_SHORT).show()
            if (requestCode > 10) {
                val uri: Uri? = data?.data
                BT_examinar_reg.setImageURI(uri)
                BT_examinar_reg.visibility = View.GONE
                Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
            } else {
                var bitmap = data?.extras?.get("data") as Bitmap
                BT_examinar_reg.setImageBitmap(bitmap)
                BT_examinar_reg.visibility = View.GONE
                Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun crearUsuarioEnBaseDeDatos(
        id: String?,
        nombrereg: String,
        correoreg: String,
        celularreg: String,
        ciudadreg: String,
        notificacionesreg: Boolean
    ) {

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("tablausuarios")

        val mStorage = FirebaseStorage.getInstance()
        val photoRef = mStorage.reference.child(id!!)
        var urlPhoto = ""

        // Get the data from an ImageView as bytes
        BT_examinar_reg.isDrawingCacheEnabled = true
        BT_examinar_reg.buildDrawingCache()
        val bitmap = (BT_examinar_reg.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = photoRef.putBytes(data)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                urlPhoto = task.result.toString()

                val usuario = UsersRemote(
                    id,
                    nombrereg,
                    correoreg,
                    celularreg,
                    ciudadreg,
                    notificacionesreg,
                    urlPhoto
                )
                myRef.child(id).setValue(usuario)
                Toast.makeText(this, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                onBackPressed()

            } else {
                // Handle failures
                // ...
            }
        }
    }


}
