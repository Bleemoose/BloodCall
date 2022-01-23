package gr.gdschua.bloodapp.Activities.HospitalActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gr.gdschua.bloodapp.DatabaseAccess.DAOAlerts;
import gr.gdschua.bloodapp.Entities.Alert;
import gr.gdschua.bloodapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HospitalAddAlertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HospitalAddAlertFragment extends Fragment {
    DAOAlerts daoAlerts=new DAOAlerts();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HospitalAddAlertFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HospitalAddAlertFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HospitalAddAlertFragment newInstance(String param1, String param2) {
        HospitalAddAlertFragment fragment = new HospitalAddAlertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_hospital_add_alert, container, false);
        Spinner bloodTypeSpn=view.findViewById(R.id.bloodtype_spinner);
        Spinner posNegSpn=view.findViewById(R.id.bloodtype_spinner_pos_neg);
        view.findViewById(R.id.createAlertBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alert alert=new Alert(FirebaseAuth.getInstance().getUid(), bloodTypeSpn.getSelectedItem().toString()+posNegSpn.getSelectedItem().toString(),new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
                daoAlerts.insertAlert(alert);
            }
        });
        return view;
    }
}