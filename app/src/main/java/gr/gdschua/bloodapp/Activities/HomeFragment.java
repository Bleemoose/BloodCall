package gr.gdschua.bloodapp.Activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import gr.gdschua.bloodapp.DatabaseAccess.DAOUsers;
import gr.gdschua.bloodapp.Entities.User;
import gr.gdschua.bloodapp.R;
import gr.gdschua.bloodapp.Utils.LevelHandler;
import gr.gdschua.bloodapp.Utils.QrEncoder;

public class HomeFragment extends Fragment {

    final DAOUsers Udao = new DAOUsers();
    private Context thisContext;
    TextView bloodTypeTV;
    TextView fullNameTextView;
    TextView emailTextView;
    StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("UserImages/" + FirebaseAuth.getInstance().getUid());;
    User currUser;
    Boolean showPermsDialog = true;
    de.hdodenhof.circleimageview.CircleImageView profilePicture;
    SwitchMaterial pushN;


    final ActivityResultLauncher<String> bgLocationRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        setNotifications(result,currUser);
    });


    final ActivityResultLauncher<String> locationRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bgLocationRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }else {
            setNotifications(result,currUser);
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisContext = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        Udao.getUser().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                currUser=dataSnapshot.getValue(User.class);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Udao.getUser().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                currUser=dataSnapshot.getValue(User.class);
            }
        });
    }

    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        bloodTypeTV = view.findViewById(R.id.bloodTypeTextView);
        QrEncoder qrEncoder = new QrEncoder(thisContext);
        fullNameTextView = view.findViewById(R.id.hosp_fullNameTextView);
        ProgressBar progressBar = view.findViewById(R.id.lvlProgressBar);
        TextView lvlTV = view.findViewById(R.id.lvlTV);
        TextView nextLvlTV = view.findViewById(R.id.nextLvlTV);
        ImageView qrIV = view.findViewById(R.id.qrImageView);
        pushN = view.findViewById(R.id.pushNotifSwitch);
        emailTextView = view.findViewById(R.id.hosp_emailTextView);
        profilePicture = view.findViewById(R.id.profilePic);

        view.findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(currUser.getId());
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment_content_user, editProfileFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Udao.getUser().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                //Toggle Switch Listener
                pushN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            handleBgLoc();
                        }
                        else {
                            setNotifications(false,currUser);
                        }
                    }
                });



                //Get logged in user information
                currUser=dataSnapshot.getValue(User.class);
                try {
                    qrIV.setImageBitmap(qrEncoder.encodeAsBitmap("BLCL:"+currUser.getId()));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                progressBar.setProgress(LevelHandler.getLvlCompletionPercentage(currUser.getXp(),LevelHandler.getLevel(currUser.getXp())));
                lvlTV.setText(getResources().getString(R.string.curr_lvl_text, LevelHandler.getLevel(currUser.getXp())));
                nextLvlTV.setText(getResources().getString(R.string.new_lvl_xp, (LevelHandler.getLvlXpCap(LevelHandler.getLevel(currUser.getXp())) - currUser.getXp()), LevelHandler.getLevel(currUser.getXp()) + 1));
                bloodTypeTV.setText(currUser.getBloodType());
                fullNameTextView.setText(currUser.getFullName());
                pushN.setChecked(currUser.getNotifications());
                emailTextView.setText(currUser.getEmail());
                try {
                    File localFile = File.createTempFile(currUser.getId(), "jpg");
                    mStorageReference.getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                profilePicture.setImageBitmap(bitmap);
                                localFile.delete();
                            } else {
                                Log.e("ERROR", "IMAGE NOT FOUND!");
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void handleBgLoc() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && showPermsDialog) {
            new AlertDialog.Builder(thisContext, R.style.CustomDialogTheme)
                    .setTitle(R.string.bg_loc_title)
                    .setMessage(R.string.bg_loc_text)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNotifications(false,currUser);
                            pushN.setChecked(false);
                            dialog.dismiss();
                            showPermsDialog = true;
                        }
                    }).show();
        } else {
            setNotifications(true,currUser);
        }
        showPermsDialog = false;
    }

    private void setNotifications(Boolean state,User currUser){
        if (state) {
            FirebaseMessaging.getInstance().subscribeToTopic(currUser.getTopic());
            currUser.setNotifications(true);
            pushN.setChecked(true);
            if (!currUser.notifFirstTime) {
                currUser.setXp(currUser.getXp() + 10);
                currUser.notifFirstTime = true;
                Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.notif_first_time, Snackbar.LENGTH_LONG);
                snackbar.show();
                currUser.notifFirstTime=true;
            }
            currUser.updateSelf();
        }
        else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic(currUser.getTopic());
            currUser.setNotifications(false);
            pushN.setChecked(false);
            currUser.updateSelf();
        }
    }
}