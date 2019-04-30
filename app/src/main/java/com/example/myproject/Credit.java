package com.example.myproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myproject.Common.Common;
import com.example.myproject.Database.Database;
import com.example.myproject.Model.Request;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class Credit extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int CAM_REQUEST =13;
    String pathToFile;
    ImageView imageView;
    Button scanBtn, btnCheckOut;
    Uri imageUri;
    TextView cardText;
    FirebaseVisionImage image = null;
    List<String> infoAfterProcess = new ArrayList<>();
    EditText cardNumber, yearNumber, validDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
        FirebaseApp.initializeApp(this);

        imageView = findViewById(R.id.imageCard);
        scanBtn = findViewById(R.id.btnScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
            }
        });
        //scanBtn.setOnClickListener(new btnTakePhotoClicker());
        cardNumber = findViewById(R.id.edtCardNumber);
        yearNumber = findViewById(R.id.edtYear);
        validDay = findViewById(R.id.edtValidDate);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });
        imageView.setImageResource(R.drawable.debit);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ImageProcess(bitmap);
    }

    private void ImageProcess(Bitmap bitmap) {
        Resources res = getBaseContext().getResources();
//        image = FirebaseVisionImage.fromBitmap(
//                bitmap);
        image = FirebaseVisionImage.fromBitmap(BitmapFactory.decodeResource(res, R.drawable.debit));
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        if (image != null) {
            Task<FirebaseVisionText> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            Log.d(TAG, "onSuccess: ");
                            List<FirebaseVisionText.Block> blocks = firebaseVisionText.getBlocks();
                            StringBuilder recognisedText = new StringBuilder("");
                            List<String> cardInfo = new ArrayList<String>();

                            for (int i = 0; i < blocks.size(); i++) {
                                Log.d(TAG, "onSuccess: " +blocks.get(i).getText() );
                                recognisedText.append(blocks.get(i).getText() + "\n");
                                cardInfo.add(blocks.get(i).getText());
                            }

                            for (String info:cardInfo)
                            {
                                int start = 0;
                                for (int pos = info.indexOf("\n"); pos != -1; pos = info.indexOf("\n", pos + 1)) {
                                    inforProcess(info.substring(start, pos));
                                    start = pos+1;
                                }
                                inforProcess( info.substring(start,info.length()));
                            }
                            if ("/".contains("/")) Log.d(TAG, "onSuccess: " + "asdasdasdasdasdasdasdasdasdads");
                            //cardText.setText(recognisedText);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: ");
                        }
                    });
        }
        else Log.d(TAG, "onCreate: fail");
    }

    private void inforProcess(String info) {
        Log.d(TAG, "inforProcess: " + info);
        infoAfterProcess.add(info);
        if (info.length()>=16 && info.matches("[0-9 ]+"))
        {

            cardNumber.setText(info.toString());
        }

        if (info.contains("/"))
        {
            String validDayString = info.substring(info.indexOf("/")-2, info.indexOf("/")+3);
            validDay.setText(validDayString);
        }
        if (info.matches("(.*)20.. (.*)"))
        {
            int index = info.indexOf("20");
            String yearNumberString = info.substring(info.indexOf("20"), info.indexOf("20")+4);
            yearNumber.setText(yearNumberString);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAM_REQUEST){
            //Bundle extras = data.getExtras();
            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
            imageView.setImageBitmap(bitmap);
            ImageProcess(bitmap);
        }
    }

    private void dispatchPictureTakerAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            photoFile = createPhotoFile();
            if (photoFile !=null){
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(Credit.this, "com.example.myproject.provider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, CAM_REQUEST);
            }
        }
    }

    String currentPhotoPath = "";
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", getFilesDir());
        } catch (IOException e) {
            Log.d(TAG, "createPhotoFile: Excep" + e.toString());
        }
        return image;
    }
    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Credit.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        final EditText edtAddress = new EditText(Credit.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Create new Request

                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Credit.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                Intent menuIntent = new Intent(Credit.this, Home.class);
                startActivity(menuIntent);
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent menuIntent = new Intent(Credit.this, Home.class);
                startActivity(menuIntent);
            }
        });
        alertDialog.show();
    }

}
