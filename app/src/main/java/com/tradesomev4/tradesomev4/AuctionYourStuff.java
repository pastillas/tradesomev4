package com.tradesomev4.tradesomev4;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.tradesomev4.tradesomev4.m_Helpers.Connectivity;
import com.tradesomev4.tradesomev4.m_Helpers.SnackBars;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuctionYourStuff extends AppCompatActivity implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener{
    private static final String log = "Auction your stuff";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 101;
    private static final int PICK_IMAGE_REQUEST = 102;
    private static final int REVIEW_AND_PUBLISH_CODE = 103;
    private int primaryPreselect;
    private int accentPreselect;
    private Toast mToast;
    private Thread mThread;
    private Handler mHandler;
    private int chooserDialog;
    private Button uploadImage;
    private Uri capturedUri1 = null;
    private Uri compressUri1 = null;
    private Uri capturedUri2 = null;
    private Uri compressUri2 = null;
    private Uri capturedUri3 = null;
    private Uri compressUri3 = null;
    private Uri capturedUri4 = null;
    private Uri compressUri4 = null;

    private ImageView itemImage1;
    private ImageView itemImage2;
    private ImageView itemImage3;
    private ImageView itemImage4;
    private int choice = 1;
    private String mCurrentPhotoPath;
    private StorageReference storageRef;
    private TextView categoriesLabel;
    private EditText itemTitle;
    private EditText startingBid;
    private EditText description;
    private TextView category;
    private String itemTitleStr;
    private int startingBidInt;
    private String descriptionStr;
    private String categoryStr;
    private boolean onSave = false;
    Toolbar toolbar;
    boolean isConnected;
    boolean isConnectionDisabledShowed;
    boolean isConnectionRestoredShowed;
    SnackBars snackBars;
    View parentView;
    int puta;
    private static final String DEBUG_TAG = "DEBUG_TAG";

    public void timer(){
        final CountDownTimer c = new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long l) {
                Log.d(DEBUG_TAG, "TIMER: " + l);
            }

            public void onFinish() {
                Connectivity connectivity = new Connectivity(getApplicationContext());

                if(!connectivity.isConnected()) {
                    isConnectionRestoredShowed = false;
                    isConnected = false;

                    if(puta == 1)
                        puta++;

                    if(!isConnectionDisabledShowed){
                        snackBars.showConnectionDisabledDialog();
                        isConnectionDisabledShowed = true;
                    }
                } else {
                    isConnected = true;
                    isConnectionDisabledShowed = false;

                    if(puta != 1 && !isConnectionRestoredShowed){
                        snackBars.showConnectionRestored();
                        isConnectionRestoredShowed = true;
                    }
                }

                timer();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_your_stuff);
        toolbar = (Toolbar) findViewById(R.id.app_bar_messaging);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        puta = 1;
        parentView = findViewById(R.id.scrollView);
        snackBars = new SnackBars(parentView, getApplicationContext());
        isConnectionDisabledShowed = false;
        isConnectionRestoredShowed = false;

        storageRef = FirebaseStorage.getInstance().getReference();

        itemImage1 = (ImageView) findViewById(R.id.itemImage1);
        itemImage2 = (ImageView) findViewById(R.id.itemImage2);
        itemImage3 = (ImageView) findViewById(R.id.itemImage3);
        itemImage4 = (ImageView) findViewById(R.id.itemImage4);

        itemImage1.setOnClickListener(this);
        itemImage2.setOnClickListener(this);
        itemImage3 .setOnClickListener(this);
        itemImage4.setOnClickListener(this);
        findViewById(R.id.selectCategory).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);

        itemTitle = (EditText) findViewById(R.id.itemTitle);
        startingBid = (EditText) findViewById(R.id.startingBid);
        description = (EditText) findViewById(R.id.description);
        category = (TextView) findViewById(R.id.category);

        mHandler = new Handler();
        primaryPreselect = DialogUtils.resolveColor(this, R.attr.colorPrimary);
        accentPreselect = DialogUtils.resolveColor(this, R.attr.colorAccent);
        timer();
        editMode();
    }

    public void editMode(){
        Bundle extras = getIntent().getExtras();


        if(extras != null){

            compressUri1 = Uri.parse(extras.getString("image1"));
            compressUri2 = Uri.parse(extras.getString("image2"));
            compressUri3 = Uri.parse(extras.getString("image3"));
            compressUri4 = Uri.parse(extras.getString("image4"));
            itemTitleStr = extras.getString("itemTitleStr");

            String startingBidStr = extras.getString("startingBidInt");
            startingBidInt = Integer.parseInt(startingBidStr);
            descriptionStr = extras.getString("descriptionStr");
            categoryStr = extras.getString("categoryStr");

            try {
                itemImage1.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri1));
                itemImage2.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri2));
                itemImage3.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri3));
                itemImage4.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri4));
            } catch (IOException e) {
                e.printStackTrace();
            }

            itemTitle.setText(itemTitleStr);
            startingBid.setText(startingBidStr);
            description.setText(descriptionStr);
            category.setText(categoryStr);
        }
    }

    public void showBasic() {
        new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(R.string.errorContent)
                .positiveText(R.string.continueBtn)
                .show();
    }

    public void showList() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialogUploadIMageTitle)
                .items(R.array.dialogUPloagIMageItems)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0)
                            requestPermission();
                        else if (which == 1)
                            dispatchLunchGallery();
                    }
                })

                .positiveText(android.R.string.cancel)
                .show();
    }

    public void showSingleChoice() {
        new MaterialDialog.Builder(this)
                .title(R.string.chooseCategoryDialogTitle)
                .items(R.array.categories)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        //showToast(which + ": " + text);
                        categoryStr = text.toString();
                        category.setText(text);
                        return true; // allow selection
                    }
                })
                .positiveText(R.string.md_choose_label)
                .show();
    }


    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "You need enable the permission for External Storage Write" +
                            " to add photos of your item.", Toast.LENGTH_LONG).show();
                    return;
                }
                break;
        }
    }

    private void dispatchLunchGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException e) {
            }

            if (photoFile != null) {
                switch (choice) {
                    case 1:
                        capturedUri1 = Uri.fromFile(photoFile);
                        Log.d(log, "Log1: " + String.valueOf(capturedUri1));
                        Log.d(log, "Log2: " + capturedUri1.toString());

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri1);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        break;

                    case 2:
                        capturedUri2 = Uri.fromFile(photoFile);
                        Log.d(log, "Log1: " + String.valueOf(capturedUri2));
                        Log.d(log, "Log2: " + capturedUri2.toString());

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri2);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        break;

                    case 3:
                        capturedUri3 = Uri.fromFile(photoFile);
                        Log.d(log, "Log1: " + String.valueOf(capturedUri3));
                        Log.d(log, "Log2: " + capturedUri3.toString());

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri3);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        break;

                    case 4:
                        capturedUri4 = Uri.fromFile(photoFile);
                        Log.d(log, "Log1: " + String.valueOf(capturedUri4));
                        Log.d(log, "Log2: " + capturedUri4.toString());

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri4);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        break;
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d(log, "mCurrentPhotoPath: " + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                //Bundle extras = data.getExtras();
                //Bitmap bitmap = (Bitmap) extras.get("data");
                //itemImage1.setImageBitmap(bitmap);
                switch (choice) {
                    case 1:
                        new ImageCompressionAsyncTask(this).execute(capturedUri1.toString());
                        break;

                    case 2:
                        new ImageCompressionAsyncTask(this).execute(capturedUri2.toString());
                        break;

                    case 3:
                        new ImageCompressionAsyncTask(this).execute(capturedUri3.toString());
                        break;

                    case 4:
                        new ImageCompressionAsyncTask(this).execute(capturedUri4.toString());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                switch (choice) {
                    case 1:
                        capturedUri1 = data.getData();
                        new ImageCompressionAsyncTask(this).execute(capturedUri1.toString());
                        break;
                    case 2:
                        capturedUri2 = data.getData();
                        new ImageCompressionAsyncTask(this).execute(capturedUri2.toString());
                        break;
                    case 3:
                        capturedUri3 = data.getData();
                        new ImageCompressionAsyncTask(this).execute(capturedUri3.toString());
                        break;
                    case 4:
                        capturedUri4 = data.getData();
                        new ImageCompressionAsyncTask(this).execute(capturedUri4.toString());
                        break;
                }
            }
        }
    }


    class ImageCompressionAsyncTask extends AsyncTask<String, Void, String> {
        Context mContext;

        public ImageCompressionAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {

            String filepath = SiliCompressor.with(mContext).compress(params[0]);
            return filepath;
        }

        @Override
        protected void onPostExecute(String s) {
            File imageFile = new File(s);

            switch (choice) {
                case 1:
                    compressUri1 = Uri.fromFile(imageFile);
                    break;
                case 2:
                    compressUri2 = Uri.fromFile(imageFile);
                    break;
                case 3:
                    compressUri3 = Uri.fromFile(imageFile);
                    break;
                case 4:
                    compressUri4 = Uri.fromFile(imageFile);
                    break;
            }

            try {
                switch (choice) {
                    case 1:
                        //Toast.makeText(AuctionYourStuff.this, "Compress done", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri1);
                        itemImage1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        itemImage1.setImageBitmap(bitmap1);
                        //uploadImage(compressUri1);
                        break;

                    case 2:
                        //Toast.makeText(AuctionYourStuff.this, "Compress done", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri2);
                        itemImage2.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        itemImage2.setImageBitmap(bitmap2);
                        //uploadImage(compressUri2);
                        break;

                    case 3:
                        //Toast.makeText(AuctionYourStuff.this, "Compress done", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap3 = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri3);
                        itemImage3.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        itemImage3.setImageBitmap(bitmap3);
                        //uploadImage(compressUri3);
                        break;
                    case 4:
                        //Toast.makeText(AuctionYourStuff.this, "Compress done", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap4 = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri4);
                        itemImage4.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        itemImage4.setImageBitmap(bitmap4);
                        //uploadImage(compressUri4);
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadImage(Uri compressUri) {
        StorageReference tmpStorageRef = storageRef.child("images/" + compressUri.getLastPathSegment());
        UploadTask uploadTask = tmpStorageRef.putFile(compressUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AuctionYourStuff.this, "jhgjhg", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(AuctionYourStuff.this, "Upload Done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mThread != null && !mThread.isInterrupted() && mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    private void validate() {
        boolean valid = true;

        if (compressUri1 == null || compressUri2 == null || compressUri3 == null || compressUri4 == null)
            valid = false;

        itemTitleStr = itemTitle.getText().toString();

        String startingBidStrTemp = startingBid.getText().toString();

        if (startingBidStrTemp.length() > 0) {
            startingBidInt = Integer.parseInt(startingBidStrTemp);
        }

        descriptionStr = description.getText().toString();

        if (TextUtils.isEmpty(itemTitleStr)) {
            itemTitle.setError("Required.");
            valid = false;
        } else {
            itemTitle.setError(null);
        }

        if (TextUtils.isEmpty(startingBidStrTemp)) {
            startingBid.setError("Required.");
            valid = false;
        } else {
            startingBid.setError(null);
        }

        if (TextUtils.isEmpty(descriptionStr)) {
            description.setError("Required.");
            valid = false;
        } else {
            description.setError(null);
        }


        if (TextUtils.isEmpty(categoryStr))
            valid = false;

        if (valid) {
            onSave = true;
            Intent intent = new Intent(getApplicationContext(), ReviewAndPublish.class);
            intent.putExtra("image1", compressUri1.toString());
            intent.putExtra("image2", compressUri2.toString());
            intent.putExtra("image3", compressUri3.toString());
            intent.putExtra("image4", compressUri4.toString());
            intent.putExtra("itemTitleStr", itemTitleStr);
            intent.putExtra("startingBidInt", startingBidInt+"");
            intent.putExtra("descriptionStr", descriptionStr);
            intent.putExtra("categoryStr", categoryStr);
            startActivity(intent);
        } else {
            showBasic();
        }
    }

    @Override
    public void onClick(View v) {
        int item = v.getId();

        switch (item) {
            case R.id.itemImage1:
                choice = 1;
                showList();
                break;

            case R.id.itemImage2:
                choice = 2;
                showList();
                break;

            case R.id.itemImage3:
                choice = 3;
                showList();
                break;

            case R.id.itemImage4:
                choice = 4;
                showList();
                break;

            case R.id.selectCategory:
                showSingleChoice();
                break;

            case R.id.next:
                validate();
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        categoriesLabel.setText(String.valueOf(parent.getItemAtPosition(position)));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        categoriesLabel.setText(String.valueOf(parent.getItemAtPosition(0)));
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
