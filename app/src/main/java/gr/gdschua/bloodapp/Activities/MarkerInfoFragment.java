package gr.gdschua.bloodapp.Activities;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import gr.gdschua.bloodapp.R;
public class MarkerInfoFragment extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_marker_info, container, false);
        TextView tv = view.findViewById(R.id.textViewMarkerTitle);
        TextView tv2=view.findViewById(R.id.textViewMarkerCenter);
        tv.setText(getArguments().get("name").toString());
        if(getArguments().get("organizer")!=null) {
            tv2.setText("Address: "+getArguments().get("address").toString()+"\nOrganized By:"+getArguments().get("organizer").toString()+"\nEmail: "+getArguments().get("email").toString());
        }else {
            tv2.setText("Address: "+getArguments().get("address").toString()+"\nEmail: "+getArguments().get("email").toString());
        }
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.30f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }
}