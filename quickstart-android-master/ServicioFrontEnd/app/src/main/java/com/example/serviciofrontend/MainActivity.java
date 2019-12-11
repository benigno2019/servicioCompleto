package com.example.serviciofrontend;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.serviciofrontend.Retrofit.Api;
import com.example.serviciofrontend.Retrofit.RetrofitCliente;
import com.example.serviciofrontend.Utils.Callbacks;
import com.example.serviciofrontend.Utils.Common;
import com.example.serviciofrontend.Utils.ProgressRequestBody;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callbacks {

    private static final int PICK_FILE_REQUEST=1000;

    Api mService;
    Button btnUpload,btnParar;
    TextView textoEmociones;
    ImageView imageViewEmoticos;
    ArrayList<Integer> myImageList = new ArrayList<>();
    ArrayList<String> emocionesList = new ArrayList<>();


    Uri selectedFileUri;
    ProgressDialog dialog;
    //CAMARA
    Thread thread,thread2;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Button capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false, initDetection = true;
    private int contNextImage=1;
    public static Bitmap bitmap = null;
    //Imagen
    ImageView imagen;
    Button siguiente;
    MediaPlayer sonidoImagen;
    BaseDatos base;
    ArrayList<Actividades> actividades_list;
    int index = 0;
    boolean actualizo= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myImageList.add(R.drawable.angry);
        myImageList.add(R.drawable.disgust);
        myImageList.add(R.drawable.fear);
        myImageList.add(R.drawable.happy);
        myImageList.add(R.drawable.sad);
        myImageList.add(R.drawable.surprise);
        myImageList.add(R.drawable.neutral);

        mService = getApiUpload();
        btnUpload = findViewById(R.id.btn_iniciar);
        textoEmociones = findViewById(R.id.textoEmociones);
        btnParar = findViewById(R.id.btn_parar);
        btnParar.setEnabled(false);
        //imageView = findViewById(R.id.img_image);
        //imageViewEmoticos = findViewById(R.id.img_iconon);

        /**************************************************** Imagenes *******************************************/
        imagen = findViewById(R.id.imagen);
        //siguiente = findViewById(R.id.siguiente);

        base = new BaseDatos(this, "Base1", null, 1);
        consultar_datos();

        imagen.setImageResource(getResources().getIdentifier(actividades_list.get(index).nomImgSon,"drawable",getPackageName()));
        sonidoImagen = MediaPlayer.create(MainActivity.this,
                getResources().getIdentifier(actividades_list.get(index).nomImgSon,
                        "raw",
                        getPackageName()));

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activarImagen();

            }
        });
        /*siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasarImagen();

            }
        });*/
        /**************************************************** Imagenes *******************************************/





        /**************************************************** CAMARA *******************************************/
        boolean priv = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        if (!priv){
            String[] privilegios = { Manifest.permission.READ_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(this, privilegios,0);

        }
        checkExternalStoragePermission();



        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });*/

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnParar.setEnabled(true);
                btnUpload.setEnabled(false);
                initThread();
            }
        });
        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread.interrupt();
                btnParar.setEnabled(false);
                btnUpload.setEnabled(true);
            }
        });

        //CAMARA
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        mCamera =  Camera.open();
        mCamera.setDisplayOrientation(90);
        cameraPreview = (LinearLayout) findViewById(R.id.cPreview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        releaseCamera();
        chooseCamera();

        /*capture = (Button) findViewById(R.id.btnCam);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });*/

        /*switchCamera = (Button) findViewById(R.id.btnSwitch);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the number of cameras
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    //release the old camera instance
                    //switch camera, from the front and the back and vice versa

                    //releaseCamera();
                    //chooseCamera();
                } else {

                }
            }
        });*/

        mCamera.startPreview();
    }

    public void activarImagen(){
        //isPlaying();
        if(sonidoImagen.isPlaying())
            return;
        int id = getResources().getIdentifier(actividades_list.get(index).nomImgSon,"raw",getPackageName());
        sonidoImagen = MediaPlayer.create(MainActivity.this,id);
        sonidoImagen.start();
        /*if(actualizo)
            return;
        actualizarPrioridad(actividades_list.get(index));
        acomodar_Lista();
        index = 0;
        actualizo = true;*/
    }
    public void pasarImagen(){
        if(contNextImage == 0) {
            contNextImage = 5;
            if (!actualizo)
                index++;
            actualizo = false;
            if(sonidoImagen.isPlaying())
                return;
            if (index >= actividades_list.size())
                index = 0;
            imagen.setImageResource(getResources().getIdentifier(actividades_list.get(index).nomImgSon, "drawable", getPackageName()));
        }
    }
    public void regresarImagen(){
        if(contNextImage == 0) {
            contNextImage = 5;
            if (!actualizo)
                index--;
            actualizo = false;
            if(sonidoImagen.isPlaying())
                return;
            if (index <= 0)
                index = actividades_list.size();
            imagen.setImageResource(getResources().getIdentifier(actividades_list.get(index).nomImgSon, "drawable", getPackageName()));
        }
    }
    public void initThread(){

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        //Toast.makeText(MainActivity.this,"entre thread",Toast.LENGTH_LONG).show();

                        mCamera.takePicture(null, null, mPicture);
                        Thread.sleep(1000);
                        uploadFile();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(this,"data "+data+" r "+requestCode,Toast.LENGTH_LONG).show();
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                //Toast.makeText(this,"data "+data,Toast.LENGTH_LONG).show();
                if(data != null){
                    selectedFileUri = data.getData();
                    Toast.makeText(this,"uri "+selectedFileUri,Toast.LENGTH_LONG).show();
                    /*if(selectedFileUri!=null && !selectedFileUri.getPath().isEmpty())
                        imageView.setImageURI(selectedFileUri);
                    else
                        Toast.makeText(this,"no se encontro3",Toast.LENGTH_LONG).show();*/
                }
            }
        }
    }

    private void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_FILE_REQUEST);
    }
    private void uploadFile(){
        if(bitmap!=null) {
            /*dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Subiendo...");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();*/
            File file = null;
            //Toast.makeText(this,"ffaa"+file,Toast.LENGTH_LONG).show();
            try {
                //file = new File(Common.getFilePath(this, selectedFileUri));
                file = new File(this.getCacheDir(), "hola");
                file.createNewFile();
                //Toast.makeText(this,"ff"+file,Toast.LENGTH_LONG).show();

                //Convert bitmap to byte array
                Bitmap bitmap1 = bitmap;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }



            if (file != null) {
                final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
                thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.uploadFile(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        //dialog.dismiss();
                                        if(response.body()!=null) {
                                            System.out.println("a "+response.body());
                                            //String[] split = response.body().split(" ");
                                            /*String image_processed_link = new StringBuilder("http://086dc108.ngrok.io/"
                                                    + split[0].replace("\"", "")).toString();*/

                                            /*Picasso.get().load(image_processed_link)
                                                    .into(imageView);*/
                                            //split[1] = split[1].replace("\"", "");


                                            String emocion = response.body().replace("\"", "");
                                            emocionesList.add(emocion);
                                            if(emocionesList.size()>3)
                                                emocionesList.remove(0);
                                            textoEmociones.setText(emocionesList.toString()+"\n Pasar imagen: "+contNextImage +"\n Deteccion activa: "+initDetection);


                                            if(emocionesList.size()>=3 && emocionesList.get(0).equals("asombrado") && emocionesList.get(1).equals("asombrado") && emocionesList.get(2).equals("asombrado"))
                                                initDetection= false;

                                            if(emocionesList.size()>=3 && emocionesList.get(0).equals("triste") && emocionesList.get(1).equals("triste") && emocionesList.get(2).equals("triste"))
                                                initDetection= true;

                                            if(initDetection) {
                                                if (emocionesList.size() >= 3 && emocionesList.get(1).equals("neutral") && emocionesList.get(2).equals("neutral"))
                                                    activarImagen();

                                                if (emocionesList.size() >= 3 && emocionesList.get(0).equals("feliz") && emocionesList.get(1).equals("feliz") && emocionesList.get(1).equals("feliz")) {
                                                    pasarImagen();
                                                    contNextImage--;
                                                }
                                            }




                                            //btnUpload.setEnabled(true);
                                            thread2.interrupt();
                                        }



                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        //dialog.dismiss();

                                    }
                                });
                    }
                });
                thread2.start();

            }
        }else{
            //Toast.makeText(MainActivity.this,"No se pudo cargar la imagen", Toast.LENGTH_LONG).show();
        }
    }

    private Api getApiUpload(){
        return RetrofitCliente.getClienr().create(Api.class);
    }

    @Override
    public void onProgressUpdate(int Porcent) {

    }
    // permisos
    private void checkExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(BATTERY_SERVICE, "Permission not granted WRITE_EXTERNAL_STORAGE.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        225);
            }
        }if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(BATTERY_SERVICE, "Permission not granted CAMERA.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        226);
            }
        }

    }

    //CAMARA**********************************************************************
    //CAMARA**********************************************************************
    //CAMARA**********************************************************************
    //CAMARA**********************************************************************
    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;

    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {

        super.onResume();
        if(mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Log.d("nu", "null");
        }else {
            Log.d("nu","no null");
        }

    }

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(0);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreview.refreshCamera (mCamera);
                    }
                });
                if(bitmap != null) { bitmap.recycle(); bitmap = null; }
                System.gc();
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 450,450, true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),scaledBitmap.getHeight(), matrix, true);
                bitmap = rotatedBitmap;
                //imageView.setImageBitmap(rotatedBitmap);
                //mPreview.refreshCamera (mCamera);
                //Intent intent = new Intent(MainActivity.this,PictureActivity.class);
                //startActivity(intent);
            }
        };
        return picture;
    }

    /*********** para la imagen y movimiento***************/
    public void isPlaying() {
        if(sonidoImagen.isPlaying())
            sonidoImagen.stop();
    }

    public void consultar_datos() {
        String cadena="";
        try {
            SQLiteDatabase base = this.base.getReadableDatabase();
            Cursor c = base.rawQuery("SELECT * FROM Actividades ORDER BY prioridad DESC", null);
            System.out.println(c.getCount());
            actividades_list = new ArrayList<Actividades>();
            if (c != null) {
                c.moveToFirst();
                do {
                    actividades_list.add(new Actividades(c.getString(0),c.getInt(1),c.getString(2).toLowerCase()));
                    cadena += c.getString(2)+"  +++  "+c.getInt(1)+"sfasdf:" +actividades_list.size()+"\n";

                } while (c.moveToNext());
            }
            //Toast.makeText(this,cadena,Toast.LENGTH_LONG).show();

            c.close();
            base.close();
        } catch (SQLiteException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void actualizarPrioridad(Actividades actividad){
        try {
            SQLiteDatabase base = this.base.getReadableDatabase();
            actividades_list.get(index).prioridad++;
            Toast.makeText(this,actividades_list.get(index).prioridad+"",Toast.LENGTH_LONG).show();
            base.execSQL("UPDATE Actividades SET prioridad='"+actividades_list.get(index).prioridad+"' WHERE nombre='"+actividades_list.get(index).nombre+"'");
            base.close();
        } catch (SQLiteException e) {
            Toast.makeText(this,"error al actualizar prioridad",Toast.LENGTH_LONG).show();
        }
    }

    public void acomodar_Lista(){
        Collections.sort(actividades_list, new Comparator<Actividades>() {
            @Override public int compare(Actividades p1, Actividades p2) {
                return p2.prioridad - p1.prioridad; // Ascending
            }
        });
        String cadenaa="";
        for (int i=0;i<actividades_list.size();i++){
            cadenaa += actividades_list.get(i).nombre+"  +++  "+actividades_list.get(i).prioridad+"\n";
        }
    }

}
