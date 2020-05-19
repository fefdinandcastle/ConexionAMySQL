package com.uam.rentadeautos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMG = 0;
    Conectar conectar;
    EditText brand,model,year;
    ImageView imagen;
    byte[] imagenCodificada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarVistas();
        conectar = new Conectar();
    }

    public void inicializarVistas(){
        brand = (EditText) findViewById(R.id.inputBrand);
        model = (EditText) findViewById(R.id.inputModel);
        year = (EditText) findViewById(R.id.inputYear);
        imagen = (ImageView)findViewById(R.id.imagen);
    }

    public void findImage(View v){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagen.setImageBitmap(selectedImage);
                imagenCodificada=codificarImagen(selectedImage);
                //Toast.makeText(getBaseContext(),imagenCodificada,Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public byte[] codificarImagen(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        //String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return byteArray;
    }

    public void submit(View v){
        if(brand.getText().toString().equals("")||model.getText().toString().equals("")||year.getText().toString().equals("")){
            Toast.makeText(this,"Debe llenar todos los campos.",Toast.LENGTH_SHORT).show();
        }else{
            agregarAuto agregarAuto = new agregarAuto();
            agregarAuto.execute("");
        }
    }

    public class agregarAuto extends AsyncTask<String,String,String>
    {
        String brandstr=brand.getText().toString();
        String modelstr=model.getText().toString();
        String yearstr=year.getText().toString();
        int yearint=Integer.valueOf(yearstr);
        String z="";
        boolean isSuccess=false;
        @Override
        protected void onPreExecute() {
            //progressDialog.setMessage("Loading...");
            //progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
                try {
                    Connection con = conectar.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {
                        String sql="INSERT INTO autos (Marca, Modelo, Anio, Disponible, Imagen) VALUES (?, ?, ?, ?, ?)";

                        PreparedStatement ps=con.prepareStatement(sql);
                        ps.setString(1,brandstr);
                        ps.setString(2,modelstr);
                        ps.setInt(3,yearint);
                        ps.setBoolean(4,true);
                        //ps.setBinaryStream(5,imagenCodificada);
                        ps.setBytes(5,imagenCodificada);
                        ps.execute();

                        //String query="INSERT INTO autos VALUES ('" + brandstr + "','" + modelstr + "','"  + yearint + "','"  + false + "','"  + imagenCodificada+");";
                        //Statement stmt = con.createStatement();
                        //stmt.executeUpdate(query);
                        z = "El auto ha sido agregado satisfactoriamente";
                        isSuccess=true;
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions"+ex;
                    Log.e("ERRO", z);
                }

            return z;
        }
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();
            if(isSuccess) {
                //startActivity(new Intent(MainActivity.this,Main2Activity.class));
                brand.setText("");
                model.setText("");
                year.setText("");
            }
            //progressDialog.hide();
        }
    }




}



