package com.calm_health.sports;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import java.io.File;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityProfile extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    private static final int SELECT_PICTURE = 100;
    private static final int TAKE_PICTURE = 99;

    CircleImageView civ_img;
    Button btn_img;
    EditText et_name, et_birth, et_gendar, et_height, et_weight;
    String strgender = "Male";

    public static Bitmap bmpPatient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(3).setChecked(true);

        initGUI();
    }

    public void initGUI(){
        civ_img = (CircleImageView)findViewById(R.id.proimg);
        et_name = (EditText) findViewById(R.id.pro_name);
        et_birth = (EditText) findViewById(R.id.pro_birth);
        et_gendar = (EditText) findViewById(R.id.pro_gendar);
        et_height = (EditText) findViewById(R.id.pro_height);
        et_weight = (EditText) findViewById(R.id.pro_weight);
        btn_img = (Button) findViewById(R.id.btnimg);

        et_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dFragment = new DatePickerFragment();
                dFragment.show(getFragmentManager(), "Date Picker");
            }
        });
        et_gendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGender();
            }
        });
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changepic();
            }
        });
    }
    public void changepic(){
        CharSequence colors[] = new CharSequence[] {"Take a Picture", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please choose your picture");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                if(which == 0)
                    getImageFromCamera();
                else
                    openImageChooser();
            }
        });
        builder.show();
    }
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    void getImageFromCamera(){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if ( selectedImageUri != null) {
                    decodeSampledBitmapFromFile(selectedImageUri.getPath(), 200, 200);
                    civ_img.setImageBitmap(bmpPatient);
                }
            }
            else if(requestCode == TAKE_PICTURE)
            {
                File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
//                Picasso.with(this).load("https://i.stack.imgur.com/9fZXU.jpg").into(imageViewAndroid);
                decodeSampledBitmapFromFile(file.getAbsolutePath(), 200, 200);
                civ_img.setImageBitmap(bmpPatient);
            }
        }
    }

    public static void decodeSampledBitmapFromFile(String path,int reqWidth, int reqHeight) { // BEST QUALITY MATCH

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }

        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bmpPatient = BitmapFactory.decodeFile(path, options);
    }



    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // DatePickerDialog THEME_DEVICE_DEFAULT_DARK
            DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,this,year,month,day);
            return  dpd;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day){
            String strDate = "";
            if((month+1)<=9)
                strDate +=""+year+"-"+"0"+(month+1)+"-";
            else
                strDate +=""+year+"-"+(month+1)+"-";
            if(day<10)
                strDate +="0"+day+"T"+"00:00:00";
            else
                strDate +=day+"T"+"00:00:00";

            et_birth.setText(year+"/"+(month+1)+"/"+day);
        }
    }

    public void showGender(){
        final Dialog valdlg = new Dialog(this);
        valdlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        valdlg.setContentView(R.layout.showgender);
        Button btnyes = (Button) valdlg.findViewById(R.id.btnyesg);
        Button btnno = (Button) valdlg.findViewById(R.id.btnnog);
        final RadioButton rbfemale = (RadioButton) valdlg.findViewById(R.id.radiofemale);
        final RadioButton rbmale = (RadioButton) valdlg.findViewById(R.id.radiomale);
        if(strgender.contentEquals("Male"))
            rbmale.setChecked(true);
        if(strgender.contentEquals("Female"))
            rbfemale.setChecked(true);
        btnyes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(rbfemale.isChecked())
                    strgender="Female";
                else if(rbmale.isChecked())
                    strgender = "Male";
                else
                    strgender = "";
                if(strgender!="")
                    et_gendar.setText(strgender);
                valdlg.dismiss();
            }
        });
        btnno.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                valdlg.dismiss(); // dismiss the dialog
            }
        });
        valdlg.show();
    }

    NavigationView navigationView;
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("menu", "menu");
        int id = item.getItemId();
        if (id == R.id.nav_exercise) {
            Intent intent = new Intent(ActivityProfile.this, ActivityMonitor.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_sleep) {
            Intent intent = new Intent(ActivityProfile.this, ActivitySleep.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_data) {
            Intent intent = new Intent(ActivityProfile.this, ActivityData.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_profile) {


        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(ActivityProfile.this, ActivitySettings.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_exit) {
            this.finish();
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }
}
